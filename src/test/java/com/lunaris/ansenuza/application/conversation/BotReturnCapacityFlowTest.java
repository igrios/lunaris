package com.lunaris.ansenuza.application.conversation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.lunaris.ansenuza.application.conversation.steps.ConfirmationHandler;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.*;
import com.lunaris.ansenuza.domain.repository.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

@org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(
        replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:bot-return-capacity;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "spring.datasource.username=sa", "spring.datasource.password="
})
class BotReturnCapacityFlowTest {
    private final WaitingListRepository persistedEntries;

    @org.springframework.beans.factory.annotation.Autowired
    BotReturnCapacityFlowTest(WaitingListRepository persistedEntries) {
        this.persistedEntries = persistedEntries;
    }

    @Test
    void testBotFlow_RedirectsToWaitingListWhenFull() {
        var reservations = mock(ReservationRepository.class);
        var sessions = mock(ConversationSessionRepository.class);
        var waitingEntries = persistedEntries;
        var messaging = mock(MessagingPort.class);
        var reservationService = mock(ReservationService.class);
        var passengers = mock(PassengerRepository.class);
        var guard = new WaitingListCapacityGuard(reservations, mock(SystemConfigurationService.class),
                sessions, messaging, new WaitingListService(waitingEntries));
        var handler = new ConfirmationHandler(sessions, passengers, mock(PricingAndScheduleService.class),
                mock(PromotionService.class), reservationService, messaging, guard);
        var session = ConversationSession.builder().phoneNumber("543511234567")
                .passengerName("Nuevo Pasajero").pickupLocality("Córdoba").destination("Morteros")
                .travelDate(LocalDate.of(2026, 9, 8)).scheduleBlock("14:00").passengerCount(2)
                .roundTrip(false).currentStep("ASK_CONFIRMATION").build();
        when(reservations.findReturnCapacityCandidates(session.getTravelDate())).thenReturn(List.of(
                Reservation.builder().passengerCount(7).departureSchedule("14:00")
                        .travelStatus(Reservation.TravelStatus.CONFIRMED).build()));

        handler.handle(session, new IncomingMessage(session.getPhoneNumber(),
                IncomingMessage.MessageType.INTERACTIVE, "confirm_ok", null));

        assertEquals("WAITING_LIST", session.getCurrentStep());
        assertEquals(1, persistedEntries.count());
        var entry = persistedEntries.findAll().getFirst();
        assertNotNull(entry.getId());
        assertEquals(WaitingListEntry.WAITING, entry.getStatus());
        assertEquals(2, entry.getPassengerCount());
        assertEquals("Córdoba", entry.getPickupLocality());
        assertEquals("Morteros", entry.getDestination());
        assertEquals(session.getTravelDate(), entry.getTravelDate());
        verify(messaging).sendText(eq(session.getPhoneNumber()), contains("Lista de Espera"));
        verifyNoInteractions(reservationService, passengers);
        verify(sessions).delete(session);
    }
}
