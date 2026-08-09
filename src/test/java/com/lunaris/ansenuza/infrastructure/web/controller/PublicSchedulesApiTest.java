package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.application.usecase.SubmitDriverApplicationUseCase;
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
        when(schedules.availableDepartureSchedules("Morteros", "Córdoba", travelDate))
                .thenReturn(List.of("03:00 AM", "08:00 AM"));
        PublicApiController controller = new PublicApiController(
                mock(CreateReservationUseCase.class),
                mock(SubmitDriverApplicationUseCase.class), schedules);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/schedules")
                        .param("pickupLocality", "Morteros")
                        .param("destination", "Córdoba")
                        .param("travelDate", "2026-08-20")
                        .param("roundTrip", "true")
                        .param("openReturn", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("03:00 AM"))
                .andExpect(jsonPath("$[1]").value("08:00 AM"));
    }

    @Test
    void acceptsArgentineTravelDateFormat() throws Exception {
        PricingAndScheduleService schedules = mock(PricingAndScheduleService.class);
        LocalDate travelDate = LocalDate.of(2026, 8, 20);
        when(schedules.availableDepartureSchedules("Morteros", "Córdoba", travelDate))
                .thenReturn(List.of("03:00 AM", "08:00 AM"));
        PublicApiController controller = new PublicApiController(
                mock(CreateReservationUseCase.class),
                mock(SubmitDriverApplicationUseCase.class), schedules);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/schedules")
                        .param("pickupLocality", "Morteros")
                        .param("destination", "Córdoba")
                        .param("travelDate", "20/08/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("03:00 AM"))
                .andExpect(jsonPath("$[1]").value("08:00 AM"));

        verify(schedules).availableDepartureSchedules("Morteros", "Córdoba", travelDate);
    }
}
