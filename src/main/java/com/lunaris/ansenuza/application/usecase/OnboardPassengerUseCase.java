package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OnboardPassengerUseCase {

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final LocalityRepository localityRepository;
    private final WhatsAppService whatsAppService;

    @Transactional
    public Reservation execute(UUID reservationId) {
        return updateTravelStatus(reservationId, Reservation.TravelStatus.ONBOARD);
    }

    @Transactional
    public Reservation updateTravelStatus(
            UUID reservationId, Reservation.TravelStatus newStatus) {
        Reservation initial = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        if (newStatus != Reservation.TravelStatus.ONBOARD) {
            initial.setTravelStatus(newStatus);
            return reservationRepository.saveAndFlush(initial);
        }
        LocalDate effectiveDate = effectiveLegDate(initial);
        if (initial.getDriver() == null || effectiveDate == null) {
            throw new IllegalStateException("La reserva no pertenece a una ruta asignada.");
        }
        UUID driverId = initial.getDriver().getId();
        if (driverId == null || driverRepository.findAllByIdForUpdate(java.util.Set.of(driverId)).isEmpty()) {
            throw new IllegalStateException("No se pudo bloquear el chofer de la ruta.");
        }
        Reservation onboard = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        LocalDate lockedEffectiveDate = effectiveLegDate(onboard);
        if (onboard.getDriver() == null || !driverId.equals(onboard.getDriver().getId())
                || lockedEffectiveDate == null || !effectiveDate.equals(lockedEffectiveDate)) {
            throw new IllegalStateException(
                    "La ruta cambió durante el abordaje. Reintentá la operación.");
        }
        if (onboard.getTravelStatus() == Reservation.TravelStatus.ONBOARD
                || onboard.getTravelStatus() == Reservation.TravelStatus.BOARDED) {
            return onboard;
        }

        onboard.setTravelStatus(Reservation.TravelStatus.ONBOARD);
        reservationRepository.saveAndFlush(onboard);

        findNextPassengerInRoute(onboard, lockedEffectiveDate)
                .ifPresent(next -> notifyNext(onboard, next));
        return onboard;
    }

    private Optional<Reservation> findNextPassengerInRoute(
            Reservation onboard, LocalDate effectiveDate) {
        List<Reservation> route = reservationRepository.findRouteByEffectiveDate(
                onboard.getDriver().getId(), effectiveDate);
        Comparator<Reservation> fallbackOrder = Comparator
                .comparing(Reservation::getDepartureSchedule,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reservation::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reservation::getId,
                        Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<Reservation> routeOrder = onboard.getRouteSequence() == null
                ? fallbackOrder
                : Comparator.comparing(
                        Reservation::getRouteSequence,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(fallbackOrder);
        List<Reservation> orderedRoute = route.stream()
                .filter(candidate -> belongsToSameLegAndDate(onboard, candidate, effectiveDate))
                .sorted(routeOrder)
                .toList();
        if (onboard.getRouteSequence() != null) {
            return orderedRoute.stream()
                    .filter(candidate -> candidate.getRouteSequence() != null)
                    .filter(candidate -> candidate.getRouteSequence()
                            == onboard.getRouteSequence() + 1)
                    .findFirst();
        }
        int currentIndex = orderedRoute.stream()
                .map(Reservation::getId)
                .toList()
                .indexOf(onboard.getId());
        return currentIndex >= 0 && currentIndex + 1 < orderedRoute.size()
                ? Optional.of(orderedRoute.get(currentIndex + 1))
                : Optional.empty();
    }

    private boolean belongsToSameLegAndDate(
            Reservation onboard, Reservation candidate, LocalDate effectiveDate) {
        return isReturnLeg(onboard) == isReturnLeg(candidate)
                && effectiveDate.equals(effectiveLegDate(candidate));
    }

    private LocalDate effectiveLegDate(Reservation reservation) {
        if (isReturnLeg(reservation) && reservation.getReturnDate() != null) {
            return reservation.getReturnDate();
        }
        return reservation.getTravelDate() != null
                ? reservation.getTravelDate()
                : reservation.getReturnDate();
    }

    private boolean isReturnLeg(Reservation reservation) {
        return reservation.getReservationCode() != null
                && reservation.getReservationCode().endsWith("-VUELTA");
    }

    private void notifyNext(Reservation onboard, Reservation next) {
        if (next.getPassenger() == null || next.getPassenger().getPhone() == null
                || next.getPassenger().getPhone().isBlank()) {
            return;
        }
        int etaMinutes = calculateEtaMinutes(onboard.getPickupLocality(), next.getPickupLocality());
        whatsAppService.sendProximoEnCaminoTemplate(
                next.getPassenger().getPhone(),
                next.getPassenger().getFirstName(),
                onboard.getDriver().getFullName(),
                etaMinutes);
    }

    private int calculateEtaMinutes(String currentLocality, String nextLocality) {
        int currentMinutes = localityRepository.findFirstByNameIgnoreCase(currentLocality)
                .map(Locality::getMinutesFromOrigin)
                .orElse(0);
        int nextMinutes = localityRepository.findFirstByNameIgnoreCase(nextLocality)
                .map(Locality::getMinutesFromOrigin)
                .orElse(0);
        return Math.abs(nextMinutes - currentMinutes);
    }
}
