package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;

class AgendaPassengerBoardTemplateTest {
    @Test
    void specialAgendaEscapesFreeTextAndEnablesInvoiceOnlyAfterPayment() throws Exception {
        String template = Files.readString(Path.of("src/main/resources/templates/agenda-day.html"));
        int start = template.indexOf("<section th:if=\"${specialTrips");
        String section = template.substring(start, template.indexOf("</section>", start) + "</section>".length());
        Reservation reservation = Reservation.builder().id(java.util.UUID.randomUUID())
                .passenger(Passenger.builder().firstName("Ana").lastName("Pérez").build())
                .originCustom("De <script>Suardi</script>").destinationCustom("Alta Gracia")
                .passengerCount(3).customPrice(new java.math.BigDecimal("86000.00"))
                .amount(new java.math.BigDecimal("258000.00"))
                .paymentVerified(false).build();
        Context context = new Context();
        context.setVariable("specialTrips", List.of(com.lunaris.ansenuza.infrastructure.web.dto.reservation.SpecialAgendaTripView.from(reservation)));
        SpringTemplateEngine engine = new SpringTemplateEngine();
        String pending = engine.process(section, context);
        assertTrue(pending.contains("&lt;script&gt;Suardi&lt;/script&gt;"));
        assertTrue(pending.contains("Pendiente de pago"));
        assertTrue(pending.contains("Registrar pago"));
        assertTrue(pending.contains("$258.000,00"));
        org.junit.jupiter.api.Assertions.assertFalse(pending.contains("$86.000,00"));
        reservation.setPaymentVerified(true);
        String paid = engine.process(section, context);
        assertTrue(paid.contains("Pagado"));
        assertTrue(paid.contains("Habilitada"));
        org.junit.jupiter.api.Assertions.assertFalse(paid.contains("Registrar pago"));
        reservation.setInvoiceIssued(true);
        assertTrue(engine.process(section, context).contains("Emitida"));
    }

    @Test
    void rendersBothLegsWithEscapedModalDataAndEmptyState() throws Exception {
        String template = Files.readString(Path.of("src/main/resources/templates/agenda-day.html"));
        String board = template.substring(template.indexOf("<section id=\"agendaBoard\""),
                template.indexOf("<div id=\"agendaList\""));
        Reservation reservation = new Reservation();
        reservation.setPassenger(Passenger.builder().firstName("Ana").lastName("Pérez")
                .phone("+5493511234567").build());
        reservation.setNotes("Retirar en <puerta> & llamar");
        reservation.setDepartureSchedule("17:30 HS");
        reservation.setPassengerCount(2);
        reservation.setPaymentVerified(true);
        Context context = new Context();
        context.setVariable("outboundReservations", List.of(reservation));
        context.setVariable("returnReservations", List.of(reservation));
        SpringTemplateEngine engine = new SpringTemplateEngine();
        String html = engine.process(board, context);
        for (String column : List.of("colIda", "colVuelta")) {
            String leg = html.substring(html.indexOf("id=\"" + column + "\""));
            assertTrue(leg.contains("data-passenger-name=\"Ana Pérez\""));
            assertTrue(leg.contains("data-notes=\"Retirar en &lt;puerta&gt; &amp; llamar\""));
            assertTrue(leg.contains("2 asientos"));
            assertTrue(leg.contains("Verificado"));
        }
        context.setVariable("returnReservations", List.of());
        assertTrue(engine.process(board, context).contains("Sin pasajeros para este tramo."));
    }
}
