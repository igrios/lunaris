package com.lunaris.ansenuza.domain.exception;

public class ReservationAlreadyCompletedException extends DomainValidationException {

    public ReservationAlreadyCompletedException() {
        super("La reserva ya fue completada y no admite cancelaciones, reintegros ni modificaciones.");
    }
}
