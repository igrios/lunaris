package com.lunaris.ansenuza.application.usecase;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FreeTextReservationExtractorTest {
    @Test void recognizesPhoneAndStreetOnSeparateLines() {
        var result = FreeTextReservationExtractor.extract("3511234567\nBv. Belgrano 450");
        assertThat(result.phone()).isEqualTo("3511234567");
        assertThat(result.pickupAddress()).isEqualTo("Bv. Belgrano 450");
    }

    @Test void ambiguousMessagesDoNotBecomeAddresses() {
        for (String message : new String[] {"hola", "2030-01-02", "3511234567", "Somos 4 pasajeros", "https://example.com/123"}) {
            assertThat(FreeTextReservationExtractor.extract(message).pickupAddress()).as(message).isNull();
        }
    }
}
