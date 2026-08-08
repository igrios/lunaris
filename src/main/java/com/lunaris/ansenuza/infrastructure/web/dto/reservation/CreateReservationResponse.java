package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import com.lunaris.ansenuza.domain.model.Reservation;
import java.util.UUID;

public record CreateReservationResponse(
        boolean success,
        String reservationCode,
        String bookingGroupCode,
        UUID id) {

    public static CreateReservationResponse from(Reservation reservation) {
        return new CreateReservationResponse(
                true,
                reservation.getReservationCode(),
                reservation.getBookingGroupCode(),
                reservation.getId());
    }
}
