package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.domain.model.service.*;
import com.lunaris.ansenuza.application.usecase.*;
import com.lunaris.ansenuza.infrastructure.config.*;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;

@WebMvcTest(BotMonitorController.class)
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class})
class BotMonitorWebTest {
    @MockitoBean com.lunaris.ansenuza.application.usecase.CreateManualReservationUseCase createManualReservation;
    @Autowired MockMvc mvc;
    @MockitoBean ConversationSessionRepository sessions;
    @MockitoBean PassengerRepository passengers;
    @MockitoBean ChatMessageRepository messages;
    @MockitoBean LocalityRepository localities;
    @MockitoBean WhatsAppService whatsapp;
    @MockitoBean PricingAndScheduleService pricing;
    @MockitoBean ReceiptStoragePort receipts;
    @MockitoBean OperationControlService operations;
    @MockitoBean ReservationService reservations;
    @MockitoBean BotMonitorService monitor;
    @MockitoBean SimpMessagingTemplate websocket;
    @MockitoBean AccountRepository accounts;
    @MockitoBean PassengerOtpService otp;
    @MockitoBean InquiryService inquiries;

    @Test void manualFormBindsAmountsCompanionsAndDirection() throws Exception {
        mvc.perform(post("/admin/bot/monitor/cargar-reserva-web")
                .with(csrf()).with(user("op").roles("OPERADOR"))
                .param("phone", "+54 9 351 111 2222").param("firstName", "Ana").param("lastName", "Pérez")
                .param("cuil", "27123456789").param("pickupLocality", "Morteros").param("destination", "Córdoba")
                .param("pickupAddress", "Belgrano 100").param("travelDate", "2030-01-02")
                .param("departureSchedule", "08:00").param("passengerCount", "2")
                .param("amount", "12345.67").param("discountAmount", "321.00").param("extraAmount", "450.50")
                .param("companionNames", "Juan Pérez").param("routeDirection", "IDA")
                .param("requiresInvoice", "true").param("notes", "Equipaje"))
                .andExpect(status().is3xxRedirection());
        var captor = org.mockito.ArgumentCaptor.forClass(com.lunaris.ansenuza.domain.model.Reservation.class);
        verify(createManualReservation).execute(captor.capture(), isNull());
        var reservation = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(reservation.getAmount()).isEqualByComparingTo("12345.67");
        org.assertj.core.api.Assertions.assertThat(reservation.getDiscountAmount()).isEqualByComparingTo("321");
        org.assertj.core.api.Assertions.assertThat(reservation.getExtraAmount()).isEqualByComparingTo("450.50");
        org.assertj.core.api.Assertions.assertThat(reservation.getCompanionNames()).isEqualTo("Juan Pérez");
        org.assertj.core.api.Assertions.assertThat(reservation.getRouteDirection()).isEqualTo("IDA");
        org.assertj.core.api.Assertions.assertThat(reservation.getRequiresInvoice()).isTrue();
        verifyNoInteractions(pricing);
    }

    @Test void operatorSeesUnassignedSessionWithSafeContentAndChatLink() throws Exception {
        var row = mock(ConversationSessionRepository.MonitorRow.class);
        when(row.getId()).thenReturn(1L);
        when(row.getPhoneNumber()).thenReturn("123");
        when(row.getPassengerName()).thenReturn("Ana");
        when(row.getLastMessage()).thenReturn("<script>alert(1)</script>");
        when(row.getBotPaused()).thenReturn(true);
        when(monitor.rows()).thenReturn(List.of(row));
        mvc.perform(get("/admin/bot/monitor").with(user("operador-nuevo").roles("OPERADOR")))
                .andExpect(status().isOk());
        mvc.perform(get("/admin/bot/monitor/rows").with(user("operador-nuevo").roles("OPERADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Muteado (Manual)")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/admin/chat?phone=123")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-paused=\"false\"")));
    }

    @Test void pauseAllowsOperatorRequiresCsrfAndRejectsDriver() throws Exception {
        mvc.perform(post("/admin/bot/monitor/pause").param("id", "1").param("paused", "true")
                .with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/admin/bot/monitor/pause").param("id", "1").param("paused", "true")
                .with(csrf()).with(user("driver").roles("CHOFER"))).andExpect(status().isForbidden());
        mvc.perform(post("/admin/bot/monitor/pause").param("id", "1").param("paused", "true")
                .with(csrf()).with(user("op").roles("OPERADOR"))).andExpect(status().isNoContent());
        verify(monitor).setPaused(1L, true);
    }
}
