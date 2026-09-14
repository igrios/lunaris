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
