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
import java.util.List;
import java.util.Optional;
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
                .passenger(passenger)
                .travelDate(LocalDate.of(2026, 8, 1))
                .status("PENDING_PAYMENT")
                .paymentVerified(true)
                .build();
        String receiptUrl = "https://cdn.example.com/receipt.jpg";
        when(storage.downloadAndSaveReceipt("media-123")).thenReturn(receiptUrl);
        when(passengers.findByPhone("543511111111")).thenReturn(Optional.of(passenger));
        when(reservations.findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(passenger))
                .thenReturn(List.of(reservation));
        ProcessPaymentReceiptUseCase useCase = new ProcessPaymentReceiptUseCase(
                passengers, reservations, storage, messaging, liveChat,
                mock(SameDayBookingPolicy.class), mock(ReservationService.class));

        useCase.execute("543511111111", "media-123");

        assertEquals(receiptUrl, reservation.getPaymentReceiptUrl());
        assertFalse(reservation.getPaymentVerified());
        assertEquals("PAYMENT_RECEIVED", reservation.getStatus());
        verify(reservations).saveAndFlush(reservation);
        verify(liveChat).recordIncomingMessage("543511111111", receiptUrl);
    }

    @Test
    void confirmsExistingPendingReservationDuringWebOtpVerification() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReceiptStoragePort storage = mock(ReceiptStoragePort.class);
        Passenger passenger = Passenger.builder().phone("543511111111").build();
        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .status("PENDING_PAYMENT")
                .paymentVerified(false)
                .build();
        var receipt = new org.springframework.mock.web.MockMultipartFile(
                "receipt", "receipt.jpg", "image/jpeg", new byte[] {1});
        when(passengers.findByPhone("543511111111")).thenReturn(Optional.of(passenger));
        when(reservations.findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(passenger))
                .thenReturn(List.of(reservation));
        when(storage.uploadFile(receipt)).thenReturn("https://cdn.example.com/receipt.jpg");
        ProcessPaymentReceiptUseCase useCase = new ProcessPaymentReceiptUseCase(
                passengers, reservations, storage, mock(MessagingPort.class),
                mock(LiveChatPort.class), mock(SameDayBookingPolicy.class),
                mock(ReservationService.class));

        useCase.confirmOrCreateWebBooking("3511111111", receipt, null);

        assertEquals("CONFIRMED", reservation.getStatus());
        assertTrue(reservation.getPaymentVerified());
        assertEquals("https://cdn.example.com/receipt.jpg", reservation.getPaymentReceiptUrl());
        verify(reservations).saveAndFlush(reservation);
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
        when(reservationService.saveReservationFlow(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation newReservation = invocation.getArgument(0);
                    return List.of(newReservation);
                });
        ProcessPaymentReceiptUseCase useCase = new ProcessPaymentReceiptUseCase(
                passengers, reservations, storage, mock(MessagingPort.class),
                mock(LiveChatPort.class), mock(SameDayBookingPolicy.class), reservationService);
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
        assertEquals(payload.totalAmount(), created.getAmount());
        assertEquals(payload.scheduleBlock(), created.getDepartureSchedule());
        verify(reservationService).saveReservationFlow(created);
    }
}
