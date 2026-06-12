package com.lunaris.ansenuza.application.usecase;

import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final PassengerRepository passengerRepository;

    public Reservation execute(CreateReservationRequest request) {

        Passenger passenger =
                passengerRepository.findById(request.passengerId())
                        .orElseThrow();

       Reservation reservation = Reservation.builder()
        .passenger(passenger)
        .travelDate(request.travelDate())
        .pickupLocality(request.pickupLocality())
        .pickupAddress(request.pickupAddress())
        .destination(request.destination())
        .roundTrip(request.roundTrip())
        .returnDate(request.returnDate())
        .paymentVerified(request.paymentVerified())
        .notes(request.notes())
        .build();

        return reservationRepository.save(reservation);
    }
    
}
