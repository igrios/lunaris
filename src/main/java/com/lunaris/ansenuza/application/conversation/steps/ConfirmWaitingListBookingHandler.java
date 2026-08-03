package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListConversionService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ConfirmWaitingListBookingHandler implements ConversationStepHandler {

    private final WaitingListConversionService conversionService;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "CONFIRMING_WAITING_LIST_BOOKING";
    }

    @Override
    @Transactional
    public void handle(ConversationSession session, IncomingMessage message) {
        String response = message.body().trim().toLowerCase();
        if ("confirm_waiting_list".equals(response)) {
            try {
                conversionService.beginPayment(session.getWaitingListEntryId());
            } catch (SeatCapacityExceededException exception) {
                messaging.sendText(session.getPhoneNumber(),
                        "Disculpá, en este momento el cupo sigue completo. "
                                + "Te avisaremos apenas se confirme un nuevo coche de refuerzo.");
                return;
            }
            session.setCurrentStep("AWAITING_PAYMENT");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(session.getPhoneNumber(), """
                    ✅ *Reservamos temporalmente tu lugar.*

                    Para confirmarlo, realizá la transferencia y enviá por acá una foto del comprobante.

                    • *Titular:* Martín Fernando Manuel Cuestaz
                    • *Alias:* cuestazm.bna
                    • *CBU:* 01103739330037363119529
                    """);
            return;
        }
        if ("reject_waiting_list".equals(response)) {
            conversionService.cancel(session.getWaitingListEntryId());
            conversationSessionRepository.delete(session);
            messaging.sendText(session.getPhoneNumber(),
                    "Entendido. Cancelamos tu lugar en la lista de espera.");
        }
    }
}
