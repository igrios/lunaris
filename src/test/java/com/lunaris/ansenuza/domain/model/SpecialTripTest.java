package com.lunaris.ansenuza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SpecialTripTest {
    @Test
    void createsAndNormalizesAValidTrip() {
        SpecialTrip trip = SpecialTrip.create("  Oktoberfest ", null, " Córdoba ", "Villa General Belgrano",
                LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 11), new BigDecimal("75000.00"),
                40, " https://example.com/trip.jpg ", true, LocalDateTime.of(2026, 8, 6, 10, 0));

        assertThat(trip.title()).isEqualTo("Oktoberfest");
        assertThat(trip.origin()).isEqualTo("Córdoba");
        assertThat(trip.imageUrl()).isEqualTo("https://example.com/trip.jpg");
    }

    @Test
    void rejectsAnInvalidDateRange() {
        assertThatThrownBy(() -> SpecialTrip.create("Viaje", null, null, null,
                LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 9), BigDecimal.TEN, 1,
                null, true, LocalDateTime.now()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("fecha de fin");
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThatThrownBy(() -> SpecialTrip.create("Viaje", null, null, null,
                LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 11), BigDecimal.TEN, 0,
                null, true, LocalDateTime.now()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("capacidad máxima");
    }
}
