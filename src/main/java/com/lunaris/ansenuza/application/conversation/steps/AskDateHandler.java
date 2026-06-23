package com.lunaris.ansenuza.application.conversation.steps;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_DATE: valida la fecha de ida; deriva a fecha de regreso (ida y vuelta) o a facturación. */
@Component
@RequiredArgsConstructor
public class AskDateHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_DATE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            LocalDate travelDate = LocalDate.parse(message.body(), dateFormatter);
            if (travelDate.isBefore(LocalDate.now())) {
                messaging.sendText(phoneNumber,
                        "❌ La fecha no puede ser anterior a hoy. Reingresá:");
                return;
            }
            session.setTravelDate(travelDate);

            if (Boolean.TRUE.equals(session.getRoundTrip())) {
                session.setCurrentStep("ASK_RETURN_DATE_TYPE");
                conversationSessionRepository.saveAndFlush(session);

                messaging.sendButtons(phoneNumber, "Fecha de Regreso",
                        "📅 *¿Cuándo programamos el regreso desde Córdoba?*\n\nSi todavía no sabés el día exacto, podés dejar la fecha abierta y coordinarla más adelante con Martín.",
                        List.of(new Button("return_fixed", "Fijar Fecha 🗓️"),
                                new Button("return_open", "Vuelta Abierta 🔄")));
            } else {
                session.setCurrentStep("ASK_DNI_REQUIRED");
                conversationSessionRepository.saveAndFlush(session);
                messaging.sendText(phoneNumber,
                        "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números):");
            }
            return;
        } catch (Exception e) {
            messaging.sendText(phoneNumber,
                    "❌ *Formato erróneo.* Acordate de usar barras separadoras: 18/06/2026");
            return;
        }
    }
}
