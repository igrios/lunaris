package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReturnWindowSelectionHandler implements ConversationStepHandler {

    private final ReservationRepository reservationRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "RETURN_WINDOW_SELECTION";
    }

    @Override
    @Transactional
    public void handle(ConversationSession session, IncomingMessage message) {
        String selection = message.body() == null ? "" : message.body().trim();
        String schedule = switch (selection) {
            case "1" -> "14:00";
            case "2" -> "17:30";
            default -> null;
        };
        if (schedule == null) {
            messaging.sendText(session.getPhoneNumber(),
                    "Respondé 1 para Turno Tarde o 2 para Turno Vespertino.");
            return;
        }
        Reservation reservation = reservationRepository
                .findByReservationCodeForUpdate(session.getReservationCode())
                .orElseThrow(() -> new IllegalStateException("No se encontró la reserva de regreso."));
        reservation.setDepartureSchedule(schedule);
        reservation.setTravelStatus(Reservation.TravelStatus.CONFIRMED);
        reservationRepository.saveAndFlush(reservation);
        session.setCurrentStep("START");
        conversationSessionRepository.saveAndFlush(session);
        messaging.sendText(session.getPhoneNumber(),
                "✅ Preferencia registrada: " + ("14:00".equals(schedule)
                        ? "Turno Tarde (14:00 a 15:00 hs)."
                        : "Turno Vespertino (17:30 a 18:00 hs)."));
    }
}
