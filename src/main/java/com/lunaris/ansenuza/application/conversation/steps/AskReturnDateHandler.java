package com.lunaris.ansenuza.application.conversation.steps;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.FechaParser; // Importamos tu parseador flexible
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_RETURN_DATE: valida la fecha de regreso fijada de forma flexible y avanza a la facturación. */
@Component
@RequiredArgsConstructor
public class AskReturnDateHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_RETURN_DATE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();

        // Parseamos usando la lógica flexible que acepta año corto y ceros omitidos
        Optional<LocalDate> fechaParseada = FechaParser.parsear(message.body());

        if (fechaParseada.isEmpty()) {
            messaging.sendText(phoneNumber,
                    "❌ *Formato erróneo.* Por favor, indicá la fecha de tu regreso "
                            + "(por ejemplo: 12/08/2026):");
            return;
        }

        LocalDate returnDate = fechaParseada.get();

        // Mantenemos la validación temporal de tu regla de negocio
        if (returnDate.isBefore(session.getTravelDate())) {
            messaging.sendText(phoneNumber,
                    "❌ El regreso no puede ser anterior al viaje de ida.");
            return;
        }

        session.setReturnDate(returnDate);
        session.setCurrentStep("ASK_DNI_REQUIRED");
        conversationSessionRepository.saveAndFlush(session);
        
        messaging.sendText(phoneNumber,
                "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números):");
    }
}
