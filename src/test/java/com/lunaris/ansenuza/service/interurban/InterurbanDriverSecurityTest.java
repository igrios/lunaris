package com.lunaris.ansenuza.service.interurban;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.infrastructure.config.PassengerBearerAuthenticationFilter;
import com.lunaris.ansenuza.infrastructure.config.SecurityConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InterurbanDriverController.class, properties = {
        "lunaris.interurban.enabled=true"})
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class, InterurbanDriverSecurityConfiguration.class,
        InterurbanDriverExceptionHandler.class})
class InterurbanDriverSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean DriverRouteSheetQuery routes;
    @MockitoBean CheckInService checkIn;
    @MockitoBean AccountRepository accounts;
    @MockitoBean PassengerOtpService passengers;
    @MockitoBean com.lunaris.ansenuza.application.usecase.InquiryService inquiryService;
    final UUID trip = UUID.randomUUID();
    final String body = "{\"token\":\"opaque-token\",\"tripId\":\"" + trip + "\"}";

    @Test void routeSheetRequiresAuthenticationAndDriverRole() throws Exception {
        mvc.perform(get("/api/v1/driver/route-sheet").param("date", "2026-09-20")).andExpect(status().isUnauthorized());
        for (String role : List.of("PASSENGER", "OPERADOR", "FACTURACION")) {
            mvc.perform(get("/api/v1/driver/route-sheet").param("date", "2026-09-20").with(user("user").roles(role)))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(routes);
    }

    @Test void routeSheetIssuesCsrfForSameSessionAndPostAcceptsIt() throws Exception {
        when(routes.find(any(), any())).thenReturn(new DriverRouteSheetQuery.RouteSheet(UUID.randomUUID(), LocalDate.of(2026,9,20), List.of()));
        when(checkIn.verify(any(), any(), any())).thenReturn(new CheckInService.Result(UUID.randomUUID(), trip, "CHECKED_IN", Instant.now()));
        var result = mvc.perform(get("/api/v1/driver/route-sheet").param("date", "2026-09-20")
                        .with(user("driver").roles("CHOFER")))
                .andExpect(status().isOk()).andExpect(header().exists("X-CSRF-TOKEN"))
                .andExpect(header().string("Cache-Control", "no-store")).andReturn();
        mvc.perform(post("/api/v1/checkin/verify").with(user("driver").roles("CHOFER"))
                        .session((MockHttpSession) result.getRequest().getSession(false))
                        .header("X-CSRF-TOKEN", result.getResponse().getHeader("X-CSRF-TOKEN"))
                        .contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CHECKED_IN"));
        verify(checkIn).verify(eq("opaque-token"), eq(trip), any());
    }

    @Test void postWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/api/v1/checkin/verify").with(user("driver").roles("CHOFER"))
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        verifyNoInteractions(checkIn);
    }

    @Test void cannotCheckInWithPassengerRoleEvenWithCsrf() throws Exception {
        mvc.perform(post("/api/v1/checkin/verify").with(user("passenger").roles("PASSENGER")).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        verifyNoInteractions(checkIn);
    }

    @Test void domainErrorsHaveExplicitStatusAndCode() throws Exception {
        doThrow(new QrAlreadyConsumedException()).when(checkIn).verify(any(), any(), any());
        mvc.perform(post("/api/v1/checkin/verify").with(user("driver").roles("CHOFER")).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("QR_ALREADY_CONSUMED"));
        doThrow(new QrExpiredException()).when(checkIn).verify(any(), any(), any());
        mvc.perform(post("/api/v1/checkin/verify").with(user("driver").roles("CHOFER")).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isGone());
        doThrow(new AccessDeniedException("fixture")).when(checkIn).verify(any(), any(), any());
        mvc.perform(post("/api/v1/checkin/verify").with(user("driver").roles("CHOFER")).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
    }

    @Test void csrfRuleDoesNotInterceptLegacyApi() throws Exception {
        // El controlador legado no está en este slice: debe llegar a MVC (404), no ser bloqueado por CSRF (403).
        mvc.perform(post("/api/driver/confirm-assistance").with(user("driver").roles("CHOFER"))
                .contentType("application/json").content("{}")).andExpect(status().isNotFound());
    }
}
