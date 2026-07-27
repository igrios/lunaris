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
}
