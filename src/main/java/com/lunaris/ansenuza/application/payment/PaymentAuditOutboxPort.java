package com.lunaris.ansenuza.application.payment;

public interface PaymentAuditOutboxPort {
    void appendAudit(PaymentDetectedAuditRecord record);

    void appendConfirmed(PaymentConfirmedEvent event);
}
