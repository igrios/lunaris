package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HojaRutaTemplateTest {

    @Test
    void liveChatUsesFullPhoneAndInternalTakeover() throws IOException {
        try (var input = getClass().getResourceAsStream("/templates/admin/hoja-ruta.html")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(template.contains("th:text=\"${sesion.phoneNumber}\""));
            assertTrue(template.contains("@{/admin/chat/{phone}/takeover(phone=${sesion.phoneNumber})}"));
            assertTrue(!template.contains("web.whatsapp.com"));
            assertTrue(!template.contains("th:text=\"${sesion.id}\""));
        }
    }

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
    void routeSheetStaysLightweightAndLinksToExternalNavigation() throws IOException {
        try (var input = getClass().getResourceAsStream(
                "/templates/admin/hoja-ruta.html")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("📍 Abrir Navegación GPS"));
            assertTrue(template.contains("th:href=\"${navigationUrl"));
            assertTrue(template.contains("#uris.escapeQueryParam(mapQuery)"));
            assertTrue(!template.contains("leaflet"));
            assertTrue(!template.contains("id=\"route-map\""));
        }
    }
}
