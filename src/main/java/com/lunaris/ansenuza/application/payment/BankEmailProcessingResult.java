package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.util.UUID;

public sealed interface BankEmailProcessingResult {
    record Detected(UUID reservationId, boolean autoConfirmed) implements BankEmailProcessingResult {}
    record Duplicate(String transactionId) implements BankEmailProcessingResult {}
    record ReservationNotFound(String reservationCode) implements BankEmailProcessingResult {}
    record AmountMismatch(BigDecimal expected, BigDecimal received) implements BankEmailProcessingResult {}
}
