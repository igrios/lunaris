package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import com.lunaris.ansenuza.shared.PhoneUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateReservationUseCase {

    private final ReservationService reservationService;
    private final PassengerRepository passengerRepository;
    private final PricingAndScheduleService pricingAndScheduleService;

    @Value("${lunaris.trips.capacity:4}")
    private int tripCapacity = 8;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation execute(CreateReservationRequest request) {
        return execute(request, null);
    }

    public Reservation execute(CreateReservationRequest request, String paymentReceiptUrl) {
        validate(request);

        Passenger passenger = resolvePassenger(request);

        // 🌟 Lógica del desplegable de asientos (Captura directa)
        Integer safePassengerCount = (request.passengerCount() == null || request.passengerCount() <= 0) ? 1 : request.passengerCount();
        String departureSchedule = resolveSchedule(request.notes());
        long occupiedSeats = pricingAndScheduleService.countReservedSeats(
                request.travelDate(), departureSchedule);
        if (occupiedSeats + safePassengerCount > tripCapacity) {
            throw new SeatCapacityExceededException(
                    "No hay asientos suficientes para el turno seleccionado. Disponibles: "
                            + Math.max(0, tripCapacity - occupiedSeats) + ".");
        }
        Boolean safePaymentVerified = Boolean.TRUE.equals(request.paymentVerified());
        String initialStatus = safePaymentVerified ? "CONFIRMED" : "PENDING_PAYMENT";

        // Centralizamos la cotización en el servicio de pricing para no duplicar reglas.
        TripType tripType = resolveTripType(request);
        boolean pairedTrip = tripType != TripType.ONE_WAY;
        var computedAmount = pricingAndScheduleService.calculateReservationAmount(
                effectivePickupLocality(request), effectiveDestination(request),
                pairedTrip, safePassengerCount);
        if (request.tripType() != null && pairedTrip) {
            computedAmount = computedAmount.multiply(java.math.BigDecimal.valueOf(2));
        }

        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .travelDate(request.travelDate())
                .pickupLocality(effectivePickupLocality(request))
                .pickupAddress(request.pickupAddress())
                .destination(effectiveDestination(request))
                .roundTrip(pairedTrip)
                .tripType(tripType)
                .returnDate(tripType == TripType.ROUND_TRIP ? request.returnDate() : null)
                .passengerCount(safePassengerCount)
                .companionNames(request.companionNames())
                .paymentVerified(safePaymentVerified)
                .status(initialStatus)
                .source(request.source() != null ? request.source() : ReservationSource.WEB)
                .amount(computedAmount) // 🌟 Inyectamos el monto calculado automáticamente
                .notes(request.notes())
                .departureSchedule(departureSchedule)
                .paymentReceiptUrl(paymentReceiptUrl)
                .build();

        List<Reservation> savedReservations = reservationService.saveReservationFlow(reservation);
        
        return savedReservations.get(0);
    }

    private TripType resolveTripType(CreateReservationRequest request) {
        if (request.tripType() != null) {
            return request.tripType();
        }
        return Boolean.TRUE.equals(request.roundTrip())
                ? (request.returnDate() == null ? TripType.OPEN_RETURN : TripType.ROUND_TRIP)
                : TripType.ONE_WAY;
    }

    private void validate(CreateReservationRequest request) {
        if (request == null || request.travelDate() == null) {
            throw new DomainValidationException("Pasajero y fecha de viaje son obligatorios.");
        }
        if (request.passengerId() == null
                && (request.fullName() == null || request.fullName().isBlank()
                || request.phone() == null || request.phone().isBlank())) {
            throw new DomainValidationException("Nombre y teléfono del pasajero son obligatorios.");
        }
        if (effectivePickupLocality(request).length() < 3
                || effectiveDestination(request).length() < 3) {
            throw new DomainValidationException("Origen y destino deben tener al menos tres caracteres.");
        }
    }

    private Passenger resolvePassenger(CreateReservationRequest request) {
        if (request.passengerId() != null) {
            return passengerRepository.findById(request.passengerId())
                    .orElseThrow(() -> new DomainValidationException("El pasajero indicado no existe."));
        }

        String phone = PhoneUtils.normalizeArgentinePhone(request.phone());
        NameParts name = splitFullName(request.fullName());
        return passengerRepository.findByPhone(phone)
                .map(existing -> repairIncompleteName(existing, name))
                .orElseGet(() -> passengerRepository.save(Passenger.builder()
                        .firstName(name.firstName())
                        .lastName(name.lastName())
                        .phone(phone)
                        .build()));
    }

    private Passenger repairIncompleteName(Passenger passenger, NameParts submittedName) {
        if (isMissingLastName(passenger.getLastName()) && !"Sin apellido".equals(submittedName.lastName())) {
            passenger.setFirstName(submittedName.firstName());
            passenger.setLastName(submittedName.lastName());
            return passengerRepository.save(passenger);
        }
        return passenger;
    }

    private boolean isMissingLastName(String lastName) {
        return lastName == null || lastName.isBlank() || "Sin apellido".equalsIgnoreCase(lastName.trim());
    }

    private NameParts splitFullName(String fullName) {
        String normalizedName = fullName.trim().replaceAll("\\s+", " ");
        int separator = normalizedName.lastIndexOf(' ');
        return separator > 0
                ? new NameParts(normalizedName.substring(0, separator), normalizedName.substring(separator + 1))
                : new NameParts(normalizedName, "Sin apellido");
    }

    private record NameParts(String firstName, String lastName) {
    }

    private String effectivePickupLocality(CreateReservationRequest request) {
        return request.pickupLocality() == null || request.pickupLocality().isBlank()
                ? "Sin especificar" : request.pickupLocality().trim();
    }

    private String effectiveDestination(CreateReservationRequest request) {
        return request.destination() == null || request.destination().isBlank()
                ? "Sin especificar" : request.destination().trim();
    }

    private String resolveSchedule(String notes) {
        return notes != null && notes.contains("08:00") ? "08:00 AM" : "03:00 AM";
    }
}
