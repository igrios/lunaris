package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.ChatbotAnalyticsSession;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatbotAnalyticsSessionRepository extends JpaRepository<ChatbotAnalyticsSession, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatbotAnalyticsSession> findFirstByEnvironmentAndSourceAndSubjectKeyVersionAndSubjectKeyAndStatus(
            String environment, String source, short version, String key, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ChatbotAnalyticsSession s where s.id = :id")
    Optional<ChatbotAnalyticsSession> findForUpdate(@Param("id") UUID id);

    @Query("select s.id from ChatbotAnalyticsSession s where s.status = 'ACTIVE' and s.expiresAt <= :now order by s.expiresAt")
    List<UUID> findExpiredIds(@Param("now") Instant now, Pageable page);

    @Query("select s.id from ChatbotAnalyticsSession s where s.endedAt < :cutoff order by s.endedAt")
    List<UUID> findRetainedIds(@Param("cutoff") Instant cutoff, Pageable page);
}

