package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class WhatsAppServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void otpIsSentAsTemplateWithCodeInBodyComponent() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new org.springframework.http.ResponseEntity<>("{}", HttpStatus.OK));
        WhatsAppService service = new WhatsAppService(restTemplate, () -> 0L, millis -> { });
        ReflectionTestUtils.setField(service, "phoneNumberId", "phone-id");
        ReflectionTestUtils.setField(service, "accessToken", "token");

        service.sendOtpMessage("+54 9 351-555-1234", "1234");

        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq("https://graph.facebook.com/v18.0/phone-id/messages"),
                requestCaptor.capture(), eq(String.class));
        Map<String, Object> payload = requestCaptor.getValue().getBody();
        Assertions.assertNotNull(payload);
        Assertions.assertEquals("template", payload.get("type"));
        Assertions.assertEquals("5493515551234", payload.get("to"));

        Map<String, Object> template = (Map<String, Object>) payload.get("template");
        Assertions.assertEquals("account_creation_confirmation_3", template.get("name"));
        Assertions.assertEquals(Map.of("code", "es"), template.get("language"));
        List<Map<String, Object>> components =
                (List<Map<String, Object>>) template.get("components");
        Assertions.assertEquals("body", components.getFirst().get("type"));
        List<Map<String, Object>> parameters =
                (List<Map<String, Object>>) components.getFirst().get("parameters");
        Assertions.assertEquals(List.of(
                Map.of("type", "text", "text", "tu cuenta"),
                Map.of("type", "text", "text", "1234")), parameters);
    }

    @Test
    void pairRateLimitRetryFailureIsHandledWithoutEscaping() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        HttpClientErrorException rateLimit = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                """
                        {"error":{"type":"OAuthException","code":131056}}
                        """.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(rateLimit);
        WhatsAppService service = new WhatsAppService(restTemplate, () -> 0L, millis -> { });
        ReflectionTestUtils.setField(service, "phoneNumberId", "phone-id");
        ReflectionTestUtils.setField(service, "accessToken", "token");

        boolean[] result = new boolean[1];
        assertDoesNotThrow(() -> result[0] = service.trySendMessage("3515551234", "Estado"));
        assertFalse(result[0]);
    }
}
