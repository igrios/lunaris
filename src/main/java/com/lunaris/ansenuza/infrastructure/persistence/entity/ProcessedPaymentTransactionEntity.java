package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_payment_transactions")
public class ProcessedPaymentTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 40)
    private String source;

    @Column(name = "external_notification_id", nullable = false, length = 255)
    private String externalNotificationId;

    @Column(name = "transaction_id", nullable = false, length = 120)
    private String transactionId;

    @Column(name = "reservation_code", nullable = false, length = 40)
    private String reservationCode;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "received_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal receivedAmount;

    @Column(name = "expected_amount", precision = 14, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "payer_name", nullable = false, length = 180)
    private String payerName;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(length = 500)
    private String detail;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
