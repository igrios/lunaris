package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.*;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WaitingForInquiryMessageHandler implements ConversationStepHandler {
    public static final String STEP = "WAITING_FOR_INQUIRY_MESSAGE";
    public static final String PROMPT = "Por favor, escribe tu consulta o detalle del viaje especial (fecha, origen, destino, cantidad de pasajeros). Un operador la revisará a la brevedad.";
    public static final String CONFIRMATION = "¡Gracias! Tu consulta ha sido registrada. Nos pondremos en contacto contigo a la brevedad.";
    private final InquiryService inquiries;
    private final ConversationSessionRepository sessions;
    private final MessagingPort messaging;

    @Override
    public String step() { return STEP; }

    @Override
    @Transactional
    public void handle(ConversationSession session, IncomingMessage message) {
        if (message.type() != IncomingMessage.MessageType.TEXT
                || message.body() == null || message.body().isBlank()) {
            messaging.sendText(session.getPhoneNumber(), PROMPT);
            return;
        }
        inquiries.register(session.getPhoneNumber(), session.getPassengerName(), message.body());
        session.setCurrentStep("START");
        sessions.saveAndFlush(session);
        messaging.sendText(session.getPhoneNumber(), CONFIRMATION);
    }
}
