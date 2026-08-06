package com.lunaris.ansenuza.domain.model;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SpecialTrip(
        Long id,
        String title,
        String description,
        String origin,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        Integer maxPassengers,
        String imageUrl,
        boolean active,
        LocalDateTime createdAt) {

    public SpecialTrip {
        title = required(title, "El título es obligatorio.", 255);
        description = optional(description);
        origin = optional(origin, 100, "El origen no puede superar los 100 caracteres.");
        destination = optional(destination, 100, "El destino no puede superar los 100 caracteres.");
        imageUrl = optional(imageUrl, 500, "La URL de imagen no puede superar los 500 caracteres.");
        if (startDate == null || endDate == null) {
            throw new DomainValidationException("Las fechas de inicio y fin son obligatorias.");
        }
        if (endDate.isBefore(startDate)) {
            throw new DomainValidationException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        if (price == null || price.signum() < 0) {
            throw new DomainValidationException("El precio debe ser mayor o igual a cero.");
        }
        if (maxPassengers == null || maxPassengers < 1) {
            throw new DomainValidationException("La capacidad máxima debe ser mayor a cero.");
        }
    }

    public static SpecialTrip create(String title, String description, String origin, String destination,
            LocalDate startDate, LocalDate endDate, BigDecimal price, Integer maxPassengers,
            String imageUrl, boolean active, LocalDateTime createdAt) {
        return new SpecialTrip(null, title, description, origin, destination, startDate, endDate,
                price, maxPassengers, imageUrl, active, createdAt);
    }

    public SpecialTrip update(String title, String description, String origin, String destination,
            LocalDate startDate, LocalDate endDate, BigDecimal price, Integer maxPassengers,
            String imageUrl, boolean active) {
        return new SpecialTrip(id, title, description, origin, destination, startDate, endDate,
                price, maxPassengers, imageUrl, active, createdAt);
    }

    private static String required(String value, String message, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(message);
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new DomainValidationException("El título no puede superar los " + maxLength + " caracteres.");
        }
        return normalized;
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String optional(String value, int maxLength, String message) {
        String normalized = optional(value);
        if (normalized != null && normalized.length() > maxLength) {
            throw new DomainValidationException(message);
        }
        return normalized;
    }
}
