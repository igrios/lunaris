package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.WaitingListCapacityGuard;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ConfirmationHandlerCapacityTest {

    @Test
    void fullScheduleOffersWaitingListWithoutCreatingPassenger() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        PricingAndScheduleService schedules = mock(PricingAndScheduleService.class);
        MessagingPort messaging = mock(MessagingPort.class);
        WaitingListCapacityGuard capacityGuard = mock(WaitingListCapacityGuard.class);
        ConfirmationHandler handler = new ConfirmationHandler(
                sessions, passengers, schedules, mock(PromotionService.class),
                mock(ReservationService.class), messaging, capacityGuard);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .travelDate(LocalDate.of(2026, 8, 20))
                .scheduleBlock("08:00 AM")
                .passengerCount(2)
                .currentStep("ASK_CONFIRMATION")
                .build();
        when(capacityGuard.offerWaitingListWhenFull(session)).thenReturn(true);

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.INTERACTIVE,
                "confirm_ok", null));

        verify(capacityGuard).offerWaitingListWhenFull(session);
        verify(passengers, never()).findByPhone(session.getPhoneNumber());
    }
}
