package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AgendaScheduleFilterTemplateTest {

    @Test
    void includesManuallyScheduledReturnTimeInAgendaFilters() {
        var reservation = com.lunaris.ansenuza.domain.model.Reservation.builder()
                .pickupLocality("Córdoba").destination("Morteros")
                .routeDirection("VUELTA").departureSchedule("16:45 PM").build();
        var schedules = AgendaViewController.agendaSchedules(java.util.List.of("14:00", "17:30"),
                java.util.List.of(reservation), true);
        org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of("14:00", "16:45", "17:30"), schedules);
    }

    @Test
    void switchesScheduleOptionsWhenRouteDirectionChanges() throws IOException {
        String template = Files.readString(
                Path.of("src/main/resources/templates/agenda-day.html"));

        assertTrue(template.contains("id=\"agendaDirection\""));
        assertTrue(template.contains("id=\"agendaSchedule\""));
        assertTrue(template.contains("returnAgendaSchedules"));
        assertTrue(template.contains("12:00"));
        assertTrue(template.contains("14:00"));
        assertTrue(template.contains("16:00"));
        assertTrue(template.contains("17:30"));
        assertTrue(template.contains("addEventListener(\"change\", updateAgendaSchedules)"));
    }
}
