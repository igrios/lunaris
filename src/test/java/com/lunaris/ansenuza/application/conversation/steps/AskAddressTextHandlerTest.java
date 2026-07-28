package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;

class AskAddressTextHandlerTest {

    @Test
    void storesMapsLinkWhenPassengerSharesWhatsappLocation() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskAddressTextHandler handler = new AskAddressTextHandler(sessions, passengers, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543512282251")
                .currentStep("ASK_ADDRESS_TEXT")
                .pickupLocality("Morteros")
                .build();
        IncomingMessage location = new IncomingMessage(
                session.getPhoneNumber(),
                IncomingMessage.MessageType.LOCATION,
                "https://maps.google.com/?q=-31.42,-64.18",
                null,
                -31.42,
                -64.18);

        handler.handle(session, location);

        assertEquals("https://maps.google.com/?q=-31.42,-64.18", session.getPickupAddress());
        assertEquals("ASK_DESTINATION", session.getCurrentStep());
        verify(sessions).saveAndFlush(session);
    }

    @Test
    void updatesExistingPassengerAddressAndAdvancesConversation() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskAddressTextHandler handler = new AskAddressTextHandler(sessions, passengers, messaging);
        Passenger passenger = Passenger.builder()
                .phone("543512282251")
                .firstName("Ana")
                .lastName("Pérez")
                .address("Dirección anterior")
                .locality("Porteña")
                .build();
        ConversationSession session = ConversationSession.builder()
                .phoneNumber(passenger.getPhone())
                .currentStep("ASK_ADDRESS_TEXT")
                .pickupLocality("Morteros")
                .build();
        when(passengers.findByPhone(passenger.getPhone())).thenReturn(Optional.of(passenger));

        handler.handle(session, new IncomingMessage(
                passenger.getPhone(), IncomingMessage.MessageType.TEXT,
                "  San Martín 450  ", null));

        assertEquals("San Martín 450", passenger.getAddress());
        assertEquals("Morteros", passenger.getLocality());
        assertEquals("San Martín 450", session.getPickupAddress());
        assertEquals("ASK_DESTINATION", session.getCurrentStep());
        verify(passengers).saveAndFlush(passenger);
        verify(sessions).saveAndFlush(session);
    }

    @Test
    void keepsAddressStepAndAsksToRetryWhenPassengerUpdateFails() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskAddressTextHandler handler = new AskAddressTextHandler(sessions, passengers, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543512282251")
                .currentStep("ASK_ADDRESS_TEXT")
                .pickupLocality("Morteros")
                .build();
        when(passengers.findByPhone(session.getPhoneNumber()))
                .thenThrow(new IllegalStateException("database unavailable"));

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.TEXT,
                "San Martín 450", null));

        assertEquals("ASK_ADDRESS_TEXT", session.getCurrentStep());
        verify(sessions, never()).saveAndFlush(session);
        verify(messaging).requestLocation(
                session.getPhoneNumber(),
                "⚠️ No pudimos guardar esa dirección. Enviá nuevamente calle y número, "
                        + "o compartí tu ubicación.");
    }
}
