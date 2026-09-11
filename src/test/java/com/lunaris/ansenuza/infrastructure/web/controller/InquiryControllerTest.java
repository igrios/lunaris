package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.infrastructure.config.SecurityConfig;
import com.lunaris.ansenuza.infrastructure.config.PassengerBearerAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(InquiryController.class)
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class})
class InquiryControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean InquiryService inquiries;
    @MockitoBean AccountRepository accounts;
    @MockitoBean PassengerOtpService passengerOtpService;

    @Test
    void operatorCanRenderInquiryWithEscapedMessageAndPendingBadge() throws Exception {
        when(inquiries.pendingCount()).thenReturn(1L);
        when(inquiries.list()).thenReturn(List.of(Inquiry.builder().id(UUID.randomUUID())
                .phone("5493515550101").message("<script>alert(1)</script>")
                .status(InquiryStatus.PENDING).createdAt(LocalDateTime.now()).build()));
        mvc.perform(get("/admin/consultas").with(user("operator").roles("OPERADOR")))
                .andExpect(status().isOk()).andExpect(view().name("inquiries"))
                .andExpect(model().attribute("pendingInquiryCount", 1L))
                .andExpect(content().string(containsString("https://wa.me/5493515550101")))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void authorizedRolesCanUpdateInquiry() throws Exception {
        UUID id = UUID.randomUUID();
        for (String role : List.of("ADMIN", "OPERADOR")) {
            mvc.perform(post("/admin/consultas/{id}/estado", id).with(user("user").roles(role))
                    .with(csrf()).param("status", "RESOLVED"))
                    .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/consultas"));
        }
        verify(inquiries, times(2)).updateStatus(id, InquiryStatus.RESOLVED);
    }

    @Test
    void otherRolesCannotReadOrUpdateInquiries() throws Exception {
        for (String role : List.of("CHOFER", "FACTURACION")) {
            mvc.perform(get("/admin/consultas").with(user("user").roles(role)))
                    .andExpect(status().isForbidden());
            mvc.perform(post("/admin/consultas/{id}/estado", UUID.randomUUID())
                    .with(user("user").roles(role)).with(csrf()).param("status", "ARCHIVED"))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(inquiries);
    }

    @Test
    void updateRequiresCsrf() throws Exception {
        mvc.perform(post("/admin/consultas/{id}/estado", UUID.randomUUID())
                .with(user("operator").roles("OPERADOR")).param("status", "ARCHIVED"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(inquiries);
    }
}
