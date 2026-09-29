package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;

public interface PaymentQueryPort {
    record Payment(String id, String status, String externalReference, BigDecimal amount,
            String currency, String collectorId, boolean liveMode, BigDecimal refundedAmount) {}
    Payment getPayment(String paymentId);
}
