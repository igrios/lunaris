package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.application.dto.ScheduleDto;
import com.lunaris.ansenuza.application.usecase.ScheduleService;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.infrastructure.config.PassengerBearerAuthenticationFilter;
import com.lunaris.ansenuza.infrastructure.config.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SchedulesController.class)
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class})
class SchedulesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private PassengerOtpService passengerOtpService;

    @Test
    void versionedSchedulesArePublicAndPreserveFrontendContract() throws Exception {
        when(scheduleService.getSchedulesForWeb(
                org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(
                        new ScheduleDto("03:00", "03:00", "03:00 AM", 19, true),
                        new ScheduleDto("08:00", "08:00", "08:00 AM", 0, false)));

        mockMvc.perform(get("/api/v1/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("03:00"))
                .andExpect(jsonPath("$[0].departureTime").value("03:00"))
                .andExpect(jsonPath("$[0].label").value("03:00 AM"))
                .andExpect(jsonPath("$[0].availableSeats").value(19))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].departureTime").value("08:00"))
                .andExpect(jsonPath("$[1].availableSeats").value(0))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    void versionedSchedulesRejectMalformedTravelDate() throws Exception {
        mockMvc.perform(get("/api/v1/schedules")
                        .param("pickupLocality", "Morteros")
                        .param("travelDate", "fecha-invalida"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnTripUsesReturnScheduleContract() throws Exception {
        var date = java.time.LocalDate.of(2026, 8, 22);
        when(scheduleService.getReturnSchedulesForWeb(date)).thenReturn(List.of(
                new ScheduleDto("14:00", "14:00", "14:00 hs", 8, true),
                new ScheduleDto("17:30", "17:30", "17:30 hs", 4, true)));

        mockMvc.perform(get("/api/v1/schedules")
                        .param("travelDate", "2026-08-22")
                        .param("returnTrip", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("14:00 hs"))
                .andExpect(jsonPath("$[1].label").value("17:30 hs"));
    }
}
