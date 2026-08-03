package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WaitingListConfirmationHandler implements ConversationStepHandler {

    private final WaitingListService waitingListService;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "WAITING_LIST_CONFIRMATION";
    }

    @Override
    @Transactional
    public void handle(ConversationSession session, IncomingMessage message) {
        String response = message.body().trim().toLowerCase();
        if ("waiting_list_yes".equals(response)) {
            waitingListService.join(session);
            conversationSessionRepository.delete(session);
            messaging.sendText(session.getPhoneNumber(), """
                    ✅ *Te sumamos a la LISTA DE ESPERA.*

                    Te contactaremos por este número si se libera un lugar para el viaje solicitado.
                    """);
            return;
        }
        if ("waiting_list_no".equals(response)) {
            conversationSessionRepository.delete(session);
            messaging.sendText(session.getPhoneNumber(),
                    "Entendido. No te agregamos a la lista de espera. Escribí *Hola* cuando quieras consultar otra fecha.");
        }
    }
}
