package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
}
