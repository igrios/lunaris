package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.*;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true")
public class InterurbanConfirmationHandler implements ConversationStepHandler {
    private final ConversationSessionRepository sessions;
    private final MessagingPort messaging;

    @Override
    public String step() { return "ASK_INTERURBAN_CONFIRMATION"; }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String input = message.body().trim();
        if ("interurban_reserve".equals(input)) {
            session.setBotPaused(true);
            session.setCurrentStep("MAIN_MENU");
            sessions.saveAndFlush(session);
            messaging.sendText(session.getPhoneNumber(), "Enviá fecha, cantidad de pasajeros y direcciones de retiro y llegada. Un operador coordinará tu reserva interurbana.");
        } else if ("interurban_cancel".equals(input) || "0".equals(input)) {
            session.setCurrentStep("MAIN_MENU");
            sessions.saveAndFlush(session);
            messaging.sendText(session.getPhoneNumber(), "1) Reservar un viaje\n2) Ver precios\n3) Operador");
        } else {
            messaging.sendText(session.getPhoneNumber(), "Elegí Coordinar reserva o Volver al menú.");
        }
    }
}
