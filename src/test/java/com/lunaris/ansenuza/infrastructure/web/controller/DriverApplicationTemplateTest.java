package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DriverApplicationTemplateTest {

    @Test
    void exposesCompanyVehicleAndDocumentLinks() throws IOException {
        try (var input = getClass().getResourceAsStream("/templates/admin/postulaciones.html")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("<th>Documentación</th>"));
            assertTrue(template.contains("app.greenCardFileUrl"));
            assertTrue(template.contains("app.insuranceFileUrl"));
            assertTrue(template.contains("app.criminalRecordFileUrl"));
            assertTrue(template.contains("btn btn-sm btn-outline-primary\">Cédula"));
            assertTrue(template.contains("btn btn-sm btn-outline-info\">Seguro"));
            assertTrue(template.contains("btn btn-sm btn-outline-warning\">Antecedentes"));
            assertTrue(template.contains("Sin adjuntos"));
            assertTrue(template.contains("Unidad de Empresa"));
        }
    }
}
