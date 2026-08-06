package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.GetSpecialTripsQuery;
import com.lunaris.ansenuza.infrastructure.web.mapper.SpecialTripWebMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SpecialTripPublicControllerTest {
    @Test
    void returnsOnlyTheTripsProvidedByTheActiveQuery() throws Exception {
        GetSpecialTripsQuery query = mock(GetSpecialTripsQuery.class);
        var trip = new SpecialTrip(7L, "Oktoberfest", "Salida especial", "Córdoba",
                "Villa General Belgrano", LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 11),
                new BigDecimal("75000.00"), 40, null, true, LocalDateTime.of(2026, 8, 6, 12, 0));
        when(query.getActive()).thenReturn(List.of(trip));
        var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var mvc = MockMvcBuilders.standaloneSetup(new SpecialTripPublicController(query, new SpecialTripWebMapper()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper)).build();

        mvc.perform(get("/api/special-trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].title").value("Oktoberfest"))
                .andExpect(jsonPath("$[0].price").value(75000.00));
    }
}
