package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.WhatsAppWebhookInboxEntity;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WhatsAppWebhookInboxRepository
        extends JpaRepository<WhatsAppWebhookInboxEntity, String> {

    @Modifying
    @Query(value = """
            INSERT INTO whatsapp_webhook_inbox (message_id, received_at)
            VALUES (:messageId, :receivedAt)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int claim(@Param("messageId") String messageId, @Param("receivedAt") Instant receivedAt);
}
