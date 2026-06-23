package com.lunaris.ansenuza.application.conversation.steps;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_TRIP_TYPE: define si el viaje es solo ida o ida y vuelta, y pide la fecha de ida. */
@Component
@RequiredArgsConstructor
public class AskTripTypeHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_TRIP_TYPE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("trip_ida".equals(body)) {
            session.setRoundTrip(false);
            session.setCurrentStep("ASK_DATE");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "📅 *¿Qué día es el viaje de ida?*\n\nEscribilo separado por barras:\n_Ejemplo: 18/06/2026_");
        } else if ("trip_completo".equals(body)) {
            session.setRoundTrip(true);
            session.setCurrentStep("ASK_DATE");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "📅 *Perfecto, ida y vuelta.*\n\n¿Qué día es el viaje de *ida*?\n_Ejemplo: 18/06/2026_");
        }
    }
}
