package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.OperatorPhoneService;
import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.domain.model.OperatorNotificationPhone;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.infrastructure.config.SecurityConfig;
import com.lunaris.ansenuza.infrastructure.config.PassengerBearerAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(OperatorPhoneController.class)
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class})
class OperatorPhoneControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean OperatorPhoneService operators;
    @MockitoBean InquiryService inquiries;
    @MockitoBean AccountRepository accounts;
    @MockitoBean PassengerOtpService passengerOtpService;

    @Test
    void adminCanRenderAndManagePhones() throws Exception {
        var operator = new OperatorNotificationPhone();
        operator.setId(UUID.randomUUID()); operator.setName("Ignacio"); operator.setPhone("5493512282251");
        when(operators.list()).thenReturn(List.of(operator));
        mvc.perform(get("/admin/operadores").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("5493512282251")));
        mvc.perform(post("/admin/operadores").with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", "Ignacio").param("phone", "5493512282251"))
                .andExpect(redirectedUrl("/admin/operadores"));
        verify(operators).add("Ignacio", "5493512282251");
        mvc.perform(post("/admin/operadores/{id}/estado", operator.getId()).with(user("admin").roles("ADMIN"))
                .with(csrf()).param("active", "false")).andExpect(status().is3xxRedirection());
        verify(operators).setActive(operator.getId(), false);
        mvc.perform(post("/admin/operadores/{id}/eliminar", operator.getId()).with(user("admin").roles("ADMIN"))
                .with(csrf())).andExpect(status().is3xxRedirection());
        verify(operators).delete(operator.getId());
    }

    @Test
    void otherRolesCannotManagePhones() throws Exception {
        for (String role : List.of("OPERADOR", "CHOFER", "FACTURACION")) {
            mvc.perform(get("/admin/operadores").with(user("user").roles(role))).andExpect(status().isForbidden());
            mvc.perform(post("/admin/operadores").with(user("user").roles(role)).with(csrf())
                    .param("name", "Otro").param("phone", "5493512282251")).andExpect(status().isForbidden());
        }
        verifyNoInteractions(operators);
    }

    @Test
    void changesRequireCsrf() throws Exception {
        mvc.perform(post("/admin/operadores").with(user("admin").roles("ADMIN"))
                .param("name", "Otro").param("phone", "5493512282251")).andExpect(status().isForbidden());
        verifyNoInteractions(operators);
    }
}
