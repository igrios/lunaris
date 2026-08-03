package com.lunaris.ansenuza.application.conversation.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.ConversationPresenter;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.WaitingListCapacityGuard;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import org.junit.jupiter.api.Test;

class AskPromotionCodeCapacityTest {

    @Test
    void fullRouteOffersWaitingListBeforeShowingSummary() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        ConversationPresenter presenter = mock(ConversationPresenter.class);
        WaitingListCapacityGuard guard = mock(WaitingListCapacityGuard.class);
        AskPromotionCodeHandler handler = new AskPromotionCodeHandler(
                sessions, mock(PromotionService.class), presenter,
                mock(MessagingPort.class), guard);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .currentStep("ASK_PROMOTION_CODE")
                .build();
        when(guard.offerWaitingListWhenFull(session)).thenReturn(true);

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.TEXT,
                "SIN PROMO", null));

        verify(guard).offerWaitingListWhenFull(session);
        verify(presenter, never()).sendReservationSummaryWithButtons(
                session.getPhoneNumber(), session);
    }
}
