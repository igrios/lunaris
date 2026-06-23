package com.lunaris.ansenuza.infrastructure.chat;

import java.time.LocalDateTime;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link LiveChatPort}: persiste el mensaje entrante
 * del cliente y lo emite por WebSocket al tópico de la sala de chat del operador.
 */
@Component
@RequiredArgsConstructor
public class WebSocketLiveChatAdapter implements LiveChatPort {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void recordIncomingMessage(String phoneNumber, String text) {
        ChatMessage msgCliente = chatMessageRepository.saveAndFlush(ChatMessage.builder()
                .phoneNumber(phoneNumber)
                .messageText(text)
                .fromOperator(false)
                .timestamp(LocalDateTime.now())
                .build());

        messagingTemplate.convertAndSend("/topic/messages/" + phoneNumber, msgCliente);
    }
}
