package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.ConversationSession;

public interface ConversationSessionRepository
        extends JpaRepository<ConversationSession, Long> {

    Optional<ConversationSession>
    findByPhoneNumber(String phoneNumber);
}