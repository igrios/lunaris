package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateManualReservationUseCase {
    private final PassengerRepository passengers;
    private final ReservationService reservations;

    @Transactional
    public List<Reservation> execute(Reservation reservation, String returnSchedule) {
        Passenger input = reservation.getPassenger();
        if (input == null || input.getFirstName() == null || input.getFirstName().isBlank()
                || input.getLastName() == null || input.getLastName().isBlank()) {
            throw new DomainValidationException("El nombre y apellido del pasajero son obligatorios.");
        }
        String phone = PhoneUtils.normalizeArgentinePhone(input.getPhone());
        reservations.validateManualReservation(reservation);
        Passenger passenger = passengers.findFirstByPhone(phone).orElseGet(Passenger::new);
        passenger.setPhone(phone);
        passenger.setFirstName(input.getFirstName().trim());
        passenger.setLastName(input.getLastName().trim());
        if (input.getCuil() != null && !input.getCuil().isBlank()) passenger.setCuil(input.getCuil().trim());
        reservation.setPassenger(passengers.saveAndFlush(passenger));
        return reservations.saveManualReservationFlow(reservation, returnSchedule);
    }
}
