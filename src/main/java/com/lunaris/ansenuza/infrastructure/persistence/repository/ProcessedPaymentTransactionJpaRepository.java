package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.ProcessedPaymentTransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessedPaymentTransactionJpaRepository
        extends JpaRepository<ProcessedPaymentTransactionEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO processed_payment_transactions
                (id, source, external_notification_id, transaction_id, reservation_code,
                 received_amount, payer_name, status, received_at)
            VALUES
                (:id, :source, :externalId, :transactionId, :reservationCode,
                 :amount, :payerName, 'RECEIVED', :receivedAt)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int claim(
            @Param("id") UUID id,
            @Param("source") String source,
            @Param("externalId") String externalId,
            @Param("transactionId") String transactionId,
            @Param("reservationCode") String reservationCode,
            @Param("amount") BigDecimal amount,
            @Param("payerName") String payerName,
            @Param("receivedAt") Instant receivedAt);

    @Modifying
    @Query("""
            UPDATE ProcessedPaymentTransactionEntity transaction
               SET transaction.status = :status,
                   transaction.reservationId = :reservationId,
                   transaction.expectedAmount = :expectedAmount,
                   transaction.detail = :detail,
                   transaction.processedAt = :processedAt
             WHERE transaction.source = :source
               AND transaction.externalNotificationId = :externalId
            """)
    int updateOutcome(
            @Param("source") String source,
            @Param("externalId") String externalId,
            @Param("status") String status,
            @Param("reservationId") UUID reservationId,
            @Param("expectedAmount") BigDecimal expectedAmount,
            @Param("detail") String detail,
            @Param("processedAt") Instant processedAt);
}
