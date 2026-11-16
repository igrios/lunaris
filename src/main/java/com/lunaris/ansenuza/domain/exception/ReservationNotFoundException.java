package com.lunaris.ansenuza.domain.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException() { super("Reserva no encontrada."); }
}
