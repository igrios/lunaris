package com.lunaris.ansenuza.application.payment;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessBankEmailService implements ProcessBankEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessBankEmailService.class);

    private final ProcessedTransactionLedgerPort ledger;
    private final BankPaymentReservationPort reservations;
    private final PaymentAuditOutboxPort outbox;
    private final boolean autoConfirmEnabled;

    public ProcessBankEmailService(
            ProcessedTransactionLedgerPort ledger,
            BankPaymentReservationPort reservations,
            PaymentAuditOutboxPort outbox,
            @Value("${app.payment.auto-confirm-enabled:false}") boolean autoConfirmEnabled) {
        this.ledger = ledger;
        this.reservations = reservations;
        this.outbox = outbox;
        this.autoConfirmEnabled = autoConfirmEnabled;
    }

    @Override
    @Transactional
    public BankEmailProcessingResult process(BankTransferNotification notification) {
        if (!ledger.claim(notification)) {
            log.info("Ignoring duplicate payment transaction {}", notification.transactionId());
            return new BankEmailProcessingResult.Duplicate(notification.transactionId());
        }

        var candidate = reservations.findByReservationCode(notification.reservationCode());
        if (candidate.isEmpty()) {
            recordAudit(notification, null, null, "RESERVATION_NOT_FOUND");
            ledger.recordOutcome(notification.source(), notification.externalNotificationId(),
                    "RESERVATION_NOT_FOUND", null, null, "Reservation code was not found");
            return new BankEmailProcessingResult.ReservationNotFound(notification.reservationCode());
        }

        var paymentCandidate = candidate.get();
        if (paymentCandidate.expectedTotal().compareTo(notification.amount()) != 0) {
            recordAudit(notification, paymentCandidate, paymentCandidate.expectedTotal(), "AMOUNT_MISMATCH");
            ledger.recordOutcome(notification.source(), notification.externalNotificationId(),
                    "AMOUNT_MISMATCH", paymentCandidate.reservationId(),
                    paymentCandidate.expectedTotal(), "Transferred amount differs from expected total");
            return new BankEmailProcessingResult.AmountMismatch(
                    paymentCandidate.expectedTotal(), notification.amount());
        }

        recordAudit(notification, paymentCandidate, paymentCandidate.expectedTotal(), "PAYMENT_DETECTED");
        if (!autoConfirmEnabled) {
            ledger.recordOutcome(notification.source(), notification.externalNotificationId(),
                    "AUDIT_MATCHED", paymentCandidate.reservationId(),
                    paymentCandidate.expectedTotal(), "Audit mode: reservation was not modified");
            log.info("AUDIT payment match transaction={} reservation={} amount={}",
                    notification.transactionId(), notification.reservationCode(), notification.amount());
            return new BankEmailProcessingResult.Detected(paymentCandidate.reservationId(), false);
        }

        reservations.confirm(notification.reservationCode());
        Instant occurredAt = Instant.now();
        outbox.appendConfirmed(new PaymentConfirmedEvent(
                notification.transactionId(), paymentCandidate.reservationId(),
                notification.amount(), occurredAt));
        ledger.recordOutcome(notification.source(), notification.externalNotificationId(),
                "AUTO_CONFIRMED", paymentCandidate.reservationId(),
                paymentCandidate.expectedTotal(), "Reservation automatically confirmed");
        return new BankEmailProcessingResult.Detected(paymentCandidate.reservationId(), true);
    }

    private void recordAudit(
            BankTransferNotification notification,
            ReservationPaymentCandidate candidate,
            java.math.BigDecimal expectedAmount,
            String eventType) {
        outbox.appendAudit(new PaymentDetectedAuditRecord(
                eventType,
                notification.transactionId(),
                notification.reservationCode(),
                candidate == null ? null : candidate.reservationId(),
                notification.amount(),
                expectedAmount,
                notification.payerName(),
                notification.receivedAt()));
    }
}
