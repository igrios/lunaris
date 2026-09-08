package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

class TomorrowBookingConversationTest {

    @ParameterizedTest
    @CsvSource({
            "Córdoba, false, false, true",
            "Córdoba, true, false, true",
            "Córdoba, false, true, true",
            "Córdoba, true, true, true",
            "Cordoba, true, false, true",
            "Morteros, false, false, true",
            "Morteros, true, false, false",
            "Morteros, true, true, false"
    })
    void offersAndValidatesTomorrowAccordingToOrigin(
            String origin, boolean pastCutoff, boolean roundTrip, boolean tomorrowAllowed) {
        var sessions = mock(ConversationSessionRepository.class);
        var messaging = mock(MessagingPort.class);
        var policy = mock(SameDayBookingPolicy.class);
        var operations = mock(OperationControlService.class);
        String schedule = origin.equals("Morteros") ? "03:00 AM" : "14:00";
        when(operations.isPastCutoffTime()).thenReturn(pastCutoff);
        when(policy.isTodayClosed(schedule)).thenReturn(pastCutoff);
        var session = ConversationSession.builder().phoneNumber("543511112222")
                .pickupLocality(origin).scheduleBlock(schedule)
                .currentStep("ASK_TRIP_TYPE").build();
        var tomorrow = ArgentinaTime.today().plusDays(1);
        String datePayload = tomorrow.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Button>> buttons = ArgumentCaptor.forClass(List.class);

        new AskTripTypeHandler(sessions, messaging, policy, operations).handle(session,
                message(roundTrip ? "trip_completo" : "trip_ida"));

        verify(messaging).sendButtons(eq(session.getPhoneNumber()), eq("Fecha del viaje"),
                anyString(), buttons.capture());
        assertEquals("ASK_DATE", session.getCurrentStep());
        assertEquals(tomorrowAllowed, buttons.getValue().contains(new Button(datePayload, "Mañana")));

        // También valida una fecha escrita manualmente, aunque no aparezca entre los botones.
        new AskDateHandler(sessions, messaging, operations, policy).handle(session, message(datePayload));

        if (tomorrowAllowed) {
            assertEquals(tomorrow, session.getTravelDate());
            assertEquals(roundTrip ? "ASK_RETURN_DATE_TYPE" : "ASK_DNI_REQUIRED", session.getCurrentStep());
            verify(messaging, never()).sendText(anyString(), contains("Logística Cerrada para Mañana"));
        } else {
            assertNull(session.getTravelDate());
            assertEquals("ASK_DATE", session.getCurrentStep());
            verify(messaging).sendText(eq(session.getPhoneNumber()), contains("Logística Cerrada para Mañana"));
        }
        verify(policy).validate(tomorrow, schedule);
    }

    private IncomingMessage message(String body) {
        return new IncomingMessage("543511112222", IncomingMessage.MessageType.TEXT, body, null);
    }
}
