package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreateManualReservationUseCase;
import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.application.usecase.MarkTripAsPaidService;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.domain.exception.AgendaTripNotFoundException;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.infrastructure.config.PassengerBearerAuthenticationFilter;
import com.lunaris.ansenuza.infrastructure.config.SecurityConfig;
import java.math.BigDecimal;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManualTripApiController.class)
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class})
class ManualTripApiControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean CreateManualReservationUseCase creation;
    @MockitoBean MarkTripAsPaidService payments;
    @MockitoBean AccountRepository accounts;
    @MockitoBean PassengerOtpService otp;
    @MockitoBean InquiryService inquiries;

    private static final String SPECIAL = """
            {"tripCategory":"SPECIAL","firstName":"Ana","lastName":"Pérez","phone":"3511234567",
             "travelDate":"2030-01-02","pickupAddress":"Belgrano 100","originCustom":"De Suardi",
             "destinationCustom":"Alta Gracia","passengerCount":4,"customPrice":"120000.50",
             "departureSchedule":"09:30","paymentStatus":"PENDING","requiresInvoice":true}
            """;

    @Test
    void rejectsMoreThanFourPassengersBeforeCallingCreation() throws Exception {
        mvc.perform(post("/api/admin/trips").with(user("op").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(SPECIAL.replace("\"passengerCount\":4", "\"passengerCount\":5")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(creation);
    }

    @Test
    void operatorCreatesSpecialUsingSharedDtoAndReceivesPaymentState() throws Exception {
        when(creation.execute(any(), isNull())).thenAnswer(call -> {
            Reservation r = call.getArgument(0);
            r.setId(UUID.randomUUID());
            return List.of(r);
        });
        mvc.perform(post("/api/admin/trips").with(user("op").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(SPECIAL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].tripCategory").value("SPECIAL"))
                .andExpect(jsonPath("$[0].originCustom").value("De Suardi"))
                .andExpect(jsonPath("$[0].customPrice").value(120000.50))
                .andExpect(jsonPath("$[0].paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$[0].invoiceIssued").value(false));
        var captor = org.mockito.ArgumentCaptor.forClass(Reservation.class);
        verify(creation).execute(captor.capture(), isNull());
        assertThat(captor.getValue().getPassengerCount()).isEqualTo(4);
        assertThat(captor.getValue().getRequiresInvoice()).isTrue();
    }

    @Test
    void regularRequestKeepsDefaultCategoryAndFixedRoute() throws Exception {
        when(creation.execute(any(), isNull())).thenAnswer(call -> List.of(call.<Reservation>getArgument(0)));
        String regular = """
                {"firstName":"Ana","lastName":"Pérez","phone":"3511234567","travelDate":"2030-01-02",
                 "pickupAddress":"Belgrano 100","pickupLocality":"Morteros","destination":"Córdoba",
                 "passengerCount":2,"departureSchedule":"08:00"}
                """;
        mvc.perform(post("/api/admin/trips").with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(regular))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].tripCategory").value("REGULAR"))
                .andExpect(jsonPath("$[0].pickupLocality").value("Morteros"));
    }

    @Test
    void invalidMoneyPrecisionIsRejectedBeforeDomainInvocation() throws Exception {
        mvc.perform(post("/api/admin/trips").with(user("op").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(SPECIAL.replace("120000.50", "120000.501")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(creation);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "OPERADOR", "FACTURACION"})
    void authorizedRolesCanRecordPayment(String role) throws Exception {
        UUID id = UUID.randomUUID();
        when(payments.markTripAsPaid(id, "account")).thenReturn(Reservation.builder().id(id)
                .tripCategory(TripCategory.SPECIAL).paymentVerified(true)
                .customPrice(new BigDecimal("120000.50")).build());
        mvc.perform(post("/api/admin/trips/{id}/paid", id).with(user("account").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.invoiceIssued").value(false));
        verify(payments).markTripAsPaid(id, "account");
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHOFER", "PASSENGER"})
    void passengerAndDriverCannotCreateOrRecordPayment(String role) throws Exception {
        mvc.perform(post("/api/admin/trips").with(user("account").roles(role))
                        .contentType(MediaType.APPLICATION_JSON).content(SPECIAL))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/trips/{id}/paid", UUID.randomUUID()).with(user("account").roles(role)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(creation, payments);
    }

    @Test
    void accountingCannotCreateAndAnonymousCannotRecordPayment() throws Exception {
        mvc.perform(post("/api/admin/trips").with(user("account").roles("FACTURACION"))
                        .contentType(MediaType.APPLICATION_JSON).content(SPECIAL))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/trips/{id}/paid", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(creation, payments);
    }

    @Test
    void unknownTripReturnsDomainNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(payments.markTripAsPaid(id, "op")).thenThrow(new AgendaTripNotFoundException(id));
        mvc.perform(post("/api/admin/trips/{id}/paid", id).with(user("op").roles("OPERADOR")))
                .andExpect(status().isNotFound());
    }
}
