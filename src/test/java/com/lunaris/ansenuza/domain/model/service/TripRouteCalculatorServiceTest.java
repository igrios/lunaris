package com.lunaris.ansenuza.domain.model.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.BookingDemand;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.RouteDirection;
import com.lunaris.ansenuza.domain.model.Reservation;
import java.util.List;
import org.junit.jupiter.api.Test;

class TripRouteCalculatorServiceTest {
    private final TripRouteCalculatorService calculator = new TripRouteCalculatorService();

    @org.junit.jupiter.api.Test
    void recognizesCordobaVariantsInBothManifestDirections() {
        Reservation outbound = Reservation.builder()
                .pickupLocality("Arrufó").destination("Aeropuerto Córdoba")
                .departureSchedule("03:00 AM").reservationCode("ARR-COR-001-IDA").build();
        Reservation returned = Reservation.builder()
                .pickupLocality("Terminal Cordoba").destination("Arrufó")
                .departureSchedule("08:00 AM").reservationCode("ARR-COR-001-VUELTA").build();

        assertThat(calculator.matchesManifest(outbound, RouteDirection.OUTBOUND, "03:00")).isTrue();
        assertThat(calculator.matchesManifest(returned, RouteDirection.RETURN, "08:00")).isTrue();
        assertThat(calculator.matchesManifest(outbound, RouteDirection.RETURN, "03:00")).isFalse();
        assertThat(calculator.matchesManifest(returned, RouteDirection.OUTBOUND, "08:00")).isFalse();
    }

    @Test
    void recognizesScheduledOpenReturnWithVtaBlockCodeByGeography() {
        Reservation scheduledReturn = Reservation.builder()
                .reservationCode("VTA-BLK-f4ad-030")
                .pickupLocality("Aeropuerto Córdoba")
                .destination("San Guillermo")
                .departureSchedule("08:00 AM")
                .build();

        assertThat(calculator.matchesManifest(
                scheduledReturn, RouteDirection.RETURN, "08:00")).isTrue();
        assertThat(calculator.matchesManifest(
                scheduledReturn, RouteDirection.OUTBOUND, "08:00")).isFalse();
    }

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

    @Test
    void separatesOutboundAndReturnLegsForSameGroupDateAndShift() {
        Reservation outbound = Reservation.builder().reservationCode("ARR-COR-001-IDA")
                .pickupLocality("Arrufó").destination("Córdoba")
                .departureSchedule("03:00 AM").build();
        Reservation returned = Reservation.builder().reservationCode("ARR-COR-001-VUELTA")
                .pickupLocality("Córdoba").destination("Arrufó")
                .departureSchedule("03:00 AM").build();

        assertThat(calculator.matchesManifest(outbound, RouteDirection.OUTBOUND, "03:00")).isTrue();
        assertThat(calculator.matchesManifest(returned, RouteDirection.OUTBOUND, "03:00")).isFalse();
        assertThat(calculator.matchesManifest(returned, RouteDirection.RETURN, "03:00")).isTrue();
        assertThat(calculator.matchesManifest(outbound, RouteDirection.RETURN, "03:00")).isFalse();
    }

    @Test
    void excludesAnotherShiftFromManifest() {
        Reservation outbound = Reservation.builder().reservationCode("ARR-COR-001-IDA")
                .pickupLocality("Arrufó").destination("Córdoba")
                .departureSchedule("08:00 AM").build();

        assertThat(calculator.matchesManifest(outbound, RouteDirection.OUTBOUND, "03:00")).isFalse();
    }
}
