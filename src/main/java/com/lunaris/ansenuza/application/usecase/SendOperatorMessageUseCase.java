package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendOperatorMessageUseCase {
    private final ConversationSessionRepository sessions;
    private final ChatMessageRepository messages;
    private final TakeOverConversationUseCase takeOver;
    private final AssistedChatService assistedChat;
    private final ApplicationEventPublisher events;
    private final WhatsAppService whatsApp;

    @Transactional
    public ChatMessage execute(String phoneNumber, String text) {
        if (text == null || text.isBlank()) {
            throw new DomainValidationException("El mensaje no puede estar vacío.");
        }
        String phone = phoneNumber == null ? "" : phoneNumber.replaceAll("[\\s()+-]", "");
        if (!phone.matches("[1-9][0-9]{7,14}")) {
            throw new DomainValidationException("El teléfono debe incluir país y área completos.");
        }
        if (sessions.findByPhoneNumber(phone).isEmpty()) {
            takeOver.execute(phone);
        }
        if (!assistedChat.canSend(phone)) {
            throw new DomainValidationException(
                    "La ventana de WhatsApp venció. Reenviá la plantilla contacto_pasajero.");
        }
        ChatMessage saved = messages.saveAndFlush(ChatMessage.builder()
                .phoneNumber(phone)
                .messageText(text.trim())
                .fromOperator(true)
                .timestamp(ArgentinaTime.now())
                .build());
        events.publishEvent(new OperatorMessageRegistered(phone, saved.getMessageText()));
        return saved;
    }

    /** La API externa solo se invoca si sesión e historial se confirmaron. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deliver(OperatorMessageRegistered event) {
        try {
            whatsApp.sendMessage(event.phone(), event.text());
        } catch (RuntimeException exception) {
            log.error("Error enviando el mensaje de operador a WhatsApp.", exception);
        }
    }

    public record OperatorMessageRegistered(String phone, String text) {}
}
