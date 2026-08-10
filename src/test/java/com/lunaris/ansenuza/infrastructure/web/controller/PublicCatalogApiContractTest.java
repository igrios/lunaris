package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.application.usecase.LocalityService;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PublicCatalogApiContractTest {

    private LocalityService localityService;
    private FareRepository fareRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        localityService = mock(LocalityService.class);
        fareRepository = mock(FareRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new PublicCatalogApiController(localityService, fareRepository))
                .build();
    }

    @Test
    void returnsSchedulesInsideSchedulesProperty() throws Exception {
        mockMvc.perform(get("/api/public/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedules").isArray())
                .andExpect(jsonPath("$.schedules[0]").value("03:00"))
                .andExpect(jsonPath("$.schedules[1]").value("08:00"));
    }

    @Test
    void returnsLocalityDetailsAndAmountAsJsonArray() throws Exception {
        UUID localityId = UUID.randomUUID();
        Locality locality = Locality.builder()
                .id(localityId)
                .name("Miramar")
                .kmsToCordoba(197)
                .minutesFromOrigin(185)
                .build();
        Fare fare = Fare.builder()
                .id(UUID.randomUUID())
                .localityName("Miramar")
                .amount(new BigDecimal("62000.00"))
                .build();
        when(localityService.findAllWithActiveFare()).thenReturn(List.of(locality));
        when(fareRepository.findFirstByLocalityNameIgnoreCase("Miramar"))
                .thenReturn(Optional.of(fare));

        mockMvc.perform(get("/api/public/localities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(localityId.toString()))
                .andExpect(jsonPath("$[0].name").value("Miramar"))
                .andExpect(jsonPath("$[0].kmsToCordoba").value(197))
                .andExpect(jsonPath("$[0].minutesFromOrigin").value(185))
                .andExpect(jsonPath("$[0].amount").value(62000.00));
    }

    @Test
    void versionedLocalitiesUseTheSameActiveFareCatalog() throws Exception {
        Locality locality = Locality.builder()
                .id(UUID.randomUUID())
                .name("Morteros")
                .kmsToCordoba(240)
                .minutesFromOrigin(40)
                .build();
        Fare fare = Fare.builder()
                .id(UUID.randomUUID())
                .localityName("Morteros")
                .amount(new BigDecimal("100000"))
                .build();
        when(localityService.findAllWithActiveFare()).thenReturn(List.of(locality));
        when(fareRepository.findFirstByLocalityNameIgnoreCase("Morteros"))
                .thenReturn(Optional.of(fare));

        mockMvc.perform(get("/api/v1/localities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Morteros"))
                .andExpect(jsonPath("$[0].amount").value(100000));
    }
}
