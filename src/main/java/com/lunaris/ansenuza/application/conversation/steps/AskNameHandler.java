package com.lunaris.ansenuza.application.conversation.steps;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_NAME: captura el nombre del pasajero titular (cuando no estaba registrado). */
@Component
@RequiredArgsConstructor
public class AskNameHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_NAME";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        session.setPassengerName(message.body().trim());
        session.setCurrentStep("ASK_COMPANIONS_COUNT");
        conversationSessionRepository.saveAndFlush(session);
        messaging.sendText(phoneNumber,
                "🔢 *Escribí cuántas personas viajan con vos, o 0 si estás solo (0)*");
    }
}
