package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
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
    private final ReservationService reservationService;
    private final PricingAndScheduleService pricingAndScheduleService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation convert(Long id) {
        WaitingListEntry entry = requireWaitingEntry(id);
        Reservation reservation = createReservation(entry, "CONFIRMED");
        entry.setStatus(WaitingListEntry.CONFIRMED);
        waitingListRepository.saveAndFlush(entry);
        return reservation;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation beginPayment(Long id) {
        WaitingListEntry entry = waitingListRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        if (!WaitingListEntry.NOTIFIED.equals(entry.getStatus())) {
            throw new DomainValidationException(
                    "La entrada ya no está disponible para confirmar.");
        }
        Reservation reservation = createReservation(entry, "PENDING_PAYMENT");
        entry.setStatus(WaitingListEntry.AWAITING_PAYMENT);
        waitingListRepository.saveAndFlush(entry);
        return reservation;
    }

    private Reservation createReservation(WaitingListEntry entry, String status) {
        int requestedSeats = Math.max(1, entry.getPassengerCount());

        // La promoción fue autorizada explícitamente por Operaciones, normalmente luego de
        // asignar un coche de refuerzo. Por eso este flujo no reaplica el cupo nominal global.
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
                .requiresInvoice(true)
                .status(status)
                .source(ReservationSource.MANUAL)
                .amount(amount)
                .waitingListEntryId(entry.getId())
                .notes("Promovida desde lista de espera")
                .build();
        List<Reservation> saved = reservationService.saveReservationFlow(reservation);
        return saved.getFirst();
    }

    @Transactional
    public WaitingListEntry cancel(Long id) {
        WaitingListEntry entry = waitingListRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        if (!WaitingListEntry.PENDING.equals(entry.getStatus())
                && !WaitingListEntry.WAITING.equals(entry.getStatus())
                && !WaitingListEntry.NOTIFIED.equals(entry.getStatus())) {
            throw new DomainValidationException(
                    "La entrada ya no se puede cancelar desde este flujo.");
        }
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
