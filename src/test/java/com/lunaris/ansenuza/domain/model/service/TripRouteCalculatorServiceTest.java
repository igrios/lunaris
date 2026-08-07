package com.lunaris.ansenuza.domain.model.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.BookingDemand;
import java.util.List;
import org.junit.jupiter.api.Test;

class TripRouteCalculatorServiceTest {
    private final TripRouteCalculatorService calculator = new TripRouteCalculatorService();

    @Test
    void startsAtMorterosWhenNorthernLocalitiesIncludingSanGuillermoHaveNoPassengers() {
        var result = calculator.calculate(List.of(
                new BookingDemand("Arrufó", 0),
                new BookingDemand("Villa Trinidad", 0),
                new BookingDemand("San Guillermo", 0),
                new BookingDemand("Morteros", 2),
                new BookingDemand("Brinkmann", 1)));

        assertThat(result.effectiveOrigin()).isEqualTo("Morteros");
        assertThat(result.skippedLocalities())
                .containsExactly("Arrufó", "Villa Trinidad", "San Guillermo", "Suardi");
        assertThat(result.message())
                .contains("Cabecera del día recalculada: Morteros", "San Guillermo");
    }

    @Test
    void keepsSanGuillermoAsHeadWhenItHasAtLeastOnePassenger() {
        var result = calculator.calculate(List.of(
                new BookingDemand("Arrufo", 0),
                new BookingDemand("Villa Trinidad", 0),
                new BookingDemand("San Guillermo", 1),
                new BookingDemand("Morteros", 2)));

        assertThat(result.effectiveOrigin()).isEqualTo("San Guillermo");
        assertThat(result.skippedLocalities()).containsExactly("Arrufó", "Villa Trinidad");
    }

    @Test
    void returnsNoOriginWhenTheScheduleHasNoPassengerDemand() {
        var result = calculator.calculate(List.of(new BookingDemand("Morteros", 0)));

        assertThat(result.effectiveOrigin()).isNull();
        assertThat(result.message()).contains("Sin pasajeros confirmados");
    }
}
