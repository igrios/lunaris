package com.lunaris.ansenuza.application.conversation;

import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.List;
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

    @Transactional
    public boolean offerWaitingListWhenFull(ConversationSession session) {
        int requestedSeats = session.getPassengerCount() == null
                ? 1 : Math.max(1, session.getPassengerCount());
        Integer occupiedResult = reservationRepository.countConfirmedPassengersByRouteAndDate(
                session.getPickupLocality(), session.getDestination(), session.getTravelDate());
        int occupiedSeats = occupiedResult == null ? 0 : occupiedResult;
        int maxCapacity = systemConfigurationService.getScheduleMaxCapacity();

        if (occupiedSeats + requestedSeats <= maxCapacity) {
            return false;
        }

        session.setCurrentStep("WAITING_LIST_CONFIRMATION");
        conversationSessionRepository.saveAndFlush(session);
        messaging.sendButtons(session.getPhoneNumber(), "LISTA DE ESPERA",
                "El cupo de " + maxCapacity
                        + " pasajes para esa fecha está completo. "
                        + "¿Deseás anotarte en la Lista de Espera?",
                List.of(
                        new Button("waiting_list_yes", "Sumarme ✅"),
                        new Button("waiting_list_no", "No, gracias ❌")));
        return true;
    }
}
