package com.lunaris.ansenuza.application.conversation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class WaitingListCapacityGuardTest {

    @Test
    void sumsRequestedPassengersAndOffersWaitingListAboveConfiguredCapacity() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        SystemConfigurationService configurations = mock(SystemConfigurationService.class);
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        WaitingListService waitingList = mock(WaitingListService.class);
        WaitingListCapacityGuard guard = new WaitingListCapacityGuard(
                reservations, configurations, sessions, messaging, waitingList);
        ConversationSession session = sessionWithPassengers(3);
        when(reservations.countReservedSeats(session.getTravelDate(), "03:00 AM"))
                .thenReturn(10L);
        when(configurations.getScheduleMaxCapacity()).thenReturn(12);

        assertTrue(guard.offerWaitingListWhenFull(session));

        verify(sessions).saveAndFlush(session);
        verify(waitingList).join(session);
        verify(messaging).sendText(
                org.mockito.ArgumentMatchers.eq(session.getPhoneNumber()),
                org.mockito.ArgumentMatchers.contains("cupo de 12 pasajeros"));
    }

    @Test
    void allowsBookingWhenRequestedSeatsFitExactly() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        SystemConfigurationService configurations = mock(SystemConfigurationService.class);
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        WaitingListCapacityGuard guard = new WaitingListCapacityGuard(
                reservations, configurations, sessions, messaging,
                mock(WaitingListService.class));
        ConversationSession session = sessionWithPassengers(2);
        when(reservations.countReservedSeats(session.getTravelDate(), "03:00 AM"))
                .thenReturn(10L);
        when(configurations.getScheduleMaxCapacity()).thenReturn(12);

        assertFalse(guard.offerWaitingListWhenFull(session));

        verify(sessions, never()).saveAndFlush(session);
    }

    private ConversationSession sessionWithPassengers(int passengerCount) {
        return ConversationSession.builder()
                .phoneNumber("543511112222")
                .pickupLocality("Morteros")
                .destination("Córdoba")
                .travelDate(LocalDate.of(2026, 8, 20))
                .passengerCount(passengerCount)
                .build();
    }
}
