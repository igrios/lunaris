package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.ChatbotAnalyticsEvent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotAnalyticsEventRepository extends JpaRepository<ChatbotAnalyticsEvent, UUID> {
    boolean existsBySessionIdAndDedupeKey(UUID sessionId, String dedupeKey);
    Optional<ChatbotAnalyticsEvent> findFirstByDedupeKeyOrderByOccurredAtAsc(String dedupeKey);
    void deleteBySessionId(UUID sessionId);
}

