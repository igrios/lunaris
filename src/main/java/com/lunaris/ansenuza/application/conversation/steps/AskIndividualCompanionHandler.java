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
        String currentName = message.body() == null ? "" : message.body().trim().replaceAll("\\s+", " ");
        if (currentName.isBlank()) {
            messaging.sendText(phoneNumber, "⚠️ Ingresá el nombre y apellido del acompañante.");
            return;
        }

        java.util.List<String> companions = parseCompanions(session.getCompanionNames());
        boolean duplicate = companions.stream().anyMatch(name -> name.equalsIgnoreCase(currentName));
        int expectedCompanions = Math.max(0, Math.min(3,
                session.getTotalCompanions() == null ? 0 : session.getTotalCompanions()));
        if (!duplicate && companions.size() < expectedCompanions && companions.size() < 3) {
            companions.add(currentName);
        }
        session.setCompanionNames(String.join(", ", companions));
        session.setPassengerCount(Math.min(4, 1 + companions.size()));

        if (companions.size() >= expectedCompanions) {
            session.setCurrentCompanionIndex(null);
            passengerAddressResolver.resolve(phoneNumber, session);
        } else {
            int nextIndex = companions.size() + 1;
            session.setCurrentCompanionIndex(nextIndex);
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "👤 *Ingresá Nombre y Apellido de tu acompañante " + nextIndex + ":*");
        }
    }

    private java.util.List<String> parseCompanions(String accumulated) {
        if (accumulated == null || accumulated.isBlank()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.stream(accumulated.split(","))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .distinct()
                .limit(3)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }
}
