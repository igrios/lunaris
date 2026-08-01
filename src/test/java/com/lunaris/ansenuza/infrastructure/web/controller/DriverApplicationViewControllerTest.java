package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.usecase.DriverApplicationManagementService;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

class DriverApplicationViewControllerTest {

    private final DriverApplicationManagementService managementService =
            mock(DriverApplicationManagementService.class);
    private final DriverApplicationRepository applicationRepository =
            mock(DriverApplicationRepository.class);
    private final DriverApplicationViewController controller =
            new DriverApplicationViewController(managementService, applicationRepository);

    @Test
    void panelProvidesEmptyPostulacionesWhenRepositoryReturnsNull() {
        when(applicationRepository.findByStatusOrderByCreatedAtAsc(
                DriverApplication.Status.PENDING)).thenReturn(null);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.panel(model);

        assertEquals("admin/postulaciones", view);
        assertEquals(List.of(), model.get("postulaciones"));
    }

    @Test
    void panelFiltersNullApplications() {
        DriverApplication application = DriverApplication.builder()
                .fullName("Ana Pérez")
                .build();
        when(applicationRepository.findByStatusOrderByCreatedAtAsc(
                DriverApplication.Status.PENDING)).thenReturn(
                        java.util.Arrays.asList(null, application));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.panel(model);

        assertEquals(List.of(application), model.get("postulaciones"));
    }
}
