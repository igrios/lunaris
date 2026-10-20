package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.lunaris.ansenuza.application.conversation.ConversationOrchestrator;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.ChatbotTelemetryPort;
import com.lunaris.ansenuza.application.telemetry.ChatbotEventType;
import com.lunaris.ansenuza.application.telemetry.ChatbotInteraction;
import com.lunaris.ansenuza.application.telemetry.ChatbotReason;
import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.application.usecase.WhatsAppWebhookInboxService;
import com.lunaris.ansenuza.domain.model.PassengerMessageReceived;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppMessageDispatcher;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppWebhookParser;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(value = WhatsAppWebhookController.class, properties = {
        "whatsapp.verify-token=test-verify-token", "whatsapp.app-secret=test-app-secret"
})
@AutoConfigureMockMvc(addFilters = false)
@RecordApplicationEvents
class WhatsAppWebhookControllerTest {
    private static final String URL = "/whatsapp/webhook";
    private static final String SECRET = "test-app-secret";
    private static final String PHONE = "543512282251";
    private static final String MESSAGE_ID = "wamid.test";
    private static final String BODY = "{\"object\":\"whatsapp_business_account\"}";

    @Autowired private MockMvc mvc;
    @Autowired private WhatsAppWebhookController controller;
    @Autowired private ApplicationEvents events;
    @MockBean private WhatsAppWebhookParser parser;
    @MockBean private ConversationOrchestrator orchestrator;
    @MockBean private ProcessPaymentReceiptUseCase receipts;
    @MockBean private WhatsAppMessageDispatcher dispatcher;
    @MockBean private WhatsAppWebhookInboxService inbox;
    @MockBean private ChatbotTelemetryPort telemetry;
    // Global MVC advice also depends on this use case.
    @MockBean private InquiryService inquiryService;
    @MockBean private PassengerOtpService passengerOtpService;
    @MockBean private AccountRepository accountRepository;

    @BeforeEach
    void resetConfiguration() {
        ReflectionTestUtils.setField(controller, "verifyToken", "test-verify-token");
        ReflectionTestUtils.setField(controller, "appSecret", SECRET);
        ReflectionTestUtils.setField(controller, "environment", new MockEnvironment());
    }

