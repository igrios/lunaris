package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
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
                passengers, reservations, storage, messaging, liveChat);

        useCase.execute("543511111111", "media-123");

        assertEquals(receiptUrl, reservation.getPaymentReceiptUrl());
        assertFalse(reservation.getPaymentVerified());
        assertEquals("PAYMENT_RECEIVED", reservation.getStatus());
        verify(reservations).saveAndFlush(reservation);
        verify(liveChat).recordIncomingMessage("543511111111", receiptUrl);
    }
}
