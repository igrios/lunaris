package com.lunaris.ansenuza.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.ConversationSession;

public interface ConversationSessionRepository
        extends JpaRepository<ConversationSession, Long> {

    Optional<ConversationSession>
    findByPhoneNumber(String phoneNumber);

    // 🧹 Sesiones del bot abandonadas: sin actividad reciente y que NO estén en manos de un operador.
    List<ConversationSession>
    findByBotPausedFalseAndLastInteractionBefore(LocalDateTime cutoff);
}