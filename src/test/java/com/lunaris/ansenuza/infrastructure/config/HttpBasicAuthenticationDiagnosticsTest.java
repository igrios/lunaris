package com.lunaris.ansenuza.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.service.interurban.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InterurbanDriverController.class, properties = "lunaris.interurban.enabled=true")
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class,
        InterurbanDriverSecurityConfiguration.class, InterurbanDriverExceptionHandler.class,
        AuthenticationDiagnostics.class})
@ExtendWith(OutputCaptureExtension.class)
class HttpBasicAuthenticationDiagnosticsTest {
    @Autowired MockMvc mvc;
    @MockitoBean AccountRepository accounts;
    @MockitoBean DriverRouteSheetQuery routes;
    @MockitoBean CheckInService checkIn;
    @MockitoBean PassengerOtpService passengers;
    @MockitoBean InquiryService inquiries;

    private Account account() {
        return Account.builder().id(UUID.randomUUID()).username("juan").displayName("Juan")
                .passwordHash(new SecurityConfig().passwordEncoder().encode("juan123"))
                .active(true).roles(Set.of(Role.CHOFER)).build();
    }

    private org.springframework.test.web.servlet.ResultActions request(String password) throws Exception {
        return mvc.perform(get("/api/v1/driver/route-sheet").param("date", "2026-09-30")
                .with(httpBasic("juan", password)));
    }

    @Test
    void authenticatesJuanWithRawBcryptHashAndPreservesAccountId() throws Exception {
        var account = account();
        var encoder = new SecurityConfig().passwordEncoder();
        assertThat(encoder.matches("juan123", account.getPasswordHash())).isTrue();
        when(accounts.findByUsernameIgnoreCase("juan")).thenReturn(Optional.of(account));
        when(routes.find(any(), any())).thenAnswer(invocation -> {
            Authentication authentication = invocation.getArgument(1);
            assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);
            assertThat(((UserPrincipal) authentication.getPrincipal()).getAccountId()).isEqualTo(account.getId());
            return new DriverRouteSheetQuery.RouteSheet(UUID.randomUUID(), LocalDate.of(2026, 9, 30), List.of());
        });
        request("juan123").andExpect(status().isOk());
    }

    @Test
    void wrongPasswordLogsBadCredentialsButReturnsGeneric401(CapturedOutput output) throws Exception {
        when(accounts.findByUsernameIgnoreCase("juan")).thenReturn(Optional.of(account()));
        var response = request("wrong-password-secret").andExpect(status().isUnauthorized()).andReturn().getResponse();
        assertThat(output).contains("reason=BadCredentialsException").doesNotContain("wrong-password-secret");
        assertThat(response.getContentAsString()).doesNotContain("BadCredentials", "juan", "password");
        verifyNoInteractions(routes);
    }

    @Test
    void missingAccountLogsLookupCauseWithoutRevealingItToClient(CapturedOutput output) throws Exception {
        when(accounts.findByUsernameIgnoreCase("juan")).thenReturn(Optional.empty());
        var response = request("juan123").andExpect(status().isUnauthorized()).andReturn().getResponse();
        assertThat(output).contains("reason=USER_NOT_FOUND");
        assertThat(response.getContentAsString()).doesNotContain("Usuario no encontrado");
    }

    @Test
    void disabledAccountLogsDisabled(CapturedOutput output) throws Exception {
        var account = account();
        account.setActive(false);
        when(accounts.findByUsernameIgnoreCase("juan")).thenReturn(Optional.of(account));
        request("juan123").andExpect(status().isUnauthorized());
        assertThat(output).contains("reason=DisabledException");
    }

    @Test
    void prefixedBcryptIsDiagnosedWithoutLoggingTheHash(CapturedOutput output) throws Exception {
        var account = account();
        account.setPasswordHash("{bcrypt}" + account.getPasswordHash());
        when(accounts.findByUsernameIgnoreCase("juan")).thenReturn(Optional.of(account));
        request("juan123").andExpect(status().isUnauthorized());
        assertThat(output).contains("reason=BCRYPT_PREFIX_UNSUPPORTED").doesNotContain(account.getPasswordHash());
    }

    @Test
    void storageFailureLogsExceptionTypesWithoutSensitiveMessages(CapturedOutput output) throws Exception {
        when(accounts.findByUsernameIgnoreCase("juan"))
                .thenThrow(new DataAccessResourceFailureException("private-storage-detail"));
        request("juan123").andExpect(status().isUnauthorized());
        assertThat(output).contains("reason=ACCOUNT_LOAD_FAILED", "rootCause=DataAccessResourceFailureException",
                "reason=InternalAuthenticationServiceException").doesNotContain("private-storage-detail");
    }

    @Test
    void roleConversionFailureIsDiagnosed(CapturedOutput output) throws Exception {
        when(accounts.findByUsernameIgnoreCase("juan"))
                .thenThrow(new IllegalArgumentException("private-role-detail"));
        request("juan123").andExpect(status().isUnauthorized());
        assertThat(output).contains("reason=ACCOUNT_LOAD_FAILED", "rootCause=IllegalArgumentException")
                .doesNotContain("private-role-detail");
    }
}
