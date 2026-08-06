package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.math.BigDecimal;

final class FareLocalityValidation {
    private FareLocalityValidation() {
    }

    static String localityName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("El nombre de la localidad es obligatorio.");
        }
        String normalized = name.trim();
        if (normalized.length() > 100) {
            throw new DomainValidationException("El nombre de la localidad no puede superar los 100 caracteres.");
        }
        return normalized;
    }

    static void amount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new DomainValidationException("La tarifa debe ser mayor a cero.");
        }
    }

    static void nonNegative(Integer value, String field) {
        if (value != null && value < 0) {
            throw new DomainValidationException(field + " no pueden ser negativos.");
        }
    }
}
