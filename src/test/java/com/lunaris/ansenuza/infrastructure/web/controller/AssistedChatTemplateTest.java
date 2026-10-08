package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import static org.junit.jupiter.api.Assertions.*;

class AssistedChatTemplateTest {
    @Test void prefilledValuesAndManualPermissionReachRenderedDom() throws Exception {
        String template = Files.readString(Path.of("src/main/resources/templates/admin/chat-room.html"));
        String form = template.substring(template.indexOf("<form id=\"formReservaManual\""),
                template.indexOf("</form>", template.indexOf("<form id=\"formReservaManual\"")) + 7)
                .replace("th:action=\"@{/admin/bot/monitor/cargar-reserva}\"", "");
        String composer = template.substring(template.indexOf("<input type=\"text\" id=\"messageInput\""),
                template.indexOf("</button>", template.indexOf("<input type=\"text\" id=\"messageInput\"")) + 9);
        Context context = new Context();
        context.setVariable("phone", "5493511234567");
        context.setVariable("chatCanSend", true);
        context.setVariable("passenger", Passenger.builder().firstName("Ana").lastName("Pérez").build());
        context.setVariable("assistedData", ConversationSession.builder().passengerCount(7)
                .pickupLocality("Aeropuerto").destination("Miramar").pickupAddress("Puerta <norte>").travelDate(LocalDate.of(2026, 10, 10))
                .roundTrip(true).requiresInvoice(true).companionNames("Luis & María").build());
        String html = new SpringTemplateEngine().process(form + composer, context);
        assertTrue(html.contains("value=\"5493511234567\""));
        assertTrue(html.contains("value=\"Ana\""));
        assertTrue(html.contains("value=\"Pérez\""));
        assertTrue(html.contains("value=\"7\""));
        assertTrue(html.contains("value=\"Aeropuerto\""));
        assertTrue(html.contains("value=\"Miramar\""));
        assertTrue(html.contains("value=\"2026-10-10\""));
        assertTrue(html.contains("Puerta &lt;norte&gt;"));
        assertTrue(html.contains("Luis &amp; María"));
        assertFalse(html.contains("disabled=\"disabled\""));
        String script = template.substring(template.indexOf("<script th:inline=\"javascript\">"), template.indexOf("const socket =")) + "</script>";
        String renderedScript = new SpringTemplateEngine().process(script, context);
        assertTrue(renderedScript.contains("Aeropuerto"));
        assertTrue(renderedScript.contains("Miramar"));
        assertTrue(renderedScript.contains("let chatCanSend = true"));
        context.setVariable("chatCanSend", false);
        assertTrue(new SpringTemplateEngine().process(composer, context).contains("disabled=\"disabled\""));
    }
}
