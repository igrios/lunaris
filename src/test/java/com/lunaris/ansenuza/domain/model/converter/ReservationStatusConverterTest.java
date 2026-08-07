package com.lunaris.ansenuza.domain.model.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReservationStatusConverterTest {

    private final ReservationStatusConverter converter = new ReservationStatusConverter();

    @Test
    void mapsNullBlankAndUnknownDatabaseValuesToPending() {
        assertEquals("PENDING", converter.convertToEntityAttribute(null));
        assertEquals("PENDING", converter.convertToEntityAttribute("  "));
        assertEquals("PENDING", converter.convertToEntityAttribute("LEGACY_UNKNOWN"));
    }

    @Test
    void normalizesKnownStatusWithoutThrowing() {
        assertEquals("CONFIRMED", converter.convertToEntityAttribute(" confirmed "));
        assertEquals("PARTIALLY_COMPLETED",
                converter.convertToEntityAttribute("partially_completed"));
        assertEquals("RECEIPT_UPLOADED",
                converter.convertToEntityAttribute("receipt_uploaded"));
        assertEquals("RESERVED", converter.convertToEntityAttribute("reserved"));
        assertEquals("REJECTED", converter.convertToEntityAttribute("rejected"));
    }
}
