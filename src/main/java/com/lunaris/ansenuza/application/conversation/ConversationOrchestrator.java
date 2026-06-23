package com.lunaris.ansenuza.application.conversation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Orquestador de la máquina de estados conversacional del bot de WhatsApp.
 *
 * <p>Centraliza las responsabilidades transversales (carga/creación de sesión, reflejo
 * en el chat en vivo, bypass de bot pausado y detección de saludos) y delega cada paso
 * concreto en el {@link ConversationStepHandler} correspondiente, registrado por su
 * {@link ConversationStepHandler#step()}.
 */
@Service
@Slf4j
public class ConversationOrchestrator {

    private final Map<String, ConversationStepHandler> handlers;
    private final ConversationSessionRepository conversationSessionRepository;
    private final LiveChatPort liveChat;

    public ConversationOrchestrator(List<ConversationStepHandler> handlerList,
            ConversationSessionRepository conversationSessionRepository,
            LiveChatPort liveChat) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(ConversationStepHandler::step, Function.identity()));
        this.conversationSessionRepository = conversationSessionRepository;
        this.liveChat = liveChat;
    }

    public void process(IncomingMessage message) {
        String raw = message.body();
        if (raw == null) {
            return;
        }
        String phoneNumber = message.from();
        String body = raw.trim().toLowerCase();

        ConversationSession session = conversationSessionRepository
                .findByPhoneNumber(phoneNumber).orElseGet(() -> {
                    ConversationSession newSession = ConversationSession.builder()
                            .phoneNumber(phoneNumber).currentStep("START").botPaused(false).build();
                    return conversationSessionRepository.saveAndFlush(newSession);
                });

        // Reflejamos el mensaje del cliente en la sala de chat humana (persistencia + WebSocket)
        liveChat.recordIncomingMessage(phoneNumber, raw.trim());

        // Marcamos actividad en cada mensaje para que el scheduler pueda detectar sesiones abandonadas
        session.setLastInteraction(LocalDateTime.now());
        conversationSessionRepository.saveAndFlush(session);

        if (session.isBotPaused()) {
            log.info("[Bypass] Bot muteado para {}. Derivando mensaje a la sala de chat humana.",
                    phoneNumber);
            return;
        }

        boolean isGreeting = "hola".equals(body) || "buen dia".equals(body)
                || "buenas".equals(body) || "menu".equals(body) || "reinicio".equals(body);

        String currentStep = session.getCurrentStep();
        String effectiveStep =
                (currentStep == null || "START".equals(currentStep) || isGreeting) ? "START"
                        : currentStep;

        ConversationStepHandler handler = handlers.get(effectiveStep);
        if (handler == null) {
            log.warn("[Bot] No hay handler registrado para el paso '{}' (teléfono {}).",
                    effectiveStep, phoneNumber);
            return;
        }

        handler.handle(session, message);
    }
}
