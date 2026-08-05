package com.lunaris.ansenuza.application.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ProcessBankEmailServiceTest {

    @Test
    void auditModeRecordsMatchedTransactionWithoutConfirmingReservation() {
        var ledger = mock(ProcessedTransactionLedgerPort.class);
        var reservations = mock(BankPaymentReservationPort.class);
        var outbox = mock(PaymentAuditOutboxPort.class);
        var notification = notification("MP-123", "MOR-COR-001-IDA", "10500.00");
        UUID reservationId = UUID.randomUUID();
        when(ledger.claim(notification)).thenReturn(true);
        when(reservations.findByReservationCode(notification.reservationCode()))
                .thenReturn(Optional.of(new ReservationPaymentCandidate(
                        reservationId, new BigDecimal("10500.00"))));

        var result = new ProcessBankEmailService(ledger, reservations, outbox, false)
                .process(notification);

        var detected = assertInstanceOf(BankEmailProcessingResult.Detected.class, result);
        assertEquals(reservationId, detected.reservationId());
        assertFalse(detected.autoConfirmed());
        verify(reservations, never()).confirm(any());
        verify(ledger).recordOutcome(
                "MERCADO_PAGO_EMAIL", "message-1", "AUDIT_MATCHED",
                reservationId, new BigDecimal("10500.00"),
                "Audit mode: reservation was not modified");

        ArgumentCaptor<PaymentDetectedAuditRecord> auditCaptor =
                ArgumentCaptor.forClass(PaymentDetectedAuditRecord.class);
        verify(outbox).appendAudit(auditCaptor.capture());
        assertEquals("PAYMENT_DETECTED", auditCaptor.getValue().eventType());
        verify(outbox, never()).appendConfirmed(any());
    }

    @Test
    void duplicateTransactionDoesNotReadOrModifyReservation() {
        var ledger = mock(ProcessedTransactionLedgerPort.class);
        var reservations = mock(BankPaymentReservationPort.class);
        var outbox = mock(PaymentAuditOutboxPort.class);
        var notification = notification("MP-123", "MOR-COR-001-IDA", "10500.00");
        when(ledger.claim(notification)).thenReturn(false);

        var result = new ProcessBankEmailService(ledger, reservations, outbox, false)
                .process(notification);

        assertInstanceOf(BankEmailProcessingResult.Duplicate.class, result);
        verify(reservations, never()).findByReservationCode(any());
        verify(reservations, never()).confirm(any());
        verify(outbox, never()).appendAudit(any());
    }

    @Test
    void amountMismatchIsAuditedWithoutConfirmation() {
        var ledger = mock(ProcessedTransactionLedgerPort.class);
        var reservations = mock(BankPaymentReservationPort.class);
        var outbox = mock(PaymentAuditOutboxPort.class);
        var notification = notification("MP-124", "MOR-COR-001-IDA", "9500.00");
        when(ledger.claim(notification)).thenReturn(true);
        when(reservations.findByReservationCode(notification.reservationCode()))
                .thenReturn(Optional.of(new ReservationPaymentCandidate(
                        UUID.randomUUID(), new BigDecimal("10500.00"))));

        var result = new ProcessBankEmailService(ledger, reservations, outbox, false)
                .process(notification);

        assertInstanceOf(BankEmailProcessingResult.AmountMismatch.class, result);
        verify(reservations, never()).confirm(any());
        verify(outbox).appendAudit(any(PaymentDetectedAuditRecord.class));
    }

    @Test
    void enabledAutoConfirmationUsesExistingConfirmationBoundaryAndEmitsEvent() {
        var ledger = mock(ProcessedTransactionLedgerPort.class);
        var reservations = mock(BankPaymentReservationPort.class);
        var outbox = mock(PaymentAuditOutboxPort.class);
        var notification = notification("MP-125", "MOR-COR-001-IDA", "10500.00");
        UUID reservationId = UUID.randomUUID();
        when(ledger.claim(notification)).thenReturn(true);
        when(reservations.findByReservationCode(notification.reservationCode()))
                .thenReturn(Optional.of(new ReservationPaymentCandidate(
                        reservationId, new BigDecimal("10500.00"))));

        var result = new ProcessBankEmailService(ledger, reservations, outbox, true)
                .process(notification);

        var detected = assertInstanceOf(BankEmailProcessingResult.Detected.class, result);
        assertEquals(true, detected.autoConfirmed());
        verify(reservations).confirm("MOR-COR-001-IDA");
        verify(outbox).appendConfirmed(any(PaymentConfirmedEvent.class));
    }

    private BankTransferNotification notification(
            String transactionId, String reservationCode, String amount) {
        return new BankTransferNotification(
                "MERCADO_PAGO_EMAIL", "message-1", transactionId,
                reservationCode, new BigDecimal(amount), "Ada Lovelace", Instant.now());
    }
}
