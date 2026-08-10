package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.ScheduleService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MarketingConfirmationHandlerTest {

    @Test
    @SuppressWarnings("unchecked")
    void botSchedulesGenerateStableInteractiveButtonPayloads() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        ScheduleService schedules = mock(ScheduleService.class);
        MessagingPort messaging = mock(MessagingPort.class);
        MarketingConfirmationHandler handler = new MarketingConfirmationHandler(
                sessions, pricing, schedules, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .pickupLocality("Arrufó")
                .destination("Córdoba")
                .currentStep("ASK_MARKETING_CONFIRMATION")
                .build();
        when(schedules.getSchedulesForBot("Arrufó", "Córdoba", null))
                .thenReturn(List.of("03:00 AM", "08:00 AM"));
        when(pricing.calculateEstimatedPickupTime(eq("Arrufó"), anyString()))
                .thenReturn("03:00 hs", "08:00 hs");

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.INTERACTIVE,
                "yes_reserve", null));

        ArgumentCaptor<List<Button>> buttons = ArgumentCaptor.forClass(List.class);
        verify(messaging).sendButtons(eq(session.getPhoneNumber()),
                eq("Selección de Horario"), anyString(), buttons.capture());
        assertEquals(List.of(
                new Button("schedule_03_00", "Horario 1 🌙"),
                new Button("schedule_08_00", "Horario 2 ☀️")), buttons.getValue());
        assertEquals("SELECT_SCHEDULE", session.getCurrentStep());
        verify(sessions).saveAndFlush(session);
    }
}
