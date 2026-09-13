package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SelectScheduleHandlerTest {

    @Test
    void acceptsStableScheduleButtonPayload() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        SelectScheduleHandler handler = new SelectScheduleHandler(sessions, passengers, messaging,
                mock(com.lunaris.ansenuza.application.usecase.ScheduleService.class));
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .currentStep("SELECT_SCHEDULE")
                .build();
        when(passengers.findByPhone(session.getPhoneNumber())).thenReturn(Optional.empty());

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.INTERACTIVE,
                "schedule_03_00", null));

        assertEquals("03:00 AM", session.getScheduleBlock());
        assertEquals("ASK_NAME", session.getCurrentStep());
        verify(sessions).saveAndFlush(session);
    }
    @Test
    void staleScheduleDoesNotAdvanceBookingAndOffersHumanAssistance() {
        var sessions = mock(ConversationSessionRepository.class);
        var passengers = mock(PassengerRepository.class);
        var messaging = mock(MessagingPort.class);
        var schedules = mock(com.lunaris.ansenuza.application.usecase.ScheduleService.class);
        var session = ConversationSession.builder().phoneNumber("123").pickupLocality("Morteros")
                .destination("Córdoba").passengerCount(3).travelDate(java.time.LocalDate.of(2030, 1, 1))
                .currentStep("SELECT_SCHEDULE").build();
        when(schedules.getSchedulesForBot("Morteros", "Córdoba", session.getTravelDate(), 3))
                .thenReturn(java.util.List.of());
        new SelectScheduleHandler(sessions, passengers, messaging, schedules).handle(session,
                new IncomingMessage("123", IncomingMessage.MessageType.INTERACTIVE, "schedule_08_00", null));
        assertEquals("WAITING_FOR_INQUIRY_MESSAGE", session.getCurrentStep());
        org.junit.jupiter.api.Assertions.assertNull(session.getScheduleBlock());
        org.mockito.Mockito.verifyNoInteractions(passengers);
        verify(messaging).sendText(org.mockito.ArgumentMatchers.eq("123"), org.mockito.ArgumentMatchers.contains("operador"));
    }
}
