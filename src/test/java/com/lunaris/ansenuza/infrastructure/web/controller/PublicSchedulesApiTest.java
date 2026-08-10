package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.dto.ScheduleDto;
import com.lunaris.ansenuza.application.usecase.ScheduleService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PublicSchedulesApiTest {

    @Test
    void roundTripReturnsOnlyCleanAvailableOutgoingBlocks() throws Exception {
        ScheduleService schedules = mock(ScheduleService.class);
        LocalDate travelDate = LocalDate.of(2026, 8, 20);
        when(schedules.getSchedulesForWeb("Morteros", travelDate)).thenReturn(List.of(
                new ScheduleDto("03:00", "03:00", 10, true),
                new ScheduleDto("08:00", "08:00", 0, false)));
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
        ScheduleService schedules = mock(ScheduleService.class);
        LocalDate travelDate = LocalDate.of(2026, 8, 20);
        when(schedules.getSchedulesForWeb("Morteros", travelDate)).thenReturn(List.of(
                new ScheduleDto("03:00", "03:00", 10, true),
                new ScheduleDto("08:00", "08:00", 9, true)));
        SchedulesController controller = new SchedulesController(schedules);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/schedules")
                        .param("pickupLocality", "Morteros")
                        .param("destination", "Córdoba")
                        .param("travelDate", "20/08/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departureTime").value("03:00"))
                .andExpect(jsonPath("$[1].departureTime").value("08:00"));

    }
}
