package com.lunaris.ansenuza.reservation.domain.model;

/** Estados persistidos por los flujos históricos y actuales de reservas. */
public enum ReservationStatus {
    PENDING,
    PENDING_PAYMENT,
    PAYMENT_RECEIVED,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    REJECTED;

    public static ReservationStatus fromPersistenceValue(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
