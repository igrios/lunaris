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
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateReservationUseCase {

    private final ReservationService reservationService;
    private final PassengerRepository passengerRepository;
    private final PricingAndScheduleService pricingAndScheduleService;

    @Value("${lunaris.trips.capacity:8}")
    private int tripCapacity = 8;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation execute(CreateReservationRequest request) {
        validate(request);

        Passenger passenger =
                passengerRepository.findById(request.passengerId())
                        .orElseThrow(() -> new DomainValidationException("El pasajero indicado no existe."));

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
        var computedAmount = pricingAndScheduleService.calculateReservationAmount(
                request.pickupLocality(),
                request.destination(),
                request.roundTrip(),
                safePassengerCount);

        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .travelDate(request.travelDate())
                .pickupLocality(request.pickupLocality())
                .pickupAddress(request.pickupAddress())
                .destination(request.destination())
                .roundTrip(request.roundTrip())
                .returnDate(request.returnDate())
                .passengerCount(safePassengerCount)
                .companionNames(request.companionNames())
                .paymentVerified(safePaymentVerified)
                .status(initialStatus)
                .amount(computedAmount) // 🌟 Inyectamos el monto calculado automáticamente
                .notes(request.notes())
                .departureSchedule(departureSchedule)
                .build();

        List<Reservation> savedReservations = reservationService.saveReservationFlow(reservation);
        
        return savedReservations.get(0);
    }

    private void validate(CreateReservationRequest request) {
        if (request == null || request.passengerId() == null || request.travelDate() == null) {
            throw new DomainValidationException("Pasajero y fecha de viaje son obligatorios.");
        }
        if (request.pickupLocality() == null || request.pickupLocality().isBlank()
                || request.destination() == null || request.destination().isBlank()) {
            throw new DomainValidationException("Origen y destino son obligatorios.");
        }
        if (request.pickupLocality().trim().length() < 3
                || request.destination().trim().length() < 3) {
            throw new DomainValidationException("Origen y destino deben tener al menos tres caracteres.");
        }
    }

    private String resolveSchedule(String notes) {
        return notes != null && notes.contains("08:00") ? "08:00 AM" : "03:00 AM";
    }
}
