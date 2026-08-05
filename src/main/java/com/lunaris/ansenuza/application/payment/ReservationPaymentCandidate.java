package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationPaymentCandidate(UUID reservationId, BigDecimal expectedTotal) {}
