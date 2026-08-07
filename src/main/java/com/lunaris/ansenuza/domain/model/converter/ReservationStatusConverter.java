package com.lunaris.ansenuza.domain.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;
import java.util.Set;

/**
 * Conversor tolerante para el estado histórico de reserva, cuyo contrato público
 * continúa siendo String para no romper integraciones existentes.
 */
@Converter
public class ReservationStatusConverter implements AttributeConverter<String, String> {

    private static final String DEFAULT_STATUS = "PENDING";
    private static final Set<String> KNOWN_STATUSES = Set.of(
            DEFAULT_STATUS,
            "PENDING_PAYMENT",
            "PAYMENT_RECEIVED",
            "RECEIPT_UPLOADED",
            "RESERVED",
            "REJECTED",
            "CONFIRMED",
            "CANCELLED",
            "OPEN_RETURN",
            "PARTIALLY_COMPLETED",
            "COMPLETED");

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return normalize(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return normalize(dbData);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_STATUS;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return KNOWN_STATUSES.contains(normalized) ? normalized : DEFAULT_STATUS;
    }
}
