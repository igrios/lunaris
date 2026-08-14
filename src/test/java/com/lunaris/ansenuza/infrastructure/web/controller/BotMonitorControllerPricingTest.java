package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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
}
