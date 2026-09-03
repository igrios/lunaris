package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.DriverRouteService;
import com.lunaris.ansenuza.domain.model.service.FleetCapacityService;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import com.lunaris.ansenuza.domain.port.in.ResolveEffectiveTripOriginUseCase;
import com.lunaris.ansenuza.domain.port.in.RouteOriginResolution;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

class AgendaRouteSheetControllerTest {

    @Test
    void pickupAddressPrefersTypedTextAndKeepsSharedMapAsSecondaryLink() {
        Passenger passenger = Passenger.builder()
                .address("https://maps.google.com/?q=-31.42,-64.18")
                .build();
        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .pickupAddress("San Martín 450")
                .build();

        var display = AgendaViewController.resolvePickupAddress(reservation);

        assertEquals("San Martín 450", display.text());
        assertEquals(
                "https://maps.google.com/?q=-31.42,-64.18",
                display.mapUrl());
    }

    @Test
    void pickupAddressFallsBackToPassengerDataBeforeMissingLabel() {
        Reservation withPassengerAddress = Reservation.builder()
                .passenger(Passenger.builder().address("Belgrano 120").build())
                .build();
        Reservation withoutAddress = Reservation.builder().build();

        assertEquals(
                "Belgrano 120",
                AgendaViewController.resolvePickupAddress(withPassengerAddress).text());
        assertEquals(
                "Sin dirección registrada",
                AgendaViewController.resolvePickupAddress(withoutAddress).text());
    }

    @Test
    void rendersEvenOneActiveReservationForExactDriverAndDate() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        UUID driverId = UUID.randomUUID();
        LocalDate travelDate = LocalDate.of(2026, 8, 5);
        Driver driver = new Driver();
        driver.setId(driverId);
        Reservation passenger = Reservation.builder()
                .driver(driver)
                .travelDate(travelDate)
                .pickupLocality("Morteros")
                .destination("Córdoba")
                .departureSchedule("03:00")
                .passengerCount(1)
                .build();
        when(drivers.findById(driverId)).thenReturn(Optional.of(driver));
        when(reservations.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                driverId, travelDate)).thenReturn(List.of(passenger));
        ResolveEffectiveTripOriginUseCase originResolver = mock(ResolveEffectiveTripOriginUseCase.class);
        when(originResolver.resolve(travelDate, "03:00")).thenReturn(new RouteOriginResolution(
                travelDate, "03:00", "Morteros", List.of("Arrufó", "Villa Trinidad", "San Guillermo", "Suardi"),
                java.util.Map.of("Morteros", 0), "Cabecera del día recalculada: Morteros."));
        AgendaViewController controller = new AgendaViewController(
                reservations,
                mock(WhatsAppService.class),
                drivers,
                mock(ConfirmPaymentUseCase.class),
                mock(DriverRouteService.class),
                mock(FleetCapacityService.class),
                mock(com.lunaris.ansenuza.domain.repository.WaitingListRepository.class),
                mock(com.lunaris.ansenuza.domain.model.service.SystemConfigurationService.class), originResolver,
                mock(com.lunaris.ansenuza.application.usecase.DriverAuthorizationService.class));
        ConcurrentModel model = new ConcurrentModel();

        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin", "n/a", org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        String view = controller.showHojaRuta(driverId, travelDate, authentication, model);

        assertEquals("admin/hoja-ruta", view);
        assertEquals(List.of(passenger), model.getAttribute("reservas"));
        assertEquals(1, model.getAttribute("totalYendo"));
        assertEquals("Morteros", model.getAttribute("effectiveOrigin"));
        verify(reservations).findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                driverId, travelDate);
    }
}
