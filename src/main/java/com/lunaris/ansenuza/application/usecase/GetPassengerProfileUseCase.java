package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPassengerProfileUseCase {

    private final PassengerRepository passengerRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public PassengerProfile execute(String phone) {
        var passenger = passengerRepository.findByPhone(phone)
                .orElseThrow(() -> new DomainValidationException("El pasajero autenticado no existe."));
        List<ReservationHistory> history =
                reservationRepository.findByPassengerOrderByTravelDateAsc(passenger).stream()
                        .map(reservation -> new ReservationHistory(
                                reservation.getId(),
                                reservation.getReservationCode(),
                                reservation.getTravelDate(),
                                reservation.getPickupLocality(),
                                reservation.getDestination(),
                                reservation.getDepartureSchedule(),
                                reservation.getStatus(),
                                reservation.getAmount()))
                        .toList();
        return new PassengerProfile(
                passenger.getId(),
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getPhone(),
                passenger.getAddress(),
                passenger.getLocality(),
                passenger.getCurrentBalance() != null
                        ? passenger.getCurrentBalance() : BigDecimal.ZERO,
                history);
    }

    public record PassengerProfile(
            UUID id,
            String firstName,
            String lastName,
            String phone,
            String address,
            String locality,
            @JsonProperty("current_balance") BigDecimal currentBalance,
            List<ReservationHistory> reservations) {
    }

    public record ReservationHistory(
            UUID id,
            String reservationCode,
            LocalDate travelDate,
            String pickupLocality,
            String destination,
            String departureSchedule,
            String status,
            BigDecimal amount) {
    }
}
