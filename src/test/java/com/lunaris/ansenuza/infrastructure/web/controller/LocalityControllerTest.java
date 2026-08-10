package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.LocalityService;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicCatalogApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocalityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocalityService localityService;

    @MockitoBean
    private FareRepository fareRepository;

    @MockitoBean
    private PassengerOtpService passengerOtpService;

    @MockitoBean
    private AccountRepository accountRepository;

    @Test
    void versionedEndpointReturnsExpectedActiveLocalityPayload() throws Exception {
        UUID id = UUID.randomUUID();
        Locality locality = Locality.builder().id(id).name("Morteros")
                .kmsToCordoba(240).minutesFromOrigin(40).build();
        Fare fare = Fare.builder().localityName("Morteros")
                .amount(new BigDecimal("100000")).build();
        when(localityService.findAllWithActiveFare()).thenReturn(List.of(locality));
        when(fareRepository.findFirstByLocalityNameIgnoreCase("Morteros"))
                .thenReturn(Optional.of(fare));

        mockMvc.perform(get("/api/v1/localities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Morteros"))
                .andExpect(jsonPath("$[0].amount").value(100000));
    }
}
