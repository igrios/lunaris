package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;

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
    void driverRouteSheetUrlContainsDatabaseUuidAndTravelDate() {
        UUID driverId = UUID.fromString("917d74d2-d5de-4ec7-a674-d0d0fb52f99c");

        assertEquals(
                "https://lunaris-backend-nn6s.onrender.com/hoja-ruta"
                        + "?driverId=917d74d2-d5de-4ec7-a674-d0d0fb52f99c&date=2026-08-05",
                WhatsAppService.buildDriverRouteSheetUrl(
                        driverId, LocalDate.of(2026, 8, 5)));
    }
}