    @Test
    void handshakeReturnsChallengeForMatchingToken() throws Exception {
        mvc.perform(handshake("test-verify-token"))
                .andExpect(status().isOk()).andExpect(content().string("challenge-123"));
        verifyNoInteractions(parser, inbox, dispatcher, orchestrator, receipts);
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong-token", "", "TEST-VERIFY-TOKEN"})
    void handshakeRejectsNonMatchingToken(String token) throws Exception {
        mvc.perform(handshake(token)).andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void handshakeRejectsUnconfiguredToken(String token) throws Exception {
        ReflectionTestUtils.setField(controller, "verifyToken", token);
        mvc.perform(handshake(token)).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"hub.mode", "hub.verify_token", "hub.challenge"})
    void handshakeRequiresEachParameter(String missing) throws Exception {
        MockHttpServletRequestBuilder request = get(URL);
        Map.of("hub.mode", "subscribe", "hub.verify_token", "test-verify-token",
                "hub.challenge", "challenge-123").forEach((name, value) -> {
                    if (!name.equals(missing)) request.param(name, value);
                });
        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"sha1=00", "SHA256=00", "sha256=00", "sha256=zz", "sha256=0", "sha256="})
    void rejectsMissingMalformedOrIncorrectSignature(String signature) throws Exception {
        MockHttpServletRequestBuilder request = post(URL).contentType("application/json").content(BODY);
        if (signature != null) request.header("X-Hub-Signature-256", signature);
        mvc.perform(request).andExpect(status().isUnauthorized()).andExpect(content().string(""));
        verifyNoInteractions(parser, inbox, dispatcher, telemetry, orchestrator, receipts);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsWebhookWithoutAppSecret(String secret) throws Exception {
        ReflectionTestUtils.setField(controller, "appSecret", secret);
        mvc.perform(signed(BODY)).andExpect(status().isUnauthorized());
        verifyNoInteractions(parser, inbox, dispatcher);
    }

    @Test
    void signatureMustMatchExactRequestBytes() throws Exception {
        mvc.perform(signed(BODY).content(BODY + " ")).andExpect(status().isUnauthorized());
        verifyNoInteractions(parser);
    }

    @Test
    void acknowledgesNotificationWithoutMessage() throws Exception {
        mvc.perform(signed(BODY)).andExpect(status().isOk()).andExpect(content().string(""));
        verify(parser).parse(Map.of("object", "whatsapp_business_account"));
        verifyNoInteractions(inbox, dispatcher, telemetry, orchestrator, receipts);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void discardsMessagesWithoutIdentifier(String id) throws Exception {
        when(parser.parse(anyMap())).thenReturn(new IncomingMessage(
                id, PHONE, IncomingMessage.MessageType.TEXT, "hola", null, null, null));
        mvc.perform(signed(BODY)).andExpect(status().isOk());
        verifyNoInteractions(inbox, dispatcher, telemetry, orchestrator, receipts);
    }

    @Test
    void acknowledgesDuplicateWithoutDispatching() throws Exception {
        when(parser.parse(anyMap())).thenReturn(message(IncomingMessage.MessageType.TEXT, "hola", null));
        when(inbox.claim(MESSAGE_ID)).thenReturn(false);
        mvc.perform(signed(BODY)).andExpect(status().isOk());
        verify(inbox).claim(MESSAGE_ID);
        verifyNoInteractions(dispatcher, telemetry, orchestrator, receipts);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TEXT", "INTERACTIVE"})
    void dispatchesTextAndInteractiveMessagesWithTelemetry(String type) throws Exception {
        IncomingMessage message = message(IncomingMessage.MessageType.valueOf(type), "hola", null);
        ChatbotInteraction interaction = prepareDispatch(message);
        mvc.perform(signed(BODY)).andExpect(status().isOk());
        verify(orchestrator).process(message.withTelemetry(interaction));
        verifyNoInteractions(receipts);
        assertPassengerEvent();
    }

    @Test
    void dispatchesImageToPaymentReceiptUseCase() throws Exception {
        prepareDispatch(message(IncomingMessage.MessageType.IMAGE, "caption", "media-123"));
        mvc.perform(signed(BODY)).andExpect(status().isOk());
        verify(receipts).execute(PHONE, "media-123");
        verifyNoInteractions(orchestrator);
        assertPassengerEvent();
    }

    @ParameterizedTest
    @ValueSource(strings = {"OTHER", "IMAGE", "DOCUMENT", "LOCATION"})
    void recordsUnsupportedMessages(String type) throws Exception {
        ChatbotInteraction interaction = prepareDispatch(
                message(IncomingMessage.MessageType.valueOf(type), null, null));
        mvc.perform(signed(BODY)).andExpect(status().isOk());
        verify(telemetry).record(argThat(signal -> signal.sessionId().equals(interaction.sessionId())
                && signal.type() == ChatbotEventType.INPUT_REJECTED
                && signal.reason() == ChatbotReason.UNSUPPORTED_MESSAGE));
        verifyNoInteractions(orchestrator, receipts);
        assertPassengerEvent();
    }

    @Test
    void malformedJsonIsAcknowledgedWithoutProcessing() throws Exception {
        mvc.perform(signed("{invalid-json")).andExpect(status().isOk());
        verifyNoInteractions(parser, inbox, dispatcher, orchestrator, receipts);
    }

    @Test
    void parserFailureIsAcknowledged() throws Exception {
        when(parser.parse(anyMap())).thenThrow(new IllegalArgumentException("Invalid payload"));
        mvc.perform(signed(BODY)).andExpect(status().isOk());
        verifyNoInteractions(inbox, dispatcher, orchestrator, receipts);
    }

    @Test
    void inboxFailureIsAcknowledgedWithoutDispatching() throws Exception {
        when(parser.parse(anyMap())).thenReturn(message(IncomingMessage.MessageType.TEXT, "hola", null));
        when(inbox.claim(MESSAGE_ID)).thenThrow(new IllegalStateException("Inbox unavailable"));
        mvc.perform(signed(BODY)).andExpect(status().isOk());
        verifyNoInteractions(dispatcher, orchestrator, receipts);
    }

    @Test
    void dispatcherFailureIsAcknowledged() throws Exception {
        when(parser.parse(anyMap())).thenReturn(message(IncomingMessage.MessageType.TEXT, "hola", null));
        when(inbox.claim(MESSAGE_ID)).thenReturn(true);
        doThrow(new IllegalStateException("Dispatcher unavailable")).when(dispatcher).dispatch(eq(PHONE), any());
        mvc.perform(signed(BODY)).andExpect(status().isOk());
        verifyNoInteractions(orchestrator, receipts);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", SECRET})
    void productionConfigurationDoesNotPreventStartup(String secret) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        ReflectionTestUtils.setField(controller, "environment", environment);
        ReflectionTestUtils.setField(controller, "appSecret", secret);
        assertDoesNotThrow(controller::validateProductionConfiguration);
    }

    private ChatbotInteraction prepareDispatch(IncomingMessage message) {
        ChatbotInteraction interaction = new ChatbotInteraction(telemetry, UUID.randomUUID(), MESSAGE_ID);
        when(parser.parse(anyMap())).thenReturn(message);
        when(inbox.claim(MESSAGE_ID)).thenReturn(true);
        when(telemetry.begin(PHONE, MESSAGE_ID, false)).thenReturn(interaction);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(1).run();
            return null;
        }).when(dispatcher).dispatch(eq(PHONE), any(Runnable.class));
        return interaction;
    }

    private void assertPassengerEvent() {
        verify(dispatcher).dispatch(eq(PHONE), any(Runnable.class));
        verify(telemetry).begin(PHONE, MESSAGE_ID, false);
        assertEquals(1, events.stream(PassengerMessageReceived.class)
                .filter(event -> PHONE.equals(event.phone())).count());
    }

    private IncomingMessage message(IncomingMessage.MessageType type, String body, String media) {
        return new IncomingMessage(MESSAGE_ID, PHONE, type, body, media, null, null);
    }

    private MockHttpServletRequestBuilder handshake(String token) {
        return get(URL).param("hub.mode", "subscribe").param("hub.verify_token", token)
                .param("hub.challenge", "challenge-123");
    }

    private MockHttpServletRequestBuilder signed(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = "sha256=" + HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        return post(URL).contentType("application/json").header("X-Hub-Signature-256", signature).content(body);
    }
}
