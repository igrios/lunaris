package com.lunaris.ansenuza.domain.exception;

public class SameDayBookingClosedException extends DomainValidationException {

    public static final String MESSAGE = "Lo sentimos, las reservas para el día de hoy ya se "
            + "encuentran cerradas por motivos logísticos. Te invitamos a seleccionar una fecha "
            + "a partir de mañana.";

    public SameDayBookingClosedException() {
        super(MESSAGE);
    }
}
