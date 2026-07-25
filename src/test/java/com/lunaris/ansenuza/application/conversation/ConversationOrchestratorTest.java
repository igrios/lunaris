package com.lunaris.ansenuza.application.conversation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.application.usecase.ProcessPromotionCommandUseCase;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.model.service.ReservationCancellationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;

class ConversationOrchestratorTest {

    @Test
    void interactiveOnboardSelectionBypassesChatAndInvokesUseCase() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        LiveChatPort liveChat = mock(LiveChatPort.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        OnboardPassengerUseCase onboard = mock(OnboardPassengerUseCase.class);
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .passenger(Passenger.builder()
                        .firstName("Ana")
                        .lastName("Pérez")
                        .build())
                .build();
        when(onboard.execute(reservationId)).thenReturn(reservation);
        ConversationOrchestrator orchestrator = new ConversationOrchestrator(
                List.of(),
                sessions,
                liveChat,
                mock(OperationControlService.class),
                mock(ReservationCancellationService.class),
                mock(DriverRepository.class),
                mock(ReservationRepository.class),
                whatsApp,
                mock(ProcessPromotionCommandUseCase.class),
                onboard);
        String driverPhone = "543512282251";

        orchestrator.process(new IncomingMessage(
                driverPhone,
                IncomingMessage.MessageType.INTERACTIVE,
                "ONBOARD_" + reservationId,
                null));

        verify(onboard).execute(reservationId);
        verify(whatsApp).sendMessage(
                driverPhone, "✓ Pasajero [Ana Pérez] marcado a bordo.");
        verify(sessions, never()).findByPhoneNumber(driverPhone);
        verify(liveChat, never()).recordIncomingMessage(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rawReservationIdFromInteractiveReplyAlsoInvokesUseCase() {
        OnboardPassengerUseCase onboard = mock(OnboardPassengerUseCase.class);
        UUID reservationId = UUID.randomUUID();
        when(onboard.execute(reservationId)).thenReturn(Reservation.builder()
                .id(reservationId)
                .passenger(Passenger.builder()
                        .firstName("Luis")
                        .lastName("Gómez")
                        .build())
                .build());
        ConversationOrchestrator orchestrator = new ConversationOrchestrator(
                List.of(),
                mock(ConversationSessionRepository.class),
                mock(LiveChatPort.class),
                mock(OperationControlService.class),
                mock(ReservationCancellationService.class),
                mock(DriverRepository.class),
                mock(ReservationRepository.class),
                mock(WhatsAppService.class),
                mock(ProcessPromotionCommandUseCase.class),
                onboard);

        orchestrator.process(new IncomingMessage(
                "543512282251",
                IncomingMessage.MessageType.INTERACTIVE,
                reservationId.toString(),
                null));

        verify(onboard).execute(reservationId);
    }
}
