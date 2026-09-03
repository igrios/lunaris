package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "whatsapp_webhook_inbox")
public class WhatsAppWebhookInboxEntity {

    @Id
    @Column(name = "message_id", length = 255, nullable = false)
    private String messageId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected WhatsAppWebhookInboxEntity() {
    }
}
