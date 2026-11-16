package com.lunaris.ansenuza.domain.exception;

public class ReservationUpdateConflictException extends RuntimeException {
    public ReservationUpdateConflictException(String message) { super(message); }
}
