package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chatbot_analytics_sessions")
@Getter
@Setter
public class ChatbotAnalyticsSession implements org.springframework.data.domain.Persistable<UUID> {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @AssignedOrGeneratedUuid
    private UUID id = UUID.randomUUID();
    @Column(nullable = false, length = 64) private String subjectKey;
    @Column(nullable = false) private short subjectKeyVersion;
    @Column(nullable = false, length = 20) private String environment;
    @Column(nullable = false, length = 20) private String source;
    @Column(name = "is_test", nullable = false) private boolean test;
    @Column(nullable = false, length = 20) private String audience = "UNKNOWN";
    @Column(nullable = false) private Instant startedAt;
    @Column(nullable = false) private Instant lastInteractionAt;
    @Column(nullable = false) private Instant expiresAt;
    private Instant bookingStartedAt;
    private Instant completedAt;
    private Instant endedAt;
    @Column(length = 64) private String currentStep;
    @Column(length = 64) private String lastMilestone;
    @Column(nullable = false, length = 24) private String status = "ACTIVE";
    @Column(length = 64) private String endReason;
    @Column(length = 40) private String bookingGroupCode;
    private UUID reservationId;
    @Transient private boolean newEntity = true;
    // An assigned UUID does not imply that the row already exists.
    @Override public boolean isNew() { return newEntity; }
    @PostLoad @PostPersist void markPersisted() { newEntity = false; }
    @PrePersist void initializeId() { if (id == null) id = UUID.randomUUID(); }
}
