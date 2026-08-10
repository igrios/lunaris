package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
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
    private PricingAndScheduleService scheduleService;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private PassengerOtpService passengerOtpService;

    @Test
    void versionedSchedulesArePublicAndPreserveFrontendContract() throws Exception {
        when(scheduleService.departureSchedules()).thenReturn(List.of("03:00 AM", "08:00 AM"));
        when(scheduleService.availableSeats(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("03:00 AM"))).thenReturn(19);
        when(scheduleService.availableSeats(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("08:00 AM"))).thenReturn(0);

        mockMvc.perform(get("/api/v1/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("03:00"))
                .andExpect(jsonPath("$[0].departureTime").value("03:00"))
                .andExpect(jsonPath("$[0].availableSeats").value(19))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].departureTime").value("08:00"))
                .andExpect(jsonPath("$[1].availableSeats").value(0))
                .andExpect(jsonPath("$[1].available").value(false));
    }
}
