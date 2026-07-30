package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.Map;

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
}
