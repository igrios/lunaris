package com.lunaris.ansenuza.infrastructure.persistence;

import com.lunaris.ansenuza.application.payment.BankTransferNotification;
import com.lunaris.ansenuza.application.payment.ProcessedTransactionLedgerPort;
import com.lunaris.ansenuza.infrastructure.persistence.repository.ProcessedPaymentTransactionJpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProcessedTransactionLedgerAdapter implements ProcessedTransactionLedgerPort {

    private final ProcessedPaymentTransactionJpaRepository repository;

    public ProcessedTransactionLedgerAdapter(ProcessedPaymentTransactionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean claim(BankTransferNotification notification) {
        return repository.claim(
                UUID.randomUUID(),
                notification.source(),
                notification.externalNotificationId(),
                notification.transactionId(),
                notification.reservationCode(),
                notification.amount(),
                notification.payerName(),
                notification.receivedAt()) == 1;
    }

    @Override
    public void recordOutcome(
            String source,
            String externalNotificationId,
            String status,
            UUID reservationId,
            BigDecimal expectedAmount,
            String detail) {
        repository.updateOutcome(source, externalNotificationId, status, reservationId,
                expectedAmount, detail, Instant.now());
    }
}
