package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.domain.model.service.WhatsAppConversationWindowService;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssistedChatServiceTest {
    private final ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
    private final ReservationRepository reservations = mock(ReservationRepository.class);
    private final WhatsAppConversationWindowService window = mock(WhatsAppConversationWindowService.class);
    private final AssistedChatService service = new AssistedChatService(sessions, reservations, window);

    @Test void manualHandoffAllowsSendingWithExpiredWindow() {
        when(sessions.findByPhoneNumber("123")).thenReturn(Optional.of(ConversationSession.builder()
                .botPaused(true).manuallyPaused(true).build()));
        assertTrue(service.canSend("123"));
    }

    @Test void automaticPauseDoesNotBypassExpiredWindow() {
        when(sessions.findByPhoneNumber("123")).thenReturn(Optional.of(ConversationSession.builder()
                .botPaused(true).manuallyPaused(false).build()));
        assertFalse(service.canSend("123"));
    }

    @Test void activeIncomingWindowAllowsSendingWithoutManualHandoff() {
        when(window.isActive("123")).thenReturn(true);
        assertTrue(service.canSend("123"));
    }

    @Test void specialTripFillsMissingSessionData() {
        var date = ArgentinaTime.now().toLocalDate().plusDays(1);
        when(reservations.findByPassengerPhone("123")).thenReturn(List.of(Reservation.builder()
                .tripCategory(TripCategory.SPECIAL).travelDate(date).pickupLocality("Aeropuerto")
                .destination("Miramar").passengerCount(7).pickupAddress("Terminal")
                .departureSchedule("14:30").roundTrip(true).build()));
        when(sessions.findByPhoneNumber("123")).thenReturn(Optional.of(ConversationSession.builder()
                .passengerName("Ana Pérez").destination("Balnearia").build()));
        var data = service.prefill("123");
        assertEquals("123", data.getPhoneNumber());
        assertEquals("Ana Pérez", data.getPassengerName());
        assertEquals("Aeropuerto", data.getPickupLocality());
        assertEquals("Balnearia", data.getDestination());
        assertEquals(7, data.getPassengerCount());
        assertEquals(date, data.getTravelDate());
        assertEquals("14:30", data.getScheduleBlock());
    }


    @Test void manualCreationReplacesOldSessionTripData() {
        var id = java.util.UUID.randomUUID();
        var session = ConversationSession.builder().pickupLocality("Origen anterior").build();
        var reservation = Reservation.builder().passenger(Passenger.builder()
                .firstName("Ana").lastName("Pérez").phone("123").build())
                .pickupLocality("Aeropuerto").destination("Miramar").passengerCount(7)
                .travelDate(ArgentinaTime.now().toLocalDate().plusDays(1)).build();
        when(reservations.findById(id)).thenReturn(Optional.of(reservation));
        when(sessions.findByPhoneNumber("123")).thenReturn(Optional.of(session));
        new ManualReservationChatHandoffService(reservations, sessions,
                mock(com.lunaris.ansenuza.application.port.LiveChatPort.class))
                .pause(new ManualReservationCreated(id));
        assertEquals("Aeropuerto", session.getPickupLocality());
        assertEquals("Miramar", session.getDestination());
        assertEquals(7, session.getPassengerCount());
        assertTrue(session.isManuallyPaused());
        verify(sessions).saveAndFlush(session);
    }

    @Test void cancelledTripIsNotPrefilled() {
        when(reservations.findByPassengerPhone("123")).thenReturn(List.of(Reservation.builder()
                .travelDate(ArgentinaTime.now().toLocalDate()).status("CANCELLED")
                .pickupLocality("Obsoleto").build()));
        assertNull(service.prefill("123").getPickupLocality());
    }
}
