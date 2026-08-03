package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AwaitingPaymentHandler implements ConversationStepHandler {

    private final MessagingPort messaging;

    @Override
    public String step() {
        return "AWAITING_PAYMENT";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        messaging.sendText(session.getPhoneNumber(),
                "Estamos esperando tu comprobante. Enviá una foto para que podamos verificar el pago.");
    }
}
