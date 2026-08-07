package com.lunaris.ansenuza.application.conversation.steps;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_RETURN_DATE_TYPE: el pasajero elige entre fijar fecha de regreso o dejar la vuelta abierta. */
@Component
@RequiredArgsConstructor
public class AskReturnDateTypeHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_RETURN_DATE_TYPE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("return_same_day".equals(body)) {
            session.setReturnDate(session.getTravelDate());
            advanceToBilling(session, phoneNumber);
            return;
        }
        if ("return_today".equals(body) || "hoy".equals(body)
                || "return_tomorrow".equals(body) || "mañana".equals(body)) {
            boolean tomorrow = "return_tomorrow".equals(body) || "mañana".equals(body);
            LocalDate selected = com.lunaris.ansenuza.shared.ArgentinaTime.today()
                    .plusDays(tomorrow ? 1 : 0);
            if (session.getTravelDate() != null && selected.isBefore(session.getTravelDate())) {
                messaging.sendText(phoneNumber,
                        "❌ La fecha de regreso no puede ser anterior al viaje de ida. Elegí otra opción:");
                return;
            }
            session.setReturnDate(selected);
            advanceToBilling(session, phoneNumber);
            return;
        }
        if ("return_choose_date".equals(body) || "return_fixed".equals(body)
                || "otra fecha".equals(body) || "otra_fecha".equals(body)) {
            session.setCurrentStep("ASK_RETURN_DATE");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "✍️ *Por favor, ingresá la fecha deseada* (ejemplo: 12/08 o 12 de agosto).\n\n"
                            + "Ventanas desde Córdoba: 14:00 a 15:00 hs o 17:30 a 18:00 hs.");
            return;
        }
        if ("return_open".equals(body)) {
            session.setReturnDate(null);
            advanceToBilling(session, phoneNumber);
            return;
        }
    }

    private void advanceToBilling(ConversationSession session, String phoneNumber) {
        session.setCurrentStep("ASK_DNI_REQUIRED");
        conversationSessionRepository.saveAndFlush(session);
        messaging.sendText(phoneNumber,
                "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números):");
    }
}
