package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record BankTransferNotification(
        String source,
        String externalNotificationId,
        String transactionId,
        String reservationCode,
        BigDecimal amount,
        String payerName,
        Instant receivedAt) {

    public BankTransferNotification {
        source = requireText(source, "source");
        externalNotificationId = requireText(externalNotificationId, "externalNotificationId");
        transactionId = requireText(transactionId, "transactionId");
        reservationCode = requireText(reservationCode, "reservationCode").toUpperCase();
        amount = Objects.requireNonNull(amount, "amount");
        payerName = requireText(payerName, "payerName");
        receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
