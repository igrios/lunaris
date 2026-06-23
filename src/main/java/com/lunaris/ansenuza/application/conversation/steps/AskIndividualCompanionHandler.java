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

        int currentIndex = Integer.parseInt(session.getCuil());
        int nextIndex = currentIndex + 1;

        if (currentIndex >= session.getTotalCompanions()) {
            session.setCuil(null);
            passengerAddressResolver.resolve(phoneNumber, session);
        } else {
            session.setCuil(String.valueOf(nextIndex));
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "👤 *Ingresá Nombre y Apellido de tu acompañante " + nextIndex + ":*");
        }
    }
}
