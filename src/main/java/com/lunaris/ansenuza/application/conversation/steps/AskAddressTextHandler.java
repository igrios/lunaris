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

/** ASK_ADDRESS_TEXT: guarda la dirección de retiro ingresada manualmente y pide el destino. */
@Component
@RequiredArgsConstructor
public class AskAddressTextHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_ADDRESS_TEXT";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String pickupAddress = message.pickupAddress();
        if (pickupAddress == null || pickupAddress.isBlank()) {
            messaging.requestLocation(phoneNumber,
                    "🏠 Enviá calle y número, o tocá el botón para compartir tu ubicación.");
            return;
        }
        session.setPickupAddress(pickupAddress);
        session.setCurrentStep("ASK_DESTINATION");
        conversationSessionRepository.saveAndFlush(session);

        messaging.sendButtons(phoneNumber, "Destino en Córdoba",
                "🎯 *¿Hacia dónde viajás en Córdoba?*",
                List.of(new Button("dest_aeropuerto", "Aeropuerto Cba ✈️"),
                        new Button("dest_capital", "Córdoba Capital 🏢")));
    }
}
