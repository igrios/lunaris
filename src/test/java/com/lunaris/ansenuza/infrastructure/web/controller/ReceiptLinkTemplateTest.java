package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ReceiptLinkTemplateTest {

    @Test
    void agendaViewsExposeReceiptInANewTab() throws IOException {
        assertReceiptLink("/templates/agenda-day.html", "reservation.paymentReceiptUrl");
        assertReceiptLink("/templates/reservations-grid.html", "r.paymentReceiptUrl");
    }

    private void assertReceiptLink(String resource, String receiptProperty) throws IOException {
        try (var input = getClass().getResourceAsStream(resource)) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains(receiptProperty));
            assertTrue(template.contains("📄 Ver Comprobante"));
            assertTrue(template.contains("target=\"_blank\""));
        }
    }
}
