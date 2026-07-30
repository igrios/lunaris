package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HojaRutaTemplateTest {

    @Test
    void onboardButtonUsesPutWithExactTravelStatusPayload() throws IOException {
        try (var input = getClass().getResourceAsStream(
                "/templates/admin/hoja-ruta.html")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("method: \"PUT\""));
            assertTrue(template.contains(
                    "JSON.stringify({ travelStatus: \"ONBOARD\" })"));
            assertTrue(template.contains(
                    "/api/reservations/${reservationId}/travel-status"));
        }
    }

    @Test
    void routeMapBindsReservationFieldsAndProvidesFallbacks() throws IOException {
        try (var input = getClass().getResourceAsStream(
                "/templates/admin/hoja-ruta.html")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("r.pickupAddress || r.address || ''"));
            assertTrue(template.contains("r.pickupLocality || r.locality || r.origin || ''"));
            assertTrue(template.contains("r.destination || 'Córdoba'"));
            assertTrue(template.contains("r.passengerPhone || (r.passenger ? r.passenger.phone : '')"));
            assertTrue(template.contains("routeMap.invalidateSize()"));
            assertTrue(template.contains("drawFallbackPolyline(uniqueCoordinates)"));
            assertTrue(template.contains(
                    "`${waypoint.address || ''}, ${waypoint.locality || 'Córdoba'}, Argentina`"));
            assertTrue(template.contains("encodeURIComponent(fullAddress(waypoint))"));
            assertTrue(template.contains("https://www.google.com/maps/dir/?${parameters.toString()}"));
            assertTrue(template.contains("destination: 'Cordoba'"));
            assertTrue(template.contains("waypoints: pickupLocations.join('|')"));
            assertTrue(template.contains("validCoordinates(waypoint.latitude, waypoint.longitude)"));
            assertTrue(template.contains("L.marker(directCoordinates)"));
            assertTrue(template.contains("#uris.escapeQueryParam(mapQuery)"));
        }
    }
}
