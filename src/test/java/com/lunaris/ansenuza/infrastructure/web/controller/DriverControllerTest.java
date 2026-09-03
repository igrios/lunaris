package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.application.usecase.DriverManagementService;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;

class DriverControllerTest {

    @Test
    void exactOnboardPayloadDelegatesToCanonicalUseCase() {
        OnboardPassengerUseCase onboard = mock(OnboardPassengerUseCase.class);
        DriverController controller = new DriverController(
                mock(ReservationRepository.class),
                onboard,
                mock(DriverManagementService.class),
                mock(com.lunaris.ansenuza.application.usecase.DriverAuthorizationService.class));
        UUID reservationId = UUID.randomUUID();
        Reservation saved = Reservation.builder()
                .id(reservationId)
                .travelStatus(Reservation.TravelStatus.ONBOARD)
                .build();
        when(onboard.updateTravelStatus(
                reservationId, Reservation.TravelStatus.ONBOARD))
                .thenReturn(saved);

        ResponseEntity<?> response = controller.updateTravelStatus(
                reservationId,
                new DriverController.UpdateTravelStatusRequest("ONBOARD"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(onboard).updateTravelStatus(
                reservationId, Reservation.TravelStatus.ONBOARD);
    }

    @Test
    void invalidOrWrongCasePayloadIsRejectedBeforeUseCase() {
        OnboardPassengerUseCase onboard = mock(OnboardPassengerUseCase.class);
        DriverController controller = new DriverController(
                mock(ReservationRepository.class),
                onboard,
                mock(DriverManagementService.class),
                mock(com.lunaris.ansenuza.application.usecase.DriverAuthorizationService.class));

        ResponseEntity<?> response = controller.updateTravelStatus(
                UUID.randomUUID(),
                new DriverController.UpdateTravelStatusRequest("onboard"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(onboard, never()).updateTravelStatus(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }
}
