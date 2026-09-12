package com.lunaris.ansenuza.infrastructure.chat;

import java.time.LocalDateTime;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.application.conversation.BotRoute;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;

/**
 * Adaptador de salida que implementa {@link LiveChatPort}: persiste el mensaje entrante
 * del cliente y lo emite por WebSocket al tópico de la sala de chat del operador.
 */
@Component
public class WebSocketLiveChatAdapter implements LiveChatPort {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationSessionRepository sessionRepository;
    private final LocalityRepository localityRepository;

    public WebSocketLiveChatAdapter(ChatMessageRepository chatMessageRepository,
            SimpMessagingTemplate messagingTemplate,
            ConversationSessionRepository sessionRepository,
            LocalityRepository localityRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.sessionRepository = sessionRepository;
        this.localityRepository = localityRepository;
    }

    @Override
    public void recordIncomingMessage(String phoneNumber, String text) {
        String readableText = readableText(phoneNumber, text);
        ChatMessage msgCliente = chatMessageRepository.saveAndFlush(ChatMessage.builder()
                .phoneNumber(phoneNumber)
                .messageText(readableText)
                .fromOperator(false)
                .timestamp(com.lunaris.ansenuza.shared.ArgentinaTime.now())
                .build());

        messagingTemplate.convertAndSend("/topic/messages/" + phoneNumber, msgCliente);
    }

    String readableText(String phoneNumber, String text) {
        if (text == null) return null;
        return switch (text.trim().toLowerCase()) {
            case "no_cancel" -> "Mantener reserva (No cancelar)";
            case "confirm_cancel" -> "Confirmar cancelación";
            default -> mapLocalitySelection(phoneNumber, text);
        };
    }

    private String mapLocalitySelection(String phoneNumber, String text) {
        ConversationSession session = sessionRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if (session == null || !text.trim().matches("\\d+")
                || !("ASK_LOCALITY".equals(session.getCurrentStep())
                        || "ASK_TOWN_DESTINATION".equals(session.getCurrentStep()))) {
            return text;
        }
        int option;
        try { option = Integer.parseInt(text.trim()); } catch (NumberFormatException ex) { return text; }
        var options = "ASK_LOCALITY".equals(session.getCurrentStep())
                ? localityRepository.findAllWithActiveFare().stream()
                        .filter(locality -> !BotRoute.fromCordoba(locality.getName())).toList()
                : BotRoute.destinations(localityRepository);
        if ("ASK_LOCALITY".equals(session.getCurrentStep()) && option == options.size() + 1) {
            return option + " - Córdoba";
        }
        if (option < 1 || option > options.size()) return text;
        return option + " - " + options.get(option - 1).getName();
    }
}
