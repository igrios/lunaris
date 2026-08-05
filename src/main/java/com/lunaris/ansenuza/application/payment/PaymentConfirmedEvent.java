package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentConfirmedEvent(
        String transactionId,
        UUID reservationId,
        BigDecimal amount,
        Instant occurredAt) {}
