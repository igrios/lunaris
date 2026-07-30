package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;

class WhatsAppTemplateConfigurationTest {

    @Test
    void resolvesLanguagePerApprovedTemplate() {
        assertEquals("en", WhatsAppService.templateLanguageFor("despierta_chofer"));
        assertEquals("en", WhatsAppService.templateLanguageFor("proximo_en_camino"));
        assertEquals("es", WhatsAppService.templateLanguageFor("chofer_asignado"));
        assertEquals("es", WhatsAppService.templateLanguageFor("contacto_pasajero"));
    }

    @Test
    void treatsPendingOrMissingTemplatesAsNonBlocking() {
        assertTrue(WhatsAppService.isTemplateUnavailable(
                "TEMPLATE PROXIMO_EN_CAMINO", 400, """
                        {"error":{"code":132001,"message":"Template does not exist"}}
                        """));
        assertTrue(WhatsAppService.isTemplateUnavailable(
                "TEMPLATE DESPIERTA CHOFER", 404, "Not found"));
        assertFalse(WhatsAppService.isTemplateUnavailable("TEXTO", 404, "Not found"));
    }

    @Test
    void nextPassengerTemplateUsesExactlyTwoApprovedParameters() {
        var parameters = WhatsAppService.proximoEnCaminoParameters("Ana", "Juan");

        assertEquals(2, parameters.size());
        assertEquals("Ana", parameters.get(0));
        assertEquals("Juan", parameters.get(1));
    }

    @Test
    void formatsArgentinePhoneNumbersForMetaCloudApi() {
        assertEquals("5493515551234",
                WhatsAppService.formatMetaPhoneNumber("+54 9 351-555-1234"));
        assertEquals("5493515551234",
                WhatsAppService.formatMetaPhoneNumber("351 555-1234"));
        assertEquals("5493515551234",
                WhatsAppService.formatMetaPhoneNumber("+54 351-555-1234"));
    }

    @Test
    void cleansButDoesNotRewriteOtherInternationalPhoneNumbers() {
        assertEquals("59899123456",
                WhatsAppService.formatMetaPhoneNumber("+598 99 123-456"));
        assertEquals("", WhatsAppService.formatMetaPhoneNumber(null));
    }

    @Test
    void driverRouteSheetUrlContainsDatabaseUuidAndTravelDate() {
        UUID driverId = UUID.fromString("917d74d2-d5de-4ec7-a674-d0d0fb52f99c");

        assertEquals(
                "https://lunaris-backend-nn6s.onrender.com/hoja-ruta"
                        + "?driverId=917d74d2-d5de-4ec7-a674-d0d0fb52f99c&date=2026-08-05",
                WhatsAppService.buildDriverRouteSheetUrl(
                        driverId, LocalDate.of(2026, 8, 5)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void driverTemplateUsesQuickReplyPayloadAtButtonIndexZero() {
        UUID driverId = UUID.fromString("917d74d2-d5de-4ec7-a674-d0d0fb52f99c");
        Map<String, Object> body = Map.of("type", "body", "parameters", List.of());

        var components = WhatsAppService.despiertaChoferComponents(
                body, driverId, LocalDate.of(2026, 8, 5));
        Map<String, Object> button = components.get(1);
        Map<String, Object> parameter =
                ((List<Map<String, Object>>) button.get("parameters")).getFirst();

        assertEquals("button", button.get("type"));
        assertEquals("quick_reply", button.get("sub_type"));
        assertEquals("0", button.get("index"));
        assertEquals("payload", parameter.get("type"));
        assertEquals(
                WhatsAppService.buildDriverRouteSheetUrl(
                        driverId, LocalDate.of(2026, 8, 5)),
                parameter.get("payload"));
        assertFalse(parameter.containsKey("text"));
    }

    @Test
    void driverDispatchSummaryContainsAllPassengerDetailsAndOnlyNavigationLink() {
        Reservation reservation = Reservation.builder()
                .id(UUID.fromString("5ca1ab1e-6806-4a50-94e3-3785b4bf5b68"))
                .passenger(Passenger.builder()
                        .firstName("Ana")
                        .lastName("Pérez")
                        .phone("351 555-1234")
                        .address("https://maps.google.com/?q=-31.1,-62.1")
                        .build())
                .pickupAddress("San Martín 123")
                .passengerCount(2)
                .companionNames("Juan Pérez")
                .build();
        String navigationUrl = "https://www.google.com/maps/dir/?api=1"
                + "&origin=San%20Mart%C3%ADn%20123&destination=Cordoba";

        String summary = WhatsAppService.buildDriverPassengerSummary(
                "Carlos", navigationUrl, List.of(reservation));

        assertTrue(summary.contains("Ana Pérez"));
        assertTrue(summary.contains("San Martín 123"));
        assertTrue(summary.contains("351 555-1234"));
        assertTrue(summary.contains("2 asiento(s)"));
        assertTrue(summary.contains("Acompañantes: Juan Pérez"));
        assertTrue(summary.contains(navigationUrl));
        assertFalse(summary.contains("/hoja-ruta"));
        assertFalse(summary.contains("lunaris-backend"));
    }

    @Test
    void driverDispatchFallsBackToPassengerMapLocation() {
        Reservation reservation = Reservation.builder()
                .passenger(Passenger.builder()
                        .firstName("Ana")
                        .lastName("Pérez")
                        .address("https://maps.google.com/?q=-31.1,-62.1")
                        .build())
                .build();

        String summary = WhatsAppService.buildDriverPassengerSummary(
                "Carlos", "https://www.google.com/maps/dir/?api=1", List.of(reservation));

        assertTrue(summary.contains("https://maps.google.com/?q=-31.1,-62.1"));
    }
}
