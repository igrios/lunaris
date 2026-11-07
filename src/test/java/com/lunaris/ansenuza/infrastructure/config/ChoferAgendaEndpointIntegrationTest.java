package com.lunaris.ansenuza.infrastructure.config;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.lunaris.ansenuza.application.usecase.ChoferAgendaService;
import com.lunaris.ansenuza.application.usecase.ChoferAgendaService.ServicioAgenda;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.infrastructure.web.controller.ChoferAgendaController;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ChoferAgendaController.class, com.lunaris.ansenuza.infrastructure.web.controller.ChoferAuthController.class})
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class,
        com.lunaris.ansenuza.application.usecase.ChoferAuthService.class})
class ChoferAgendaEndpointIntegrationTest {
    private static final String PATH = "/api/choferes/agenda";
    private static final String ORIGIN = "https://lunaris-web-reload.vercel.app";

    @Autowired MockMvc mockMvc;
    @MockitoBean ChoferAgendaService agendaService;
    @MockitoBean AccountRepository accounts;
    @MockitoBean PassengerOtpService passengerOtpService;
    @MockitoBean InquiryService inquiryService;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "OPERADOR", "CHOFER"})
    void returnsJsonAgendaAtExactFrontendPath(String role) throws Exception {
        LocalDate fecha = LocalDate.of(2026, 11, 7);
        var servicio = new ServicioAgenda(UUID.randomUUID(), "RES-001", fecha,
                "08:00", "Morteros", "Cordoba", UUID.randomUUID(), 2, "CONFIRMED", null,
                "Ana Pérez", "5493511234567", "Lleva equipaje",
                true, com.lunaris.ansenuza.domain.model.TripType.ROUND_TRIP);
        when(agendaService.execute(fecha)).thenReturn(List.of(servicio));

        mockMvc.perform(get(PATH).param("fecha", fecha.toString())
                        .header("Origin", ORIGIN).with(user("usuario").roles(role)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].codigo").value("RES-001"))
                .andExpect(jsonPath("$[0].fecha").value("2026-11-07"))
                .andExpect(jsonPath("$[0].nombrePasajero").value("Ana Pérez"))
                .andExpect(jsonPath("$[0].telefono").value("5493511234567"))
                .andExpect(jsonPath("$[0].comentarios").value("Lleva equipaje"))
                .andExpect(jsonPath("$[0].pasajeros").value(2))
                .andExpect(jsonPath("$[0].esIdaYVuelta").value(true))
                .andExpect(jsonPath("$[0].tipoViaje").value("ROUND_TRIP"))
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void acceptsMissingDate() throws Exception {
        when(agendaService.execute(isNull())).thenReturn(List.of());
        mockMvc.perform(get(PATH).with(user("chofer").roles("CHOFER")))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(agendaService).execute(isNull());
    }

    @Test
    void rejectsInvalidDate() throws Exception {
        mockMvc.perform(get(PATH).param("fecha", "07-11-2026")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allowsCorsPreflightWithoutAuthentication() throws Exception {
        mockMvc.perform(options(PATH).header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    @Test
    void rejectsAgendaWithoutSessionOrBearerToken() throws Exception {
        LocalDate fecha = LocalDate.of(2026, 11, 7);
        when(agendaService.execute(fecha)).thenReturn(List.of());
        mockMvc.perform(get(PATH).param("fecha", fecha.toString()))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void loginCreatesSessionThatCanAccessAgenda() throws Exception {
        var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/choferes/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"hector\",\"password\":\"cantor123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("hector"))
                .andExpect(jsonPath("$.password").doesNotExist()).andReturn();
        when(agendaService.execute(isNull())).thenReturn(List.of());
        mockMvc.perform(get(PATH).session((org.springframework.mock.web.MockHttpSession)
                        result.getRequest().getSession(false)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"usuario\":\"hector\",\"password\":\"incorrecta\"}",
            "{\"usuario\":\"Hector\",\"password\":\"cantor123\"}",
            "{\"usuario\":\"hector \",\"password\":\"cantor123\"}",
            "{\"usuario\":\"hector\",\"password\":\"Cantor123\"}",
            "{\"usuario\":\"\",\"password\":\"\"}", "{}"})
    void rejectsInvalidLogin(String credentials) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/choferes/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas."));
    }

    @Test
    void allowsLoginCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/choferes/login").header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void billingCannotAccessDriverAgenda() throws Exception {
        mockMvc.perform(get(PATH).with(user("billing").roles("FACTURACION")))
                .andExpect(status().isForbidden());
    }

}
