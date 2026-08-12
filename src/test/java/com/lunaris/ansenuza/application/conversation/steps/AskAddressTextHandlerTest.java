package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.UpdatePassengerAddressUseCase;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;

class AskAddressTextHandlerTest {

    @Test
    void storesMapsLinkWhenPassengerSharesWhatsappLocation() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        UpdatePassengerAddressUseCase addressUpdater = mock(UpdatePassengerAddressUseCase.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskAddressTextHandler handler = new AskAddressTextHandler(sessions, addressUpdater, messaging);
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
        UpdatePassengerAddressUseCase addressUpdater = mock(UpdatePassengerAddressUseCase.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskAddressTextHandler handler = new AskAddressTextHandler(sessions, addressUpdater, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543512282251")
                .currentStep("ASK_ADDRESS_TEXT")
                .pickupLocality("Morteros")
                .build();

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.TEXT,
                "  San Martín 450  ", null));

        assertEquals("San Martín 450", session.getPickupAddress());
        assertEquals("ASK_DESTINATION", session.getCurrentStep());
        verify(addressUpdater).update("543512282251", "San Martín 450", "Morteros");
        verify(sessions).saveAndFlush(session);
    }

    @Test
    void retriesWithFreshTransactionAfterOptimisticLockConflict() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        UpdatePassengerAddressUseCase addressUpdater = mock(UpdatePassengerAddressUseCase.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskAddressTextHandler handler = new AskAddressTextHandler(sessions, addressUpdater, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543512282251")
                .currentStep("ASK_ADDRESS_TEXT")
                .pickupLocality("Morteros")
                .build();
        doThrow(new ObjectOptimisticLockingFailureException(Passenger.class, "concurrent"))
                .doNothing()
                .when(addressUpdater).update("543512282251", "San Martín 450", "Morteros");

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.TEXT,
                "San Martín 450", null));

        assertEquals("ASK_DESTINATION", session.getCurrentStep());
        verify(addressUpdater, times(2))
                .update("543512282251", "San Martín 450", "Morteros");
        verify(sessions).saveAndFlush(session);
    }

    @Test
    void keepsAddressStepAndAsksToRetryWhenPassengerUpdateFails() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        UpdatePassengerAddressUseCase addressUpdater = mock(UpdatePassengerAddressUseCase.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskAddressTextHandler handler = new AskAddressTextHandler(sessions, addressUpdater, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543512282251")
                .currentStep("ASK_ADDRESS_TEXT")
                .pickupLocality("Morteros")
                .build();
        doThrow(new IllegalStateException("database unavailable"))
                .when(addressUpdater).update(
                        session.getPhoneNumber(), "San Martín 450", "Morteros");

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
