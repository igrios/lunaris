package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.conversation.*;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.*;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.*;

class CordobaOriginFlowTest {
    @Test
    void selectsOnlyActiveFareTownsAndKeepsDestinationThroughPassengerDetails() {
        var localities = mock(LocalityRepository.class);
        var sessions = mock(ConversationSessionRepository.class);
        var pricing = mock(PricingAndScheduleService.class);
        var messaging = mock(MessagingPort.class);
        var passengers = mock(PassengerRepository.class);
        var session = ConversationSession.builder().phoneNumber("543511112222")
                .currentStep("ASK_LOCALITY").build();
        when(localities.findAllWithActiveFare()).thenReturn(List.of(Locality.builder().name("Morteros").build(),
                Locality.builder().name("Marull").build(), Locality.builder().name("Córdoba").build()));
        new AskLocalityHandler(sessions, localities, pricing, messaging).handle(session, message("3"));
        assertEquals("Córdoba", session.getPickupLocality());
        assertNull(session.getPickupAddress());
        assertEquals("ASK_TOWN_DESTINATION", session.getCurrentStep());
        verify(messaging).sendText(anyString(), contains("1) Marull"));
        when(pricing.calculateTripPrice("Marull", true, 1)).thenReturn(new BigDecimal("50000"));
        new AskTownDestinationHandler(localities, sessions, pricing, messaging).handle(session, message("1"));
        assertEquals("Marull", session.getDestination());
        assertEquals("ASK_MARKETING_CONFIRMATION", session.getCurrentStep());
        var schedules = new ScheduleService(pricing, mock(LocalityService.class));
        assertEquals(List.of("14:00", "17:30"), schedules.getSchedulesForBot("Córdoba", "Marull", null));
        var selector = new SelectScheduleHandler(sessions, passengers, messaging, schedules);
        selector.handle(session, message("schedule_03_00"));
        assertNull(session.getScheduleBlock());
        selector.handle(session, message("schedule_17_30"));
        assertEquals("17:30", session.getScheduleBlock());
        new PassengerAddressResolver(passengers, sessions, messaging).resolve(session.getPhoneNumber(), session);
        assertEquals("ASK_ADDRESS_TEXT", session.getCurrentStep());
        assertEquals("Marull", session.getDestination());
        verify(messaging).requestLocation(eq(session.getPhoneNumber()), contains("Córdoba"));
        verify(localities, never()).findAll();
    }

    private IncomingMessage message(String body) {
        return new IncomingMessage("543511112222", IncomingMessage.MessageType.TEXT, body, null);
    }
}
