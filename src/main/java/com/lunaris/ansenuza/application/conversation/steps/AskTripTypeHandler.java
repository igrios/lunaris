package com.lunaris.ansenuza.application.conversation.steps;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_TRIP_TYPE: define si el viaje es solo ida o ida y vuelta, y pide la fecha de ida. */
@Component
@RequiredArgsConstructor
public class AskTripTypeHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;
    private final SameDayBookingPolicy sameDayBookingPolicy;
    private final OperationControlService operationControlService;

    @Override
    public String step() {
        return "ASK_TRIP_TYPE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("trip_ida".equals(body)) {
            session.setRoundTrip(false);
            session.setCurrentStep("ASK_DATE");
            conversationSessionRepository.saveAndFlush(session);
            sendDateOptions(session, "📅 *¿Qué día es el viaje de ida?*");
        } else if ("trip_completo".equals(body)) {
            session.setRoundTrip(true);
            session.setCurrentStep("ASK_DATE");
            conversationSessionRepository.saveAndFlush(session);
            sendDateOptions(session,
                    "📅 *Perfecto, ida y vuelta.* ¿Qué día es el viaje de ida?");
        }
    }

    private void sendDateOptions(ConversationSession session, String prompt) {
        String phoneNumber = session.getPhoneNumber();
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        DateTimeFormatter payloadFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Button> options = new ArrayList<>();
        if (!sameDayBookingPolicy.isTodayClosed(session.getScheduleBlock())) {
            options.add(new Button(today.format(payloadFormat),
                    "Hoy (" + today.format(payloadFormat) + ")"));
        }
        LocalDate candidate = today.plusDays(1);
        if (operationControlService.isPastCutoffTime()) {
            candidate = candidate.plusDays(1);
        }
        while (options.size() < 3) {
            String formattedDate = candidate.format(payloadFormat);
            String title = candidate.equals(today.plusDays(1))
                    ? "Mañana (" + formattedDate + ")"
                    : formattedDate;
            options.add(new Button(formattedDate, title));
            candidate = candidate.plusDays(1);
        }
        messaging.sendButtons(phoneNumber, "Fecha del viaje",
                prompt + "\n\nPor favor, indicá la fecha de tu viaje "
                        + "(por ejemplo: 12/08/2026):", options);
    }
}
