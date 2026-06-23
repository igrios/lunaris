package com.lunaris.ansenuza.application.conversation.steps;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;

/** START / saludo: muestra el menú principal y transiciona a MAIN_MENU. */
@Component
@RequiredArgsConstructor
public class StartHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerRepository passengerRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "START";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        session.setCurrentStep("MAIN_MENU");
        session.setLastInteraction(LocalDateTime.now());
        conversationSessionRepository.saveAndFlush(session);

        Optional<Passenger> existingPassenger = passengerRepository.findByPhone(phoneNumber);
        String saludo = existingPassenger.isPresent()
                ? "¡Hola de nuevo, *" + existingPassenger.get().getFirstName() + "*! 👋\n"
                : "¡Bienvenido a Lunaris Ansenuza! 🚐\n";

        String menuPrincipal = saludo + """
                ¿En qué te podemos ayudar hoy? Por favor, elegí una opción enviando el número:

                1️⃣ *Reservar un viaje* (Flujo rápido)
                2️⃣ *Ver Precios y Cotizar* 💸
                3️⃣ *Hablar con un operador* (Soporte humano)
                4️⃣ *📋 Consultar mis Reservas*
                5️⃣ *❌ Cancelar un viaje*
                """;

        messaging.sendText(phoneNumber, menuPrincipal);
    }
}
