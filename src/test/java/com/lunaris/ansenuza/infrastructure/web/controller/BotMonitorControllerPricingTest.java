package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;

class BotMonitorControllerPricingTest {

    @Test
    void manualQuoteDelegatesPassengerCountAndTripModeToOfficialPricingService() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        when(pricing.calculateReservationAmount("Morteros", "Córdoba", true, 3))
                .thenReturn(new BigDecimal("300000.00"));
        BotMonitorController controller = new BotMonitorController(
                mock(ConversationSessionRepository.class),
                mock(SimpMessagingTemplate.class),
                mock(PassengerRepository.class),
                mock(ChatMessageRepository.class),
                mock(LocalityRepository.class),
                mock(WhatsAppService.class),
                pricing,
                mock(ReceiptStoragePort.class),
                mock(OperationControlService.class),
                mock(ReservationService.class));

        var response = controller.cotizarFilaManual("Morteros", "Córdoba", 3, true);

        assertEquals(new BigDecimal("300000.00"), response.getBody().get("monto"));
        verify(pricing).calculateReservationAmount("Morteros", "Córdoba", true, 3);
    }

    @Test
    void manualBookingIsConfirmedButDoesNotClaimVerifiedPayment() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        ReservationService reservations = mock(ReservationService.class);
        when(passengers.findByPhone("3511111111")).thenReturn(Optional.empty());
        when(pricing.calculateReservationAmount("Morteros", "Córdoba", false, 1))
                .thenReturn(new BigDecimal("48000.00"));
        when(reservations.saveReservationFlow(any(Reservation.class), any()))
                .thenReturn(List.of());
        BotMonitorController controller = new BotMonitorController(
                mock(ConversationSessionRepository.class), mock(SimpMessagingTemplate.class),
                passengers, mock(ChatMessageRepository.class), mock(LocalityRepository.class),
                mock(WhatsAppService.class), pricing, mock(ReceiptStoragePort.class),
                mock(OperationControlService.class), reservations);

        controller.cargarReservaWebTradicional(
                "3511111111", "Ada", "Lovelace", null, "Morteros", "Córdoba",
                "Belgrano 100", 1, LocalDate.of(2026, 9, 10), null, "08:00",
                null, false, false, null, mock(RedirectAttributes.class));

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservations).saveReservationFlow(captor.capture(), org.mockito.ArgumentMatchers.isNull());
        assertEquals("CONFIRMED", captor.getValue().getStatus());
        assertEquals(false, captor.getValue().getPaymentVerified());
    }
}
