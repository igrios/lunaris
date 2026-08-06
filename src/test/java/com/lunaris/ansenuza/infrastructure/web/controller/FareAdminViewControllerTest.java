package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.lunaris.ansenuza.domain.port.in.CreateFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.DeleteFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.GetFaresQuery;
import com.lunaris.ansenuza.domain.port.in.UpdateLocalityFareUseCase;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.dao.DataIntegrityViolationException;

class FareAdminViewControllerTest {
    private GetFaresQuery query;
    private CreateFareLocalityUseCase createUseCase;
    private DeleteFareLocalityUseCase deleteUseCase;
    private FareAdminViewController controller;

    @BeforeEach
    void setUp() {
        query = mock(GetFaresQuery.class);
        createUseCase = mock(CreateFareLocalityUseCase.class);
        deleteUseCase = mock(DeleteFareLocalityUseCase.class);
        controller = new FareAdminViewController(query, createUseCase,
                mock(UpdateLocalityFareUseCase.class), deleteUseCase);
    }

    @Test
    void panelLoadsFaresThroughQueryPort() {
        when(query.getAll()).thenReturn(List.of());
        var model = new ExtendedModelMap();

        assertThat(controller.panel(model)).isEqualTo("admin/fares");
        assertThat(model.get("tarifas")).isEqualTo(List.of());
    }

    @Test
    void createAndDeleteDelegateToPrimaryPorts() {
        var redirectAttributes = new RedirectAttributesModelMap();
        controller.create("Miramar", 197, 185, new BigDecimal("62000"), redirectAttributes);
        UUID fareId = UUID.randomUUID();
        controller.delete(fareId, new RedirectAttributesModelMap());

        verify(createUseCase).create("Miramar", 197, 185, new BigDecimal("62000"));
        verify(deleteUseCase).delete(fareId);
        assertThat(redirectAttributes.getFlashAttributes().get("success"))
                .isEqualTo("Localidad y tarifa agregadas exitosamente.");
    }

    @Test
    void persistenceFailureRedirectsWithFriendlyErrorInsteadOfPropagating() {
        doThrow(new DataIntegrityViolationException("restricción duplicada"))
                .when(createUseCase).create("Miramar", 197, 185, new BigDecimal("62000"));
        var redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.create("Miramar", 197, 185, new BigDecimal("62000"), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/fares");
        assertThat(redirectAttributes.getFlashAttributes().get("error").toString())
                .contains("Error al guardar", "restricción duplicada");
    }

    @Test
    void unexpectedFailureRedirectsWithoutExposingInternalDetails() {
        doThrow(new IllegalStateException("password=secret"))
                .when(createUseCase).create("Miramar", 197, 185, new BigDecimal("62000"));
        var redirectAttributes = new RedirectAttributesModelMap();

        controller.create("Miramar", 197, 185, new BigDecimal("62000"), redirectAttributes);

        assertThat(redirectAttributes.getFlashAttributes().get("error"))
                .isEqualTo("Ocurrió un error inesperado al procesar la tarifa.");
    }
}
