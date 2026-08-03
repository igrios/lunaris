package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitingListReengagementService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final WaitingListRepository waitingListRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Transactional
    public WaitingListEntry promote(Long id) {
        WaitingListEntry entry = waitingListRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        if (!WaitingListEntry.WAITING.equals(entry.getStatus())) {
            throw new DomainValidationException("Sólo se pueden notificar entradas en estado WAITING.");
        }

        ConversationSession session = conversationSessionRepository
                .findByPhoneNumber(entry.getPhoneNumber())
                .orElseGet(() -> ConversationSession.builder()
                        .phoneNumber(entry.getPhoneNumber())
                        .build());
        session.setPassengerName(entry.getPassengerName());
        session.setTravelDate(entry.getTravelDate());
        session.setPickupLocality(entry.getPickupLocality());
        session.setDestination(entry.getDestination());
        session.setPassengerCount(entry.getPassengerCount());
        session.setWaitingListEntryId(entry.getId());
        session.setCurrentStep("CONFIRMING_WAITING_LIST_BOOKING");
        session.setBotPaused(false);
        conversationSessionRepository.saveAndFlush(session);

        entry.setStatus(WaitingListEntry.NOTIFIED);
        waitingListRepository.saveAndFlush(entry);
        messaging.sendButtons(entry.getPhoneNumber(), "Lugar disponible",
                "¡Hola " + entry.getPassengerName()
                        + "! Se liberó un lugar para tu viaje del "
                        + entry.getTravelDate().format(DATE_FORMAT) + " ("
                        + entry.getPickupLocality() + " -> " + entry.getDestination()
                        + "). ¿Deseás confirmar tu reserva ahora?",
                List.of(
                        new Button("confirm_waiting_list", "Confirmar y Pagar ✅"),
                        new Button("reject_waiting_list", "Rechazar ❌")));
        return entry;
    }
}
