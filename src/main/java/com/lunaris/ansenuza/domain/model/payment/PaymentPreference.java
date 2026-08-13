package com.lunaris.ansenuza.domain.model.payment;

import java.math.BigDecimal;

public record PaymentPreference(
        String id,
        String paymentUrl,
        String externalReference,
        BigDecimal baseAmount,
        BigDecimal finalAmount) {
}
