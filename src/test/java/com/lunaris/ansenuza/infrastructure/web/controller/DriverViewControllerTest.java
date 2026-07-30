package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.dao.DataIntegrityViolationException;

class DriverViewControllerTest {

    private DriverRepository driverRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        driverRepository = mock(DriverRepository.class);
        when(driverRepository.findAll()).thenReturn(List.of());
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new DriverViewController(driverRepository))
                .build();
    }

    @Test
    void emptyIdCreatesDriverWithoutTypeMismatch() throws Exception {
        mockMvc.perform(post("/choferes/guardar")
                        .param("id", "")
                        .param("fullName", "  Ana Pérez  ")
                        .param("phone", "  543512345678  ")
                        .param("ranking", "")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/choferes"))
                .andExpect(flash().attribute("successMessage", "Chofer creado correctamente."));

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).saveAndFlush(captor.capture());
        Driver saved = captor.getValue();
        assertNotNull(saved.getId());
        assertEquals("Ana Pérez", saved.getFullName());
        assertEquals("543512345678", saved.getPhone());
        assertEquals(5, saved.getRanking());
    }

    @Test
    void invalidUuidReturnsFormWithDetailedBindingError() throws Exception {
        mockMvc.perform(post("/choferes/guardar")
                        .param("id", "not-a-uuid")
                        .param("fullName", "Ana Pérez")
                        .param("phone", "543512345678")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("choferes"))
                .andExpect(model().attributeHasFieldErrors("driver", "id"))
                .andExpect(model().attributeExists("choferes"));

        verify(driverRepository, never()).saveAndFlush(any());
    }

    @Test
    void validationErrorsReturnFormInsteadOfServerError() throws Exception {
        mockMvc.perform(post("/choferes/guardar")
                        .param("id", "")
                        .param("fullName", " ")
                        .param("phone", "")
                        .param("ranking", "9"))
                .andExpect(status().isOk())
                .andExpect(view().name("choferes"))
                .andExpect(model().attributeHasFieldErrors(
                        "driver", "fullName", "phone"));

        verify(driverRepository, never()).saveAndFlush(any());
    }

    @Test
    void invalidRankingDefaultsToFiveAndRedirectsAfterSaving() throws Exception {
        mockMvc.perform(post("/choferes/guardar")
                        .param("fullName", "Ana Pérez")
                        .param("phone", "543512345678")
                        .param("ranking", "no-es-un-numero")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/choferes"));

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).saveAndFlush(captor.capture());
        assertEquals(5, captor.getValue().getRanking());
    }

    @Test
    void persistenceErrorRedirectsBackWithErrorFlag() throws Exception {
        when(driverRepository.saveAndFlush(any(Driver.class)))
                .thenThrow(new DataIntegrityViolationException("teléfono duplicado"));

        mockMvc.perform(post("/choferes/guardar")
                        .param("fullName", "Ana Pérez")
                        .param("phone", "543512345678")
                        .param("ranking", "4")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/choferes?error=true"));
    }
}
