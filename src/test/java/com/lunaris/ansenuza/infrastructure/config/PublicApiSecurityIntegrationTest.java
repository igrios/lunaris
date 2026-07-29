package com.lunaris.ansenuza.infrastructure.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.infrastructure.web.controller.PublicCatalogApiController;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicCatalogApiController.class)
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class})
class PublicApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocalityRepository localityRepository;

    @MockitoBean
    private FareRepository fareRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private PassengerOtpService passengerOtpService;

    @BeforeEach
    void setUp() {
        when(localityRepository.findLocalitiesWithFares()).thenReturn(List.of());
    }

    @Test
    void publicLocalitiesDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/localities"))
                .andExpect(status().isOk());
    }

    @Test
    void publicSchedulesDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/schedules"))
                .andExpect(status().isOk());
    }

    @Test
    void configuredOriginCanCompleteCorsPreflight() throws Exception {
        String origin = "https://www.lunarisansenuza.com.ar";

        mockMvc.perform(options("/api/public/localities")
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origin))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("GET")));
    }
}
