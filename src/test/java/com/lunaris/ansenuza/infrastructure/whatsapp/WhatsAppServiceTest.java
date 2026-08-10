package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class WhatsAppServiceTest {

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
