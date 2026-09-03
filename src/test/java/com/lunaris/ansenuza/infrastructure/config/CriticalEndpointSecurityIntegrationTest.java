package com.lunaris.ansenuza.infrastructure.config;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.application.usecase.DriverAuthorizationService;
import com.lunaris.ansenuza.application.usecase.DriverManagementService;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.DriverRouteService;
import com.lunaris.ansenuza.domain.model.service.FleetCapacityService;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.port.in.ResolveEffectiveTripOriginUseCase;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.infrastructure.web.controller.AgendaViewController;
import com.lunaris.ansenuza.infrastructure.web.controller.DriverController;
import com.lunaris.ansenuza.infrastructure.web.controller.WhatsAppController;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AgendaViewController.class, DriverController.class, WhatsAppController.class})
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class,
        DriverAuthorizationService.class})
class CriticalEndpointSecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AccountRepository accounts;
    @MockitoBean PassengerOtpService passengerOtpService;
    @MockitoBean ReservationRepository reservations;
    @MockitoBean DriverRepository drivers;
    @MockitoBean OnboardPassengerUseCase onboardPassengerUseCase;
    @MockitoBean DriverManagementService driverManagementService;
    @MockitoBean WhatsAppService whatsAppService;
    @MockitoBean ConfirmPaymentUseCase confirmPaymentUseCase;
    @MockitoBean DriverRouteService driverRouteService;
    @MockitoBean FleetCapacityService fleetCapacityService;
    @MockitoBean WaitingListRepository waitingListRepository;
    @MockitoBean SystemConfigurationService systemConfigurationService;
    @MockitoBean ResolveEffectiveTripOriginUseCase originResolver;
    @MockitoBean MessagingPort messagingPort;

    @Test
    void routeSheetRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/hoja-ruta")
                        .param("driverId", UUID.randomUUID().toString())
                        .param("date", "2026-09-10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void driverCannotReadAnotherDriversRouteSheet() throws Exception {
        UUID authenticatedId = UUID.randomUUID();
        UUID requestedId = UUID.randomUUID();
        Driver authenticated = driver(authenticatedId, "5493511111111");
        when(drivers.findFirstByPhone("5493511111111")).thenReturn(Optional.of(authenticated));

        mockMvc.perform(get("/hoja-ruta")
                        .with(user("5493511111111").roles("CHOFER"))
                        .param("driverId", requestedId.toString())
                        .param("date", LocalDate.of(2026, 9, 10).toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void confirmAssistanceRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/driver/confirm-assistance")
                        .contentType("application/json")
                        .content("{\"code\":\"MOR-COR-001\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void driverCannotConfirmReservationAssignedToAnotherDriver() throws Exception {
        UUID authenticatedId = UUID.randomUUID();
        Driver authenticated = driver(authenticatedId, "5493511111111");
        Driver assigned = driver(UUID.randomUUID(), "5493512222222");
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID()).driver(assigned).reservationCode("MOR-COR-001").build();
        when(drivers.findFirstByPhone("5493511111111")).thenReturn(Optional.of(authenticated));
        when(reservations.findByReservationCode("MOR-COR-001"))
                .thenReturn(Optional.of(reservation));

        mockMvc.perform(post("/api/driver/confirm-assistance")
                        .with(user("5493511111111").roles("CHOFER"))
                        .contentType("application/json")
                        .content("{\"code\":\"MOR-COR-001\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void whatsappTestRejectsNonAdmin() throws Exception {
        mockMvc.perform(get("/whatsapp/test").with(user("driver").roles("CHOFER")))
                .andExpect(status().isForbidden());
    }

    private Driver driver(UUID id, String phone) {
        Driver driver = new Driver();
        driver.setId(id);
        driver.setPhone(phone);
        driver.setActive(true);
        return driver;
    }
}
