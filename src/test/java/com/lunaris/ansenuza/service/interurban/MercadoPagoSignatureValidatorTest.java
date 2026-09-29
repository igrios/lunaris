package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class MercadoPagoSignatureValidatorTest {
    private final Instant now = Instant.parse("2026-09-20T18:00:00Z");
    private final MercadoPagoSignatureValidator validator = new MercadoPagoSignatureValidator(
            MercadoPagoWebhookServiceTest.SECRET, Clock.fixed(now, ZoneOffset.UTC), Duration.ofHours(24));

    @Test void acceptsSecondsAndMillisecondsWithoutChangingSignedTimestamp() throws Exception {
        for (String ts : new String[]{Long.toString(now.getEpochSecond()), Long.toString(now.toEpochMilli())}) {
            validator.validate("12345", "request", MercadoPagoWebhookServiceTest.signature("12345", "request", ts));
        }
    }

    @Test void rejectsTampering() throws Exception {
        String signature = MercadoPagoWebhookServiceTest.signature("12345", "request", Long.toString(now.getEpochSecond()));
        assertThrows(InterurbanPaymentException.class, () -> validator.validate("54321", "request", signature));
        assertThrows(InterurbanPaymentException.class, () -> validator.validate("12345", "other-request", signature));
    }

    @Test void rejectsExpiredFutureAndDuplicateTimestamps() throws Exception {
        for (Instant time : new Instant[]{now.minusSeconds(86401), now.plusSeconds(61)}) {
            String signature = MercadoPagoWebhookServiceTest.signature("12345", "request", Long.toString(time.getEpochSecond()));
            assertThrows(InterurbanPaymentException.class, () -> validator.validate("12345", "request", signature));
        }
        String signature = MercadoPagoWebhookServiceTest.signature("12345", "request", Long.toString(now.getEpochSecond()));
        assertThrows(InterurbanPaymentException.class, () -> validator.validate("12345", "request", signature + ",ts=1"));
    }
}
