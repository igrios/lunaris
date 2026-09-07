package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.application.usecase.LocalityService;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.application.usecase.TakeOverConversationUseCase;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.WhatsAppConversationWindowService;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.controller.ChatController;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class, TakeOverConversationUseCase.class})
class ChatTakeoverSecurityIntegrationTest {
    private static final String PHONE = "5493562123456";
    private static final String ENDPOINT = "/admin/chat/" + PHONE + "/takeover";

    @Autowired MockMvc mockMvc;
    @MockitoBean AccountRepository accounts;
    @MockitoBean PassengerOtpService passengerOtpService;
    @MockitoBean ChatMessageRepository messages;
    @MockitoBean ConversationSessionRepository sessions;
    @MockitoBean LocalityService localities;
    @MockitoBean PassengerRepository passengers;
    @MockitoBean WhatsAppConversationWindowService conversationWindow;
    @MockitoBean WhatsAppService whatsApp;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "OPERADOR"})
    void authorizedOperatorPausesBotAndOpensFullPhone(String role) throws Exception {
        var session = ConversationSession.builder().id(9L).phoneNumber(PHONE).build();
        when(sessions.findByPhoneNumber(PHONE)).thenReturn(Optional.of(session));

        mockMvc.perform(post(ENDPOINT).with(user("operador").roles(role)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/chat/" + PHONE));

        assertTrue(session.isBotPaused());
        verify(sessions).saveAndFlush(session);
        verifyNoInteractions(whatsApp);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHOFER", "FACTURACION"})
    void otherRolesCannotTakeOverConversation(String role) throws Exception {
        mockMvc.perform(post(ENDPOINT).with(user("otro").roles(role)).with(csrf()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(sessions, whatsApp);
    }

    @Test
    void missingCsrfCannotPauseBot() throws Exception {
        mockMvc.perform(post(ENDPOINT).with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(sessions, whatsApp);
    }
}
