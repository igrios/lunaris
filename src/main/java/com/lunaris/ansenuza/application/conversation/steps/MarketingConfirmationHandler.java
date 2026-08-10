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
import com.lunaris.ansenuza.application.usecase.ScheduleService;
import lombok.RequiredArgsConstructor;

/** ASK_MARKETING_CONFIRMATION: el pasajero confirma (o no) iniciar la reserva tras la cotización. */
@Component
@RequiredArgsConstructor
public class MarketingConfirmationHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final ScheduleService scheduleService;
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
            List<String> schedules = scheduleService.getSchedulesForBot(
                    session.getPickupLocality(), session.getDestination(), session.getTravelDate());
            if (schedules.isEmpty()) {
                messaging.sendText(phoneNumber,
                        "No hay horarios con disponibilidad para la fecha seleccionada.");
                return;
            }
            session.setCurrentStep("SELECT_SCHEDULE");
            conversationSessionRepository.saveAndFlush(session);

            String scheduleDetails = schedules.stream()
                    .map(schedule -> "• Pasa aprox *" + pricingAndScheduleService
                            .calculateEstimatedPickupTime(
                                    session.getPickupLocality(), schedule.substring(0, 5)) + "*")
                    .collect(java.util.stream.Collectors.joining("\n"));
            String infoTexto = "⏱️ *Horarios de retiro por tu domicilio:*\n"
                    + scheduleDetails + "\n\nSeleccioná el horario en el que preferís viajar:";

            messaging.sendButtons(phoneNumber, "Selección de Horario", infoTexto,
                    schedules.stream()
                            .map(schedule -> new Button(
                                    buttonPayload(schedule),
                                    "03:00 AM".equals(schedule)
                                            ? "Horario 1 🌙" : "Horario 2 ☀️"))
                            .toList());
            return;
        }
        if ("no_cancel".equals(body)) {
            conversationSessionRepository.delete(session);
            messaging.sendText(phoneNumber,
                    "Entendido. Si cambiás de opinión, escribinos 'Hola' cuando quieras.");
            return;
        }
    }

    private String buttonPayload(String schedule) {
        return "schedule_" + schedule.substring(0, 5).replace(':', '_');
    }
}
