package com.lunaris.ansenuza.reservation.application.port.in;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;

public interface CreateReservationUseCase {
    Reservation create(Reservation reservation);
}
