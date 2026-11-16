package com.lunaris.ansenuza.domain.exception;

public class PassengerNotFoundException extends RuntimeException {
    public PassengerNotFoundException() {
        super("El pasajero no existe.");
    }
}
