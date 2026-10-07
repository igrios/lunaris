package com.lunaris.ansenuza.domain.exception;

import java.util.UUID;

public class AgendaTripNotFoundException extends RuntimeException {
    public AgendaTripNotFoundException(UUID id) {
        super("Viaje de agenda no encontrado: " + id);
    }
}
