package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ThymeleafRoutesTest {

    @Test
    void homeRedirectsToAdminDashboardAndLoginViewExists() {
        HomeController controller = new HomeController();

        assertEquals("redirect:/admin/dashboard", controller.home());
        assertEquals("login", controller.login());
        assertTrue(Files.isRegularFile(
                Path.of("src/main/resources/templates/login.html")));
        assertTrue(Files.isRegularFile(
                Path.of("src/main/resources/templates/dashboard.html")));
        assertTrue(Files.isRegularFile(
                Path.of("src/main/resources/templates/passengers.html")));
    }

    @Test
    void driverFormUsesBoundObjectActionAndCsrfToken() throws Exception {
        String template = Files.readString(
                Path.of("src/main/resources/templates/choferes.html"));

        assertTrue(template.contains("th:action=\"@{/choferes/guardar}\""));
        assertTrue(template.contains("th:disabled=\"${driver.id == null}\""));
        assertTrue(template.contains("th:object=\"${driver}\""));
        assertTrue(template.contains("th:name=\"${_csrf.parameterName}\""));
        assertTrue(template.contains("th:value=\"${_csrf.token}\""));
    }

    @Test
    void passengersPanelRendersDynamicSpecialEventBadges() throws Exception {
        String template = Files.readString(
                Path.of("src/main/resources/templates/passengers.html"));

        assertTrue(template.contains("entry.eventType == 'AIRBAG_CORDOBA'"));
        assertTrue(template.contains("entry.eventType == 'AIRBAG'"));
        assertTrue(template.contains("'bg-purple'"));
        assertTrue(template.contains("entry.eventType == 'POPE_VISIT'"));
        assertTrue(template.contains("'bg-danger'"));
        assertTrue(template.contains("eventLabels[entry.eventType]"));
    }

    @Test
    void agendaDetailLinkAlwaysSerializesDateAsIso() throws Exception {
        String template = Files.readString(
                Path.of("src/main/resources/templates/agenda.html"));

        assertTrue(template.contains(
                "date=${#temporals.format(day.date, 'yyyy-MM-dd')}"));
    }
}
