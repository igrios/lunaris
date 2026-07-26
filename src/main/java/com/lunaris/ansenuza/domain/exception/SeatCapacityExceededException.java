package com.lunaris.ansenuza.domain.exception;

public class SeatCapacityExceededException extends DomainValidationException {

    public SeatCapacityExceededException(String message) {
        super(message);
    }
}
