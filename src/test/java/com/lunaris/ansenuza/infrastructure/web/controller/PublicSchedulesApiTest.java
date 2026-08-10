package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PublicSchedulesApiTest {

    @Test
    void roundTripReturnsOnlyCleanAvailableOutgoingBlocks() throws Exception {
        PricingAndScheduleService schedules = mock(PricingAndScheduleService.class);
        LocalDate travelDate = LocalDate.of(2026, 8, 20);
        when(schedules.departureSchedules()).thenReturn(List.of("03:00 AM", "08:00 AM"));
        when(schedules.availableSeats(travelDate, "03:00 AM")).thenReturn(10);
        when(schedules.availableSeats(travelDate, "08:00 AM")).thenReturn(0);
        SchedulesController controller = new SchedulesController(schedules);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/schedules")
                        .param("pickupLocality", "Morteros")
                        .param("destination", "Córdoba")
                        .param("travelDate", "2026-08-20")
                        .param("roundTrip", "true")
                        .param("openReturn", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("03:00"))
                .andExpect(jsonPath("$[0].departureTime").value("03:00"))
                .andExpect(jsonPath("$[0].availableSeats").value(10))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    void acceptsArgentineTravelDateFormat() throws Exception {
        PricingAndScheduleService schedules = mock(PricingAndScheduleService.class);
        LocalDate travelDate = LocalDate.of(2026, 8, 20);
        when(schedules.departureSchedules()).thenReturn(List.of("03:00 AM", "08:00 AM"));
        when(schedules.availableSeats(travelDate, "03:00 AM")).thenReturn(10);
        when(schedules.availableSeats(travelDate, "08:00 AM")).thenReturn(9);
        SchedulesController controller = new SchedulesController(schedules);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/schedules")
                        .param("pickupLocality", "Morteros")
                        .param("destination", "Córdoba")
                        .param("travelDate", "20/08/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departureTime").value("03:00"))
                .andExpect(jsonPath("$[1].departureTime").value("08:00"));

        verify(schedules).availableSeats(travelDate, "03:00 AM");
    }
}
