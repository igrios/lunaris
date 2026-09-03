package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunaris.ansenuza.application.conversation.ConversationOrchestrator;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.application.usecase.WhatsAppWebhookInboxService;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppMessageDispatcher;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppWebhookParser;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WhatsAppWebhookControllerSecurityTest {

    private static final String APP_SECRET = "meta-app-secret-for-tests";
    private MockMvc mockMvc;
    private WhatsAppWebhookParser parser;
    private WhatsAppMessageDispatcher dispatcher;
    private WhatsAppWebhookInboxService inbox;

    @BeforeEach
    void setUp() {
        parser = mock(WhatsAppWebhookParser.class);
        dispatcher = mock(WhatsAppMessageDispatcher.class);
        inbox = mock(WhatsAppWebhookInboxService.class);
        when(parser.parse(any(Map.class))).thenReturn(null);
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        WhatsAppWebhookController controller = new WhatsAppWebhookController(
                parser, mock(ConversationOrchestrator.class),
                mock(ProcessPaymentReceiptUseCase.class),
                dispatcher, inbox, new ObjectMapper(), environment,
                "environment-verify-token", APP_SECRET);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void handshakeUsesConfiguredVerificationToken() throws Exception {
        mockMvc.perform(get("/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "environment-verify-token")
                        .param("hub.challenge", "challenge-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("challenge-123"));
    }

    @Test
    void productionWebhookRejectsMissingOrInvalidSignature() throws Exception {
        byte[] body = "{\"object\":\"whatsapp_business_account\"}"
                .getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(post("/whatsapp/webhook").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/whatsapp/webhook")
                        .header("X-Hub-Signature-256", "sha256=00")
                        .contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsPayloadSignedWithMetaAppSecret() throws Exception {
        byte[] body = "{\"object\":\"whatsapp_business_account\"}"
                .getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(post("/whatsapp/webhook")
                        .header("X-Hub-Signature-256", sign(body))
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());
    }

    @Test
    void duplicateMessageReturnsOkWithoutDispatching() throws Exception {
        byte[] body = "{\"object\":\"whatsapp_business_account\"}"
                .getBytes(StandardCharsets.UTF_8);
        when(parser.parse(any(Map.class))).thenReturn(new IncomingMessage(
                "wamid.duplicate", "543512282251", IncomingMessage.MessageType.TEXT,
                "hola", null, null, null));
        when(inbox.claim("wamid.duplicate")).thenReturn(false);

        mockMvc.perform(post("/whatsapp/webhook")
                        .header("X-Hub-Signature-256", sign(body))
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());

        verifyNoInteractions(dispatcher);
    }

    @Test
    void productionConfigurationFailsWithoutAppSecret() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        WhatsAppWebhookController controller = new WhatsAppWebhookController(
                mock(WhatsAppWebhookParser.class), mock(ConversationOrchestrator.class),
                mock(ProcessPaymentReceiptUseCase.class), mock(WhatsAppMessageDispatcher.class),
                mock(WhatsAppWebhookInboxService.class), new ObjectMapper(), environment,
                "verify-token", "");

        assertThrows(IllegalStateException.class, controller::validateProductionConfiguration);
    }

    private String sign(byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body));
    }
}
