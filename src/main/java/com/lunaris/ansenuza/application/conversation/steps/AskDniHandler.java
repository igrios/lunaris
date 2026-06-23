package com.lunaris.ansenuza.application.conversation.steps;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationPresenter;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_DNI_REQUIRED: valida el DNI/CUIT fiscal y muestra el resumen del itinerario para confirmar. */
@Component
@RequiredArgsConstructor
public class AskDniHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final ConversationPresenter presenter;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_DNI_REQUIRED";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        String cleanDni = body.replaceAll("[^0-9]", "");
        if (cleanDni.length() < 7 || cleanDni.length() > 11) {
            messaging.sendText(phoneNumber,
                    "❌ *DNI o CUIT inválido.* Verificá el número e ingresalo nuevamente sin guiones:");
            return;
        }
        session.setCuil(cleanDni);
        session.setCurrentStep("ASK_CONFIRMATION");
        conversationSessionRepository.saveAndFlush(session);
        presenter.sendReservationSummaryWithButtons(phoneNumber, session);
    }
}
