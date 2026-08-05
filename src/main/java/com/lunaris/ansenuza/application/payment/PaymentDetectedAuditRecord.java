package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentDetectedAuditRecord(
        String eventType,
        String transactionId,
        String reservationCode,
        UUID reservationId,
        BigDecimal receivedAmount,
        BigDecimal expectedAmount,
        String payerName,
        Instant occurredAt) {}
