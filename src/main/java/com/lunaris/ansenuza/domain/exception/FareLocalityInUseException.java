package com.lunaris.ansenuza.domain.exception;

public class FareLocalityInUseException extends RuntimeException {
    public FareLocalityInUseException(String localityName) {
        super("No se puede eliminar la tarifa de " + localityName
                + " porque existen reservas activas asociadas.");
    }
}
