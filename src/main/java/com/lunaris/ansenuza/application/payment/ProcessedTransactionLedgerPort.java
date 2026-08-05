package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProcessedTransactionLedgerPort {
    boolean claim(BankTransferNotification notification);

    void recordOutcome(
            String source,
            String externalNotificationId,
            String status,
            UUID reservationId,
            BigDecimal expectedAmount,
            String detail);
}
