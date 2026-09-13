package com.lunaris.ansenuza.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.ConversationSession;

public interface ConversationSessionRepository
        extends JpaRepository<ConversationSession, Long> {

    interface MonitorRow {
        Long getId();
        String getPhoneNumber();
        String getPassengerName();
        String getCurrentStep();
        boolean getBotPaused();
        LocalDateTime getLastInteraction();
        String getLastMessage();
    }

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT s.id AS id, s.phone_number AS phoneNumber,
               COALESCE(NULLIF(TRIM(CONCAT(CONCAT(p.first_name, ' '), p.last_name)), ''),
                        s.passenger_name, 'Cliente Anónimo') AS passengerName,
               s.current_step AS currentStep, COALESCE(s.bot_paused, false) AS botPaused,
               s.last_interaction AS lastInteraction, m.message_text AS lastMessage
        FROM conversation_sessions s
        LEFT JOIN (SELECT p.*, ROW_NUMBER() OVER (PARTITION BY phone ORDER BY id) AS rn
                   FROM passengers p) p ON p.phone = s.phone_number AND p.rn = 1
        LEFT JOIN (SELECT m.*, ROW_NUMBER() OVER (
                       PARTITION BY phone_number ORDER BY timestamp DESC, id DESC) AS rn
                   FROM chat_messages m) m ON m.phone_number = s.phone_number AND m.rn = 1
        ORDER BY COALESCE(m.timestamp, s.last_interaction) DESC NULLS LAST, s.id DESC
        """, nativeQuery = true)
    List<MonitorRow> findMonitorRows();

    Optional<ConversationSession>
    findByPhoneNumber(String phoneNumber);

    // 🧹 Sesiones del bot abandonadas: sin actividad reciente y que NO estén en manos de un operador.
    List<ConversationSession>
    findByBotPausedFalseAndLastInteractionBefore(LocalDateTime cutoff);
}