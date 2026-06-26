package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import org.springframework.stereotype.Service;
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

    public Reservation execute(CreateReservationRequest request) {

        Passenger passenger =
                passengerRepository.findById(request.passengerId())
                        .orElseThrow();

        // 🌟 Lógica del desplegable de asientos (Captura directa)
        Integer safePassengerCount = (request.passengerCount() == null || request.passengerCount() <= 0) ? 1 : request.passengerCount();
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
                .build();

        List<Reservation> savedReservations = reservationService.saveReservationFlow(reservation);
        
        return savedReservations.get(0);
    }
}
