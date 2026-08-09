package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import com.lunaris.ansenuza.infrastructure.web.dto.NewsBannerDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class NewsBannerAdminControllerTest {

    @Test
    void panelLoadsAllBanners() {
        NewsBannerService service = mock(NewsBannerService.class);
        when(service.findAll()).thenReturn(List.of());
        NewsBannerAdminController controller = new NewsBannerAdminController(service);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("admin/novedades", controller.panel(model));
        assertEquals(List.of(), model.get("banners"));
        assertEquals(NewsBannerDto.class, model.get("newsBannerForm").getClass());
    }

    @Test
    void deletesBannerAndRedirects() {
        NewsBannerService service = mock(NewsBannerService.class);
        NewsBannerAdminController controller = new NewsBannerAdminController(service);
        UUID id = UUID.randomUUID();

        String view = controller.delete(id, new RedirectAttributesModelMap());

        assertEquals("redirect:/admin/novedades", view);
        verify(service).delete(id);
    }

    @Test
    void panelReturnsSafeModelWhenLoadingFails() {
        NewsBannerService service = mock(NewsBannerService.class);
        when(service.findAll()).thenThrow(new IllegalStateException("database unavailable"));
        NewsBannerAdminController controller = new NewsBannerAdminController(service);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("admin/novedades", controller.panel(model));
        assertEquals(List.of(), model.get("banners"));
        assertEquals(List.of(), model.get("specialTrips"));
        assertEquals(NewsBannerDto.class, model.get("newsBannerForm").getClass());
        assertEquals("No se pudieron cargar las novedades.", model.get("errorMessage"));
    }

    @Test
    void createAppliesDefaultsAndHandlesMultipartFailure() {
        NewsBannerService service = mock(NewsBannerService.class);
        when(service.create(any(), any(), any(), any(Boolean.class), any(Boolean.class),
                any(), any(), any()))
                .thenThrow(new IllegalStateException("cloudinary unavailable"));
        NewsBannerAdminController controller = new NewsBannerAdminController(service);
        NewsBannerDto form = new NewsBannerDto();
        form.setTitle("Evento");
        form.setEventType(null);
        form.setHasWaitingList(null);
        form.setActive(null);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/novedades", controller.create(form, redirect));
        assertEquals("GENERAL", form.getEventType());
        assertEquals(Boolean.FALSE, form.getHasWaitingList());
        assertEquals(Boolean.TRUE, form.getActive());
        assertEquals("No se pudo procesar la novedad. Revisá el flyer y los datos.",
                redirect.getFlashAttributes().get("errorMessage"));
    }
}
