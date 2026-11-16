package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.CapacityLockRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bloquea los recursos viejos y nuevos antes de contar la demanda final agregada. */
@Service
@RequiredArgsConstructor
public class ReservationUpdateCapacityService {
    private final CapacityLockRepository locks;
    private final ReservationRepository reservations;

    private record Slot(LocalDate date, String schedule, boolean returning) {
        String key() { return date + "|" + (returning ? "DAY" : schedule) + "|" + (returning ? "RETURN" : "OUTBOUND"); }
    }

    @Transactional
    public void validateReplacement(List<Reservation> before, List<Reservation> after,
            LocalDate outboundDate) {
        var now = ArgentinaTime.now();
        Map<Slot, Integer> oldDemand = demand(before, outboundDate, now);
        Map<Slot, Integer> newDemand = demand(after, outboundDate, now);
        var keys = new TreeSet<String>();
        oldDemand.keySet().forEach(slot -> keys.add(slot.key()));
        newDemand.keySet().forEach(slot -> keys.add(slot.key()));
        keys.forEach(this::lock);
        Set<UUID> excluded = new HashSet<>();
        before.forEach(leg -> excluded.add(leg.getId()));
        for (var entry : newDemand.entrySet()) {
            Slot slot = entry.getKey();
            long available = slot.returning()
                    ? ReturnCapacityPolicy.availableSeats(reservations, slot.date(), slot.schedule(), now, excluded)
                    : ReturnCapacityPolicy.CAPACITY - reservations.countReservedSeatsExcluding(
                            slot.date(), slot.schedule(), excluded);
            if (entry.getValue() > available) {
                throw new SeatCapacityExceededException("No hay asientos suficientes para el turno "
                        + slot.date() + " " + slot.schedule() + ".");
            }
        }
    }

    public void lock(String key) {
        locks.ensureExists(key);
        if (locks.findForUpdate(key) == null) {
            throw new DomainValidationException("No se pudo bloquear el recurso de reserva.");
        }
    }

    private Map<Slot, Integer> demand(List<Reservation> group, LocalDate outboundDate,
            java.time.LocalDateTime now) {
        Map<Slot, Integer> demand = new HashMap<>();
        for (Reservation leg : group) {
            if (!active(leg) || leg.isSpecialTrip() || leg.isFlexibleReturnTime()
                    && leg.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN) continue;
            boolean returning = TripRouteCalculatorService.isCordoba(leg.getPickupLocality());
            boolean open = leg.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN;
            LocalDate date = open ? outboundDate : leg.getTravelDate();
            if (date == null || LocalDate.of(2099, 12, 31).equals(date)) continue;
            if (open && (!returning || !now.isBefore(date.atTime(11, 0)))) continue;
            List<String> schedules = returning && (open || leg.getDepartureSchedule() == null
                    || leg.getDepartureSchedule().isBlank()) ? List.of("14:00", "17:30")
                    : List.of(ReturnCapacityPolicy.normalizeSchedule(leg.getDepartureSchedule()));
            for (String schedule : schedules) {
                demand.merge(new Slot(date, schedule, returning), leg.getTotalSeats(), Integer::sum);
            }
        }
        // Una vuelta sin hora ocupa cualquier horario del día, incluidos horarios manuales del plan.
        for (Slot slot : List.copyOf(demand.keySet())) {
            if (!slot.returning() || List.of("14:00", "17:30").contains(slot.schedule())) continue;
            int wildcard = group.stream().filter(ReservationUpdateCapacityService::active)
                    .filter(r -> !r.isSpecialTrip() && (!r.isFlexibleReturnTime()
                            || r.getTravelStatus() != Reservation.TravelStatus.OPEN_RETURN))
                    .filter(r -> TripRouteCalculatorService.isCordoba(r.getPickupLocality()))
                    .filter(r -> r.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN
                            || r.getDepartureSchedule() == null || r.getDepartureSchedule().isBlank())
                    .filter(r -> r.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN
                            ? slot.date().equals(outboundDate) && now.isBefore(slot.date().atTime(11, 0))
                            : slot.date().equals(r.getTravelDate()))
                    .mapToInt(Reservation::getTotalSeats).sum();
            demand.merge(slot, wildcard, Integer::sum);
        }
        return demand;
    }

    public static boolean active(Reservation reservation) {
        return !Set.of("CANCELLED", "CANCELED", "EXPIRED", "REJECTED")
                .contains(Objects.toString(reservation.getStatus(), "").toUpperCase(Locale.ROOT))
                && reservation.getTravelStatus() != Reservation.TravelStatus.CANCELED
                && reservation.getTravelStatus() != Reservation.TravelStatus.NO_SHOW;
    }
}
