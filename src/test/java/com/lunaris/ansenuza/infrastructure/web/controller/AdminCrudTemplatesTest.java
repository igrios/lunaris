package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AdminCrudTemplatesTest {
    @Test
    void faresTemplateContainsCrudFormsAndCsrfTokens() throws Exception {
        String template = Files.readString(Path.of("src/main/resources/templates/admin/fares.html"));

        assertThat(template).contains("Nueva Localidad / Tarifa", "Editar", "Eliminar");
        assertThat(template).contains("th:action=\"@{/admin/fares}\"");
        assertThat(template).contains("th:name=\"${_csrf.parameterName}\"");
        assertThat(template).contains("th:value=\"${_csrf.token}\"");
    }

    @Test
    void newsTemplateContainsSpecialTripsPreviewToggleAndCsrf() throws Exception {
        String template = Files.readString(Path.of("src/main/resources/templates/admin/novedades.html"));

        assertThat(template).contains("Viajes especiales", "cloudinary-input", "trip-status");
        assertThat(template).contains("/admin/novedades/viajes", "name=\"_csrf\"");
    }
}
