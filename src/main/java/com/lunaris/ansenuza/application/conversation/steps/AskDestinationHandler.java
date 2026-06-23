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

/** ASK_DESTINATION: registra el destino en Córdoba (Capital o Aeropuerto) y pide la modalidad. */
@Component
@RequiredArgsConstructor
public class AskDestinationHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_DESTINATION";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        String dest = "dest_aeropuerto".equals(body) ? "Aeropuerto Córdoba"
                : "dest_capital".equals(body) ? "Córdoba" : null;
        if (dest == null) {
            return;
        }

        session.setDestination(dest);
        session.setCurrentStep("ASK_TRIP_TYPE");
        conversationSessionRepository.saveAndFlush(session);

        messaging.sendButtons(phoneNumber, "Modalidad", "🔄 *¿Qué tipo de viaje vas a realizar?*",
                List.of(new Button("trip_ida", "Solo ida ➡️"),
                        new Button("trip_completo", "Ida y vuelta 🔄")));
    }
}
