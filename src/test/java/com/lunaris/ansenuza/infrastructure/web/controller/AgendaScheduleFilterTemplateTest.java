package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AgendaScheduleFilterTemplateTest {

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
