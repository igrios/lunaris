package com.lunaris.ansenuza.application.conversation.steps;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.PassengerAddressResolver;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_INDIVIDUAL_COMPANION: acumula los nombres de cada acompañante uno por uno. */
@Component
@RequiredArgsConstructor
public class AskIndividualCompanionHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerAddressResolver passengerAddressResolver;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_INDIVIDUAL_COMPANION";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String currentName = message.body().trim();
        String accumulated = session.getCompanionNames();

        if (accumulated == null || accumulated.isBlank()) {
            accumulated = currentName;
        } else {
            accumulated = accumulated + ", " + currentName;
        }
        session.setCompanionNames(accumulated);
        session.setPassengerCount(session.getPassengerCount() + 1);

        // 🛠️ CORRECCIÓN CRÍTICA: Reemplazamos el abuso semántico de session.getCuil()
        // Usamos el campo entero dedicado de la base de datos para controlar el bucle
        Integer currentIndexObj = session.getCurrentCompanionIndex();
        int currentIndex = (currentIndexObj != null) ? currentIndexObj : 1;
        int nextIndex = currentIndex + 1;

        // 🛠️ Evaluamos contra el total de acompañantes esperados
        if (currentIndex >= session.getTotalCompanions()) {
            // Ya cargó todos. Dejamos el índice en limpio y resolvemos dirección.
            session.setCurrentCompanionIndex(null);
            passengerAddressResolver.resolve(phoneNumber, session);
        } else {
            // Faltan acompañantes. Incrementamos el índice correcto de forma tipada
            session.setCurrentCompanionIndex(nextIndex);
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "👤 *Ingresá Nombre y Apellido de tu acompañante " + nextIndex + ":*");
        }
    }
}