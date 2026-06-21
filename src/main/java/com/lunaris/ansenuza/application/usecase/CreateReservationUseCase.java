package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.FareRepository; // 🌟 Sumamos el repo de tarifas
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateReservationUseCase {

    private final ReservationService reservationService;
    private final PassengerRepository passengerRepository;
    private final FareRepository fareRepository; // 🌟 Inyectamos el repositorio de precios

    public Reservation execute(CreateReservationRequest request) {

        Passenger passenger =
                passengerRepository.findById(request.passengerId())
                        .orElseThrow();

        // 🌟 Lógica del desplegable de asientos (Captura directa)
        Integer safePassengerCount = (request.passengerCount() == null || request.passengerCount() <= 0) ? 1 : request.passengerCount();
        Boolean safePaymentVerified = Boolean.TRUE.equals(request.paymentVerified());
        String initialStatus = safePaymentVerified ? "CONFIRMED" : "PENDING_PAYMENT";

        // 🌟 CÁLCULO DE PRECIO RADIAL SIMÉTRICO (Zona ⇄ Córdoba)
        // Detectamos cuál de los dos campos contiene el pueblo de la zona (el que no sea Córdoba)
        String zoneLocality = request.pickupLocality().toLowerCase().contains("córdoba") 
            ? request.destination() 
            : request.pickupLocality();

        // Buscamos el precio correspondiente en la base de datos de manera automática
        BigDecimal computedAmount = fareRepository.findByLocalityNameIgnoreCase(zoneLocality)
            .map(Fare::getAmount)
            .orElse(BigDecimal.ZERO); // Ponemos 0 si pasa algo raro o no está tasada

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