package com.lunaris.ansenuza.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunaris.ansenuza.application.payment.PaymentAuditOutboxPort;
import com.lunaris.ansenuza.application.payment.PaymentConfirmedEvent;
import com.lunaris.ansenuza.application.payment.PaymentDetectedAuditRecord;
import com.lunaris.ansenuza.infrastructure.persistence.entity.PaymentAuditOutboxEntity;
import com.lunaris.ansenuza.infrastructure.persistence.repository.PaymentAuditOutboxJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaPaymentAuditOutboxAdapter implements PaymentAuditOutboxPort {

    private final PaymentAuditOutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    public JpaPaymentAuditOutboxAdapter(
            PaymentAuditOutboxJpaRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void appendAudit(PaymentDetectedAuditRecord record) {
        save(record.eventType(), record.transactionId(), record, record.occurredAt());
    }

    @Override
    public void appendConfirmed(PaymentConfirmedEvent event) {
        save("PAYMENT_CONFIRMED", event.transactionId(), event, event.occurredAt());
    }

    private void save(String eventType, String aggregateId, Object event, java.time.Instant occurredAt) {
        try {
            repository.save(new PaymentAuditOutboxEntity(
                    eventType, aggregateId, objectMapper.writeValueAsString(event), occurredAt));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize payment audit event", exception);
        }
    }
}
