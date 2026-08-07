package com.lunaris.ansenuza.domain.model.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lunaris.ansenuza.domain.model.Reservation;
import org.junit.jupiter.api.Test;

class TravelStatusConverterTest {

    private final TravelStatusConverter converter = new TravelStatusConverter();

    @Test
    void mapsConfirmedReturnWindowStatusWithoutDowngradingIt() {
        assertEquals(Reservation.TravelStatus.CONFIRMED,
                converter.convertToEntityAttribute("CONFIRMED"));
    }

    @Test
    void safelyMapsNullEmptyAndValidDatabaseValues() {
        assertEquals(Reservation.TravelStatus.PENDING,
                converter.convertToEntityAttribute(null));
        assertEquals(Reservation.TravelStatus.PENDING,
                converter.convertToEntityAttribute("  "));
        assertEquals(Reservation.TravelStatus.COMPLETED,
                converter.convertToEntityAttribute("completed"));
    }
}
