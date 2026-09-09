package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.DriverRouteService;
import com.lunaris.ansenuza.domain.model.service.FleetCapacityService;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.agenda.EnviarHojaRutaRequest;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.domain.port.in.ResolveEffectiveTripOriginUseCase;
import com.lunaris.ansenuza.domain.port.in.RouteOriginResolution;

class AgendaDriverDispatchControllerTest {

    @Test
    void sortsPendingReservationsBeforeDispatchedReservations() {
        Reservation pending = Reservation.builder().id(UUID.randomUUID()).build();
        Reservation dispatched = Reservation.builder().id(UUID.randomUUID())
                .travelStatus(Reservation.TravelStatus.ROUTE_SENT).build();

        List<Reservation> sorted = java.util.stream.Stream.of(dispatched, pending)
                .sorted(AgendaViewController.dispatchedLastComparator())
                .toList();

        assertEquals(pending.getId(), sorted.getFirst().getId());
        assertEquals(dispatched.getId(), sorted.getLast().getId());
    }

    @Test
    void keepsAssignmentSuccessfulAndReturnsWarningWhenWhatsAppDispatchFails() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        DriverRouteService routes = mock(DriverRouteService.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        ResolveEffectiveTripOriginUseCase originResolver = mock(ResolveEffectiveTripOriginUseCase.class);
        AgendaViewController controller = new AgendaViewController(
                reservations, whatsApp, drivers, mock(ConfirmPaymentUseCase.class), routes,
                mock(FleetCapacityService.class),
                mock(com.lunaris.ansenuza.domain.repository.WaitingListRepository.class),
                mock(com.lunaris.ansenuza.domain.model.service.SystemConfigurationService.class), originResolver,
                mock(com.lunaris.ansenuza.application.usecase.DriverAuthorizationService.class));
        UUID driverId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setFullName("Carlos Chofer");
        driver.setPhone("+54 351 555-1234");
        Reservation reservation = Reservation.builder().id(reservationId)
                .travelDate(LocalDate.of(2026, 8, 4)).build();
        when(drivers.findById(driverId)).thenReturn(Optional.of(driver));
        when(reservations.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(routes.replaceRoute(
                org.mockito.ArgumentMatchers.eq(driver),
                org.mockito.ArgumentMatchers.eq(reservation.getTravelDate()),
                org.mockito.ArgumentMatchers.eq(List.of(reservationId)),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(reservation));
        when(originResolver.resolve(reservation.getTravelDate(), "03:00")).thenReturn(new RouteOriginResolution(
                reservation.getTravelDate(), "03:00", "Morteros", List.of(), Map.of("Morteros", 0),
                "Cabecera del día recalculada: Morteros."));
        when(whatsApp.sendDriverRouteDispatch(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList()))
                .thenThrow(new IllegalStateException("Meta unavailable"));

        var response = controller.enviarHojaRuta(
                new EnviarHojaRutaRequest(driverId, driver.getPhone(), List.of(reservationId)));

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("assigned"));
        assertEquals("Warning", body.get("whatsAppStatus"));
        assertTrue(body.get("message").toString().startsWith(
                "Chofer asignado correctamente en sistema."));
        verify(drivers).findById(driverId);
        var order = inOrder(routes, whatsApp);
        order.verify(routes).replaceRoute(
                org.mockito.ArgumentMatchers.eq(driver),
                org.mockito.ArgumentMatchers.eq(reservation.getTravelDate()),
                org.mockito.ArgumentMatchers.eq(List.of(reservationId)),
                org.mockito.ArgumentMatchers.any());
        order.verify(whatsApp).sendDriverRouteDispatch(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList());
    }
}
