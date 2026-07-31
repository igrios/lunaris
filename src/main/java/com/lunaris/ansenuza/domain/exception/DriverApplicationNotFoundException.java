package com.lunaris.ansenuza.domain.exception;

import java.util.UUID;

public class DriverApplicationNotFoundException extends RuntimeException {

    public DriverApplicationNotFoundException(UUID id) {
        super("No se encontró la solicitud de chofer " + id + ".");
    }
}
