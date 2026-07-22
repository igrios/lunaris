package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // Trae el historial de mensajes de un teléfono ordenado del más viejo al más nuevo
    List<ChatMessage> findByPhoneNumberOrderByTimestampAsc(String phoneNumber);

    Optional<ChatMessage> findFirstByPhoneNumberAndFromOperatorFalseOrderByTimestampDesc(String phoneNumber);
}
