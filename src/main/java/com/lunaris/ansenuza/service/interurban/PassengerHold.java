package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.util.UUID;

/** Datos ya cotizados por el caso de uso de booking; un registro por pasajero. */
public record PassengerHold(UUID bookingId, UUID fareId, String name, String phone,
        String pickupAddress, String dropoffAddress, BigDecimal fare, BigDecimal commission) {
    public PassengerHold {
        if (bookingId == null || fareId == null || invalid(name, 150) || invalid(phone, 30)
                || invalid(pickupAddress, 255) || invalid(dropoffAddress, 255)
                || invalidMoney(fare) || invalidMoney(commission) || commission.compareTo(fare) > 0) {
            throw new InvalidHoldException("Datos de pasajero o cotización inválidos.");
        }
    }

    private static boolean invalid(String value, int max) {
        return value == null || value.isBlank() || value.length() > max;
    }

    private static boolean invalidMoney(BigDecimal value) {
        return value == null || value.signum() < 0 || value.stripTrailingZeros().scale() > 2
                || value.compareTo(new BigDecimal("9999999999.99")) > 0;
    }
}
