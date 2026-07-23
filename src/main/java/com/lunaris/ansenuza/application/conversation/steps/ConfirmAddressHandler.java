package com.lunaris.ansenuza.application.conversation.steps;

import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** CONFIRM_ADDRESS_BUTTONS: confirma el domicilio habitual detectado o pide uno nuevo. */
@Component
@RequiredArgsConstructor
public class ConfirmAddressHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "CONFIRM_ADDRESS_BUTTONS";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("addr_yes".equals(body)) {
            session.setCurrentStep("ASK_DESTINATION");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendButtons(phoneNumber, "Destino en Córdoba",
                    "🎯 *¿Hacia dónde viajás en Córdoba?*",
                    List.of(new Button("dest_aeropuerto", "Aeropuerto Cba ✈️"),
                            new Button("dest_capital", "Córdoba Capital 🏢")));
            return;
        }
        if ("addr_no".equals(body)) {
            session.setCurrentStep("ASK_ADDRESS_TEXT");
            conversationSessionRepository.saveAndFlush(session);
            messaging.requestLocation(phoneNumber,
                    "🏠 Escribí la nueva calle y número para el retiro, o tocá el botón para compartir tu ubicación.");
            return;
        }
    }
}
