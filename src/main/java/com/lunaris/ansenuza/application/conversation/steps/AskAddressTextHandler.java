package com.lunaris.ansenuza.application.conversation.steps;

import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/** ASK_ADDRESS_TEXT: guarda la dirección de retiro ingresada manualmente y pide el destino. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AskAddressTextHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerRepository passengerRepository;
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
        String normalizedAddress = pickupAddress.trim();
        try {
            passengerRepository.findByPhone(phoneNumber).ifPresent(passenger -> {
                passenger.setAddress(normalizedAddress);
                passenger.setLocality(session.getPickupLocality());
                passengerRepository.saveAndFlush(passenger);
            });

            session.setPickupAddress(normalizedAddress);
            session.setCurrentStep("ASK_DESTINATION");
            conversationSessionRepository.saveAndFlush(session);

            messaging.sendButtons(phoneNumber, "Dirección actualizada",
                    "✅ *Actualizamos tu dirección de retiro:*\n"
                            + normalizedAddress + "\n\n"
                            + "🎯 *¿Hacia dónde viajás en Córdoba?*",
                    List.of(new Button("dest_aeropuerto", "Aeropuerto Cba ✈️"),
                            new Button("dest_capital", "Córdoba Capital 🏢")));
        } catch (RuntimeException exception) {
            log.warn("No se pudo actualizar la dirección del pasajero {}.", phoneNumber, exception);
            session.setCurrentStep("ASK_ADDRESS_TEXT");
            messaging.requestLocation(phoneNumber,
                    "⚠️ No pudimos guardar esa dirección. Enviá nuevamente calle y número, "
                            + "o compartí tu ubicación.");
        }
    }
}
