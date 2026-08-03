package com.lunaris.ansenuza.application.conversation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.application.usecase.ProcessPromotionCommandUseCase;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Driver;
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
    void activeDriverIsRoutedBeforeOperatorLoadBalancerAndPassengerSessionCreation() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        OperationControlService operationControl = mock(OperationControlService.class);
        LiveChatPort liveChat = mock(LiveChatPort.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        Driver driver = new Driver();
        driver.setPhone("351 555-0101");
        driver.setActive(true);
        when(drivers.findFirstByPhone("3515550101")).thenReturn(Optional.of(driver));
        ConversationOrchestrator orchestrator = new ConversationOrchestrator(
                List.of(),
                sessions,
                liveChat,
                operationControl,
                mock(ReservationCancellationService.class),
                drivers,
                mock(ReservationRepository.class),
                whatsApp,
                mock(ProcessPromotionCommandUseCase.class),
                mock(OnboardPassengerUseCase.class));

        orchestrator.process(new IncomingMessage(
                "+54 9 351 555-0101",
                IncomingMessage.MessageType.TEXT,
                "hola",
                null));

        verify(whatsApp).sendMessage(
                "+54 9 351 555-0101",
                "🚐 Menú de chofer\n\nEscribí *VER RUTA* para consultar tus viajes asignados.");
        verify(sessions, never()).findByPhoneNumber(
                org.mockito.ArgumentMatchers.anyString());
        verify(sessions, never()).saveAndFlush(
                org.mockito.ArgumentMatchers.any());
        verify(operationControl, never()).getOperatorWithLeastLoad();
        verify(liveChat, never()).recordIncomingMessage(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void completedReservationReturnsFriendlyMessageWithoutBoardingAgain() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        OnboardPassengerUseCase onboard = mock(OnboardPassengerUseCase.class);
        UUID reservationId = UUID.randomUUID();
        Reservation completed = Reservation.builder()
                .id(reservationId)
                .status("COMPLETED")
                .travelStatus(Reservation.TravelStatus.REALIZED)
                .build();
        when(reservations.findById(reservationId)).thenReturn(Optional.of(completed));
        ConversationOrchestrator orchestrator = new ConversationOrchestrator(
                List.of(),
                mock(ConversationSessionRepository.class),
                mock(LiveChatPort.class),
                mock(OperationControlService.class),
                mock(ReservationCancellationService.class),
                mock(DriverRepository.class),
                reservations,
                whatsApp,
                mock(ProcessPromotionCommandUseCase.class),
                onboard);

        orchestrator.process(new IncomingMessage(
                "543512282251",
                IncomingMessage.MessageType.INTERACTIVE,
                "ONBOARD_" + reservationId,
                null));

        verify(onboard, never()).execute(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString());
        verify(whatsApp).sendMessage(
                "543512282251",
                "Esta reserva ya se encuentra abordada o finalizada.");
    }

    @Test
    void anyRegisteredDriverCanQueryTripsOnFutureDatesWithoutConversationSession() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        ReservationRepository reservations = mock(ReservationRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        OnboardPassengerUseCase onboard = mock(OnboardPassengerUseCase.class);
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setPhone("351 555-0101");
        driver.setFullName("Chofer Asignado");
        driver.setActive(false);
        Reservation futureTrip = Reservation.builder()
                .id(UUID.randomUUID())
                .driver(driver)
                .passenger(Passenger.builder()
                        .firstName("Ana")
                        .lastName("Pérez")
                        .phone("3515550202")
                        .build())
                .travelDate(LocalDate.of(2030, 12, 15))
                .routeSequence(1)
                .departureSchedule("08:00")
                .pickupLocality("Morteros")
                .pickupAddress("San Martín 100")
                .destination("Córdoba")
                .status("CONFIRMED")
                .build();
        when(drivers.findFirstByPhone("3515550101")).thenReturn(Optional.of(driver));
        when(reservations.findAllAssignedByDriverId(driver.getId()))
                .thenReturn(new java.util.ArrayList<>(List.of(futureTrip)));
        ConversationOrchestrator orchestrator = new ConversationOrchestrator(
                List.of(),
                sessions,
                mock(LiveChatPort.class),
                mock(OperationControlService.class),
                mock(ReservationCancellationService.class),
                drivers,
                reservations,
                whatsApp,
                mock(ProcessPromotionCommandUseCase.class),
                onboard);

        orchestrator.process(new IncomingMessage(
                "3515550101",
                IncomingMessage.MessageType.INTERACTIVE,
                "MIS_VIAJES",
                null));

        verify(reservations).findAllAssignedByDriverId(driver.getId());
        verify(whatsApp).sendMessage(
                org.mockito.ArgumentMatchers.eq("3515550101"),
                org.mockito.ArgumentMatchers.contains("Ana Pérez"));
        verify(sessions, never()).findByPhoneNumber(
                org.mockito.ArgumentMatchers.anyString());
        verify(onboard, never()).execute(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void activeDriverLocationUpdatesCurrentGoogleMapsLink() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setPhone("543512282251");
        driver.setActive(true);
        when(drivers.findFirstByPhone(driver.getPhone())).thenReturn(Optional.of(driver));
        ConversationOrchestrator orchestrator = new ConversationOrchestrator(
                List.of(),
                sessions,
                mock(LiveChatPort.class),
                mock(OperationControlService.class),
                mock(ReservationCancellationService.class),
                drivers,
                mock(ReservationRepository.class),
                whatsApp,
                mock(ProcessPromotionCommandUseCase.class),
                mock(OnboardPassengerUseCase.class));

        orchestrator.process(new IncomingMessage(
                driver.getPhone(),
                IncomingMessage.MessageType.LOCATION,
                "https://maps.google.com/?q=-31.42,-64.18",
                null,
                -31.42,
                -64.18));

        assertEquals(
                "https://maps.google.com/?q=-31.42,-64.18",
                driver.getCurrentLocationUrl());
        verify(drivers).saveAndFlush(driver);
        verify(whatsApp).sendMessage(
                driver.getPhone(), "✓ Ubicación del chofer actualizada.");
    }

    @Test
    void passengerAddressSessionTakesPriorityOverDriverLocationUpdate() {
        String phone = "543512282251";
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        ConversationStepHandler addressHandler = mock(ConversationStepHandler.class);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber(phone)
                .currentStep("AWAITING_PICKUP_ADDRESS")
                .botPaused(false)
                .build();
        Driver driver = new Driver();
        driver.setPhone(phone);
        driver.setActive(true);
        when(sessions.findByPhoneNumber(phone)).thenReturn(Optional.of(session));
        when(addressHandler.step()).thenReturn("ASK_ADDRESS_TEXT");
        ConversationOrchestrator orchestrator = new ConversationOrchestrator(
                List.of(addressHandler),
                sessions,
                mock(LiveChatPort.class),
                mock(OperationControlService.class),
                mock(ReservationCancellationService.class),
                drivers,
                mock(ReservationRepository.class),
                mock(WhatsAppService.class),
                mock(ProcessPromotionCommandUseCase.class),
                mock(OnboardPassengerUseCase.class));
        IncomingMessage location = new IncomingMessage(
                phone,
                IncomingMessage.MessageType.LOCATION,
                "https://maps.google.com/?q=-31.42,-64.18",
                null,
                -31.42,
                -64.18);

        orchestrator.process(location);

        verify(addressHandler).handle(session, location);
        verify(drivers, never()).findFirstByPhone(phone);
        verify(drivers, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }

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
        when(onboard.execute(reservationId, "543512282251")).thenReturn(reservation);
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

        verify(onboard).execute(reservationId, driverPhone);
        verify(sessions, never()).findByPhoneNumber(driverPhone);
        verify(liveChat, never()).recordIncomingMessage(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rawReservationIdFromInteractiveReplyAlsoInvokesUseCase() {
        OnboardPassengerUseCase onboard = mock(OnboardPassengerUseCase.class);
        DriverRepository drivers = mock(DriverRepository.class);
        UUID reservationId = UUID.randomUUID();
        Driver activeDriver = new Driver();
        activeDriver.setPhone("543512282251");
        activeDriver.setActive(true);
        when(drivers.findFirstByPhone("543512282251"))
                .thenReturn(Optional.of(activeDriver));
        when(onboard.execute(reservationId, "543512282251")).thenReturn(Reservation.builder()
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
                drivers,
                mock(ReservationRepository.class),
                mock(WhatsAppService.class),
                mock(ProcessPromotionCommandUseCase.class),
                onboard);

        orchestrator.process(new IncomingMessage(
                "543512282251",
                IncomingMessage.MessageType.INTERACTIVE,
                reservationId.toString(),
                null));

        verify(onboard).execute(reservationId, "543512282251");
    }

    @Test
    void passengerInteractiveReplyContinuesStandardChatFlow() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        OnboardPassengerUseCase onboard = mock(OnboardPassengerUseCase.class);
        ConversationStepHandler startHandler = mock(ConversationStepHandler.class);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511111111")
                .currentStep("START")
                .botPaused(false)
                .build();
        when(startHandler.step()).thenReturn("START");
        when(sessions.findByPhoneNumber("543511111111"))
                .thenReturn(Optional.of(session));
        when(drivers.findFirstByPhone("543511111111"))
                .thenReturn(Optional.empty());
        when(drivers.findByActiveTrue()).thenReturn(List.of());
        ConversationOrchestrator orchestrator = new ConversationOrchestrator(
                List.of(startHandler),
                sessions,
                mock(LiveChatPort.class),
                mock(OperationControlService.class),
                mock(ReservationCancellationService.class),
                drivers,
                mock(ReservationRepository.class),
                mock(WhatsAppService.class),
                mock(ProcessPromotionCommandUseCase.class),
                onboard);
        IncomingMessage passengerReply = new IncomingMessage(
                "543511111111",
                IncomingMessage.MessageType.INTERACTIVE,
                "OK avisarme",
                null);

        orchestrator.process(passengerReply);

        verify(startHandler).handle(session, passengerReply);
        verify(onboard, never()).execute(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString());
    }
}
