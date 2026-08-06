package com.lunaris.ansenuza.domain.exception;

public class SpecialTripNotFoundException extends RuntimeException {
    public SpecialTripNotFoundException(Long id) {
        super("No existe el viaje especial con id " + id + ".");
    }
}
