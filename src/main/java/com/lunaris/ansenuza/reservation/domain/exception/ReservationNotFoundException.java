package com.lunaris.ansenuza.reservation.domain.exception;

import java.util.UUID;

public final class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(UUID reservationId) {
        super("Reserva no encontrada: " + reservationId);
    }
}
