package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.format.annotation.DateTimeFormat;

class AgendaDateFormatContractTest {

    @Test
    void detailEndpointAcceptsIsoAndLegacyAgendaDateFormats() throws Exception {
        Method endpoint = AgendaViewController.class.getMethod(
                "dayAgenda", LocalDate.class, String.class,
                com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.RouteDirection.class,
                org.springframework.ui.Model.class);
        DateTimeFormat format = java.util.Arrays.stream(endpoint.getParameterAnnotations()[0])
                .filter(DateTimeFormat.class::isInstance)
                .map(DateTimeFormat.class::cast)
                .findFirst()
                .orElseThrow();

        assertEquals(DateTimeFormat.ISO.DATE, format.iso());
        assertArrayEquals(new String[] {"dd/MM/yyyy", "d/M/yy"}, format.fallbackPatterns());
    }
}
