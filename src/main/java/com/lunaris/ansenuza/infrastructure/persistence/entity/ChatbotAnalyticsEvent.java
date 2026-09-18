package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chatbot_analytics_events",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "dedupe_key"}))
@Getter
@Setter
public class ChatbotAnalyticsEvent implements org.springframework.data.domain.Persistable<UUID> {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @AssignedOrGeneratedUuid
    private UUID id = UUID.randomUUID();
    @Column(name = "session_id", nullable = false) private UUID sessionId;
    @Column(nullable = false, length = 64) private String eventType;
    @Column(nullable = false) private Instant occurredAt;
    @Column(length = 64) private String step;
    @Column(length = 64) private String reasonCode;
    @Column(name = "dedupe_key", nullable = false, length = 128) private String dedupeKey;
    @Transient private boolean newEntity = true;
    @Override public boolean isNew() { return newEntity; }
    @PostLoad @PostPersist void markPersisted() { newEntity = false; }
    @PrePersist void initializeId() { if (id == null) id = UUID.randomUUID(); }
}
