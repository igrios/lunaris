package com.lunaris.ansenuza.application.conversation.steps;

import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_MARKETING_CONFIRMATION: el pasajero confirma (o no) iniciar la reserva tras la cotización. */
@Component
@RequiredArgsConstructor
public class MarketingConfirmationHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_MARKETING_CONFIRMATION";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("yes_reserve".equals(body)) {
            session.setCurrentStep("SELECT_SCHEDULE");
            conversationSessionRepository.saveAndFlush(session);

            String primerHorario = pricingAndScheduleService
                    .calculateEstimatedPickupTime(session.getPickupLocality(), "03:00");
            String segundoHorario = pricingAndScheduleService
                    .calculateEstimatedPickupTime(session.getPickupLocality(), "08:00");

            String infoTexto = "⏱️ *Horarios de retiro por tu domicilio:*\n"
                    + "• Opción 1: Pasa aprox *" + primerHorario + "*\n"
                    + "• Opción 2: Pasa aprox *" + segundoHorario + "*\n\n"
                    + "Seleccioná el horario en el que preferís viajar:";

            messaging.sendButtons(phoneNumber, "Selección de Horario", infoTexto,
                    List.of(new Button("time_0300", "Primer Horario 🌙"),
                            new Button("time_0800", "Segundo Horario ☀️")));
            return;
        }
        if ("no_cancel".equals(body)) {
            conversationSessionRepository.delete(session);
            messaging.sendText(phoneNumber,
                    "Entendido. Si cambiás de opinión, escribinos 'Hola' cuando quieras.");
            return;
        }
    }
}
