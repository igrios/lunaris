package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitingListConversionService {

    private final WaitingListRepository waitingListRepository;
    private final PassengerRepository passengerRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final SystemConfigurationService systemConfigurationService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation convert(Long id) {
        WaitingListEntry entry = requireWaitingEntry(id);
        int requestedSeats = Math.max(1, entry.getPassengerCount());
        int occupiedSeats = safeOccupiedSeats(entry);
        int maxCapacity = systemConfigurationService.getScheduleMaxCapacity();
        if (occupiedSeats + requestedSeats > maxCapacity) {
            throw new SeatCapacityExceededException(
                    "No hay cupo para promover esta entrada. Disponibles: "
                            + Math.max(0, maxCapacity - occupiedSeats) + ".");
        }

        Passenger passenger = resolvePassenger(entry);
        BigDecimal amount = pricingAndScheduleService.calculateReservationAmount(
                entry.getPickupLocality(), entry.getDestination(),
                TripType.ONE_WAY, requestedSeats);
        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .travelDate(entry.getTravelDate())
                .pickupLocality(entry.getPickupLocality())
                .destination(entry.getDestination())
                .roundTrip(false)
                .tripType(TripType.ONE_WAY)
                .passengerCount(requestedSeats)
                .paymentVerified(false)
                .status("CONFIRMED")
                .source(ReservationSource.MANUAL)
                .amount(amount)
                .notes("Promovida desde lista de espera")
                .build();
        List<Reservation> saved = reservationService.saveReservationFlow(reservation);
        entry.setStatus(WaitingListEntry.CONFIRMED);
        waitingListRepository.saveAndFlush(entry);
        return saved.getFirst();
    }

    @Transactional
    public WaitingListEntry cancel(Long id) {
        WaitingListEntry entry = requireWaitingEntry(id);
        entry.setStatus(WaitingListEntry.CANCELLED);
        return waitingListRepository.saveAndFlush(entry);
    }

    private WaitingListEntry requireWaitingEntry(Long id) {
        WaitingListEntry entry = waitingListRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        if (!WaitingListEntry.WAITING.equals(entry.getStatus())) {
            throw new DomainValidationException(
                    "La entrada ya no se encuentra en estado WAITING.");
        }
        return entry;
    }

    private int safeOccupiedSeats(WaitingListEntry entry) {
        Integer occupied = reservationRepository.countConfirmedPassengersByRouteAndDate(
                entry.getPickupLocality(), entry.getDestination(), entry.getTravelDate());
        return occupied == null ? 0 : occupied;
    }

    private Passenger resolvePassenger(WaitingListEntry entry) {
        String normalizedPhone = PhoneUtils.normalizeArgentinePhone(entry.getPhoneNumber());
        return passengerRepository.findByPhone(normalizedPhone).orElseGet(() -> {
            String normalizedName = entry.getPassengerName().trim().replaceAll("\\s+", " ");
            int separator = normalizedName.lastIndexOf(' ');
            String firstName = separator > 0 ? normalizedName.substring(0, separator) : normalizedName;
            String lastName = separator > 0 ? normalizedName.substring(separator + 1) : "Sin apellido";
            return passengerRepository.saveAndFlush(Passenger.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .phone(normalizedPhone)
                    .build());
        });
    }
}
