package com.lunaris.ansenuza.application.conversation;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitingListCapacityGuard {

    private final ReservationRepository reservationRepository;
    private final SystemConfigurationService systemConfigurationService;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;
    private final WaitingListService waitingListService;

    @Transactional
    public boolean offerWaitingListWhenFull(ConversationSession session) {
        int requestedSeats = session.getPassengerCount() == null
                ? 1 : Math.max(1, session.getPassengerCount());
        String schedule = session.getScheduleBlock() == null
                || session.getScheduleBlock().isBlank()
                ? "03:00 AM" : session.getScheduleBlock().trim();
        int occupiedSeats = Math.toIntExact(reservationRepository.countReservedSeats(
                session.getTravelDate(), schedule));
        int maxCapacity = systemConfigurationService.getPrimaryVehicleCapacity();

        if (occupiedSeats + requestedSeats <= maxCapacity) {
            return false;
        }

        waitingListService.join(session);
        messaging.sendText(session.getPhoneNumber(),
                "⏳ La unidad principal de " + maxCapacity + " pasajeros para el " + schedule
                        + " está completa. Te agregamos a la Lista de Espera y te avisaremos "
                        + "por WhatsApp cuando se libere un lugar.");
        conversationSessionRepository.delete(session);
        conversationSessionRepository.flush();
        return true;
    }
}
