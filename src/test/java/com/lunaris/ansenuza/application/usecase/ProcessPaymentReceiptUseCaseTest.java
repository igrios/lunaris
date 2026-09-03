package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;

class ProcessPaymentReceiptUseCaseTest {

    @Test
    void persistsReceiptAsPendingReviewOnOldestPendingReservation() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReceiptStoragePort storage = mock(ReceiptStoragePort.class);
        MessagingPort messaging = mock(MessagingPort.class);
        LiveChatPort liveChat = mock(LiveChatPort.class);
        Passenger passenger = Passenger.builder().phone("543511111111").build();
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .passenger(passenger)
                .travelDate(LocalDate.of(2026, 8, 1))
                .reservationCode("ARR-COR-001-IDA")
                .status("PENDING_PAYMENT")
                .paymentVerified(true)
                .paymentExpiresAt(LocalDateTime.of(2026, 8, 1, 10, 0))
                .build();
        Reservation returnLeg = Reservation.builder().id(UUID.randomUUID())
                .passenger(passenger).reservationCode("ARR-COR-001-VUELTA")
                .status("PENDING_PAYMENT").paymentVerified(true).build();
        returnLeg.setPaymentExpiresAt(LocalDateTime.of(2026, 8, 1, 10, 0));
        String receiptUrl = "https://cdn.example.com/receipt.jpg";
        when(storage.downloadAndSaveReceipt("media-123")).thenReturn(receiptUrl);
        when(passengers.findByPhone("543511111111")).thenReturn(Optional.of(passenger));
        when(reservations.findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(passenger))
                .thenReturn(List.of(reservation));
        when(reservations.findReservationGroupForUpdate("ARR-COR-001"))
                .thenReturn(List.of(reservation, returnLeg));
        ProcessPaymentReceiptUseCase useCase = new ProcessPaymentReceiptUseCase(
                passengers, reservations, storage, messaging, liveChat,
                mock(SameDayBookingPolicy.class), mock(ReservationService.class),
                new PersistPaymentReceiptUseCase(passengers, reservations,
                        mock(ReservationEventRepository.class)),
                mock(PricingAndScheduleService.class));

        useCase.execute("543511111111", "media-123");

        assertEquals(receiptUrl, reservation.getPaymentReceiptUrl());
        assertFalse(reservation.getPaymentVerified());
        assertEquals("PAYMENT_RECEIVED", reservation.getStatus());
        assertEquals(null, reservation.getPaymentExpiresAt());
        assertEquals(receiptUrl, returnLeg.getPaymentReceiptUrl());
        assertFalse(returnLeg.getPaymentVerified());
        assertEquals("PAYMENT_RECEIVED", returnLeg.getStatus());
        assertEquals(null, returnLeg.getPaymentExpiresAt());
        verify(reservations).saveAllAndFlush(List.of(reservation, returnLeg));
        verify(liveChat).recordIncomingMessage("543511111111", receiptUrl);
    }

    @Test
    void leavesExistingReservationPendingUntilOperatorVerifiesReceipt() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReceiptStoragePort storage = mock(ReceiptStoragePort.class);
        Passenger passenger = Passenger.builder().phone("543511111111").build();
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .passenger(passenger)
                .reservationCode("ARR-COR-002-IDA")
                .status("PENDING_PAYMENT")
                .paymentVerified(false)
                .build();
        Reservation returnLeg = Reservation.builder().id(UUID.randomUUID())
                .passenger(passenger).reservationCode("ARR-COR-002-VUELTA")
                .status("PENDING_PAYMENT").paymentVerified(false).build();
        var receipt = new org.springframework.mock.web.MockMultipartFile(
                "receipt", "receipt.jpg", "image/jpeg", new byte[] {1});
        when(passengers.findByPhone("543511111111")).thenReturn(Optional.of(passenger));
        when(reservations.findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(passenger))
                .thenReturn(List.of(reservation));
        when(reservations.findReservationGroupForUpdate("ARR-COR-002"))
                .thenReturn(List.of(reservation, returnLeg));
        when(storage.uploadFile(receipt)).thenReturn("https://cdn.example.com/receipt.jpg");
        PersistPaymentReceiptUseCase receiptPersistence = mock(PersistPaymentReceiptUseCase.class);
        ProcessPaymentReceiptUseCase useCase = new ProcessPaymentReceiptUseCase(
                passengers, reservations, storage, mock(MessagingPort.class),
                mock(LiveChatPort.class), mock(SameDayBookingPolicy.class),
                mock(ReservationService.class), receiptPersistence,
                mock(PricingAndScheduleService.class));

        useCase.confirmOrCreateWebBooking("3511111111", receipt, null);

        assertEquals("PENDING_PAYMENT", reservation.getStatus());
        assertFalse(reservation.getPaymentVerified());
        verify(receiptPersistence).executeByReservationCode(
                "ARR-COR-002-IDA", "https://cdn.example.com/receipt.jpg", "PASSENGER_WEB");
    }

    @Test
    void createsReservationFromPayloadWhenNoPendingReservationExists() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReceiptStoragePort storage = mock(ReceiptStoragePort.class);
        Passenger passenger = Passenger.builder().phone("543511111111").build();
        when(passengers.findByPhone("543511111111")).thenReturn(Optional.of(passenger));
        when(reservations.findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(passenger))
                .thenReturn(List.of());
        ReservationService reservationService = mock(ReservationService.class);
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        when(pricing.calculateReservationAmount(
                "La Puerta", "Córdoba", TripType.ONE_WAY, 2))
                .thenReturn(new BigDecimal("42000.00"));
        when(reservationService.saveReservationFlow(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation newReservation = invocation.getArgument(0);
                    return List.of(newReservation);
                });
        ProcessPaymentReceiptUseCase useCase = new ProcessPaymentReceiptUseCase(
                passengers, reservations, storage, mock(MessagingPort.class),
                mock(LiveChatPort.class), mock(SameDayBookingPolicy.class), reservationService,
                mock(PersistPaymentReceiptUseCase.class), pricing);
        BookingVerificationData payload = new BookingVerificationData(
                LocalDate.of(2026, 8, 10), "08:00 AM", "La Puerta", "Córdoba",
                2, TripType.ONE_WAY, new BigDecimal("56000.00"));

        Reservation created = useCase.confirmOrCreateWebBooking("3511111111", null, payload);

        assertNotNull(created.getId());
        assertEquals(passenger, created.getPassenger());
        assertEquals("PENDING_VERIFICATION", created.getStatus());
        assertFalse(created.getPaymentVerified());
        assertTrue(created.getRequiresInvoice());
        assertEquals(ReservationSource.WEB, created.getSource());
        assertEquals(new BigDecimal("42000.00"), created.getAmount());
        assertEquals(payload.scheduleBlock(), created.getDepartureSchedule());
        verify(reservationService).saveReservationFlow(created);
    }
}
