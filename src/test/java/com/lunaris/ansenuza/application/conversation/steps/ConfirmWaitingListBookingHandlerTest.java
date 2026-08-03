package com.lunaris.ansenuza.application.conversation.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListConversionService;
import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import org.junit.jupiter.api.Test;

class ConfirmWaitingListBookingHandlerTest {

    @Test
    void capacityFailureRepliesGracefullyAndKeepsCurrentStep() {
        WaitingListConversionService conversion = mock(WaitingListConversionService.class);
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        ConfirmWaitingListBookingHandler handler =
                new ConfirmWaitingListBookingHandler(conversion, sessions, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .waitingListEntryId(7L)
                .currentStep("CONFIRMING_WAITING_LIST_BOOKING")
                .build();
        when(conversion.beginPayment(7L)).thenThrow(
                new SeatCapacityExceededException("Cupo completo"));

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.INTERACTIVE,
                "confirm_waiting_list", null));

        verify(messaging).sendText(
                session.getPhoneNumber(),
                "Disculpá, en este momento el cupo sigue completo. "
                        + "Te avisaremos apenas se confirme un nuevo coche de refuerzo.");
        verify(sessions, never()).saveAndFlush(session);
    }
}
