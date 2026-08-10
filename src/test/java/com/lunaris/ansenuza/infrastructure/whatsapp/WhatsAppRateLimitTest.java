package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class WhatsAppRateLimitTest {

    @Test
    void retriesOnceAfterMetaPairRateLimit() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        List<Long> sleeps = new ArrayList<>();
        WhatsAppService service = service(restTemplate, () -> 0L, sleeps::add);
        HttpClientErrorException rateLimit = pairRateLimitException();
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(rateLimit)
                .thenReturn(ResponseEntity.ok("{}"));

        boolean sent = service.trySendMessage("3515551234", "Estado actualizado");

        assertTrue(sent);
        assertEquals(List.of(1_000L), sleeps);
        verify(restTemplate, times(2)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void throttlesConsecutiveCallsToSameRecipient() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{}"));
        AtomicLong nanos = new AtomicLong();
        List<Long> sleeps = new ArrayList<>();
        WhatsAppService service = service(restTemplate, nanos::get, millis -> {
            sleeps.add(millis);
            nanos.addAndGet(millis * 1_000_000L);
        });

        service.trySendMessage("3515551234", "Primer estado");
        service.trySendMessage("3515551234", "Segundo estado");

        assertEquals(List.of(300L), sleeps);
    }

    @Test
    void recognizesOAuthPairRateLimitPayloadOnlyForBadRequest() {
        assertTrue(WhatsAppService.isPairRateLimit(pairRateLimitException()));
    }

    private WhatsAppService service(RestTemplate restTemplate,
            java.util.function.LongSupplier nanoTime, WhatsAppService.Sleeper sleeper) {
        WhatsAppService service = new WhatsAppService(restTemplate, nanoTime, sleeper);
        ReflectionTestUtils.setField(service, "phoneNumberId", "phone-id");
        ReflectionTestUtils.setField(service, "accessToken", "token");
        return service;
    }

    private HttpClientErrorException pairRateLimitException() {
        return HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                org.springframework.http.HttpHeaders.EMPTY,
                """
                        {"error":{"message":"Pair Rate Limit Hit","type":"OAuthException","code":131056}}
                        """.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);
    }
}
