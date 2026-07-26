package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;

class OnboardPassengerUseCaseTest {

    @Test
    void marksOnboardAndNotifiesNextPassengerWithLocalityEtaDifference() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        LocalityRepository localities = mock(LocalityRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        OnboardPassengerUseCase useCase =
                new OnboardPassengerUseCase(reservations, drivers, localities, whatsApp);
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setFullName("Juan Chofer");
        driver.setCurrentLocationUrl("https://maps.google.com/?q=-31.42,-64.18");
        LocalDate date = LocalDate.of(2026, 7, 23);
        Reservation current = reservation(driver, date, 1, "Morteros", "111", "Actual");
        Reservation next = reservation(driver, date, 2, "Porteña", "222", "Siguiente");

        when(reservations.findById(current.getId())).thenReturn(Optional.of(current));
        when(reservations.findByIdForUpdate(current.getId())).thenReturn(Optional.of(current));
        when(drivers.findAllByIdForUpdate(java.util.Set.of(driver.getId())))
                .thenReturn(List.of(driver));
        when(reservations.findRouteByEffectiveDate(driver.getId(), date))
                .thenReturn(List.of(current, next));
        when(localities.findFirstByNameIgnoreCase("Morteros"))
                .thenReturn(Optional.of(locality("Morteros", 40)));
        when(localities.findFirstByNameIgnoreCase("Porteña"))
                .thenReturn(Optional.of(locality("Porteña", 70)));

        useCase.execute(current.getId());

        assertEquals(Reservation.TravelStatus.ONBOARDED, current.getTravelStatus());
        InOrder persistenceBeforeLookup = inOrder(reservations);
        persistenceBeforeLookup.verify(reservations).saveAndFlush(current);
        persistenceBeforeLookup.verify(reservations)
                .findRouteByEffectiveDate(driver.getId(), date);
        verify(whatsApp).sendProximoEnCaminoTemplate("222", "Siguiente", driver.getFullName(), 30);
        verify(whatsApp).sendMessage(
                "222", "📍 Ubicación actual del chofer: https://maps.google.com/?q=-31.42,-64.18");
    }

    @Test
    void returnLegUsesReturnDateAndNullSafeFallbackOrderToNotifyNextPassenger() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        LocalityRepository localities = mock(LocalityRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        OnboardPassengerUseCase useCase =
                new OnboardPassengerUseCase(reservations, drivers, localities, whatsApp);
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setFullName("Juan Chofer");
        LocalDate outboundDate = LocalDate.of(2026, 7, 20);
        LocalDate returnDate = LocalDate.of(2026, 7, 23);
        Reservation current =
                reservation(driver, outboundDate, null, "Porteña", "111", "Actual");
        current.setReservationCode("RES-1-VUELTA");
        current.setReturnDate(returnDate);
        current.setCreatedAt(LocalDateTime.of(2026, 7, 20, 10, 0));
        Reservation next =
                reservation(driver, null, null, "Morteros", "222", "Siguiente");
        next.setReservationCode("RES-2-VUELTA");
        next.setReturnDate(returnDate);
        next.setCreatedAt(LocalDateTime.of(2026, 7, 20, 10, 1));

        when(reservations.findById(current.getId())).thenReturn(Optional.of(current));
        when(reservations.findByIdForUpdate(current.getId())).thenReturn(Optional.of(current));
        when(drivers.findAllByIdForUpdate(java.util.Set.of(driver.getId())))
                .thenReturn(List.of(driver));
        when(reservations.findRouteByEffectiveDate(driver.getId(), returnDate))
                .thenReturn(List.of(next, current));
        when(localities.findFirstByNameIgnoreCase("Porteña"))
                .thenReturn(Optional.of(locality("Porteña", 70)));
        when(localities.findFirstByNameIgnoreCase("Morteros"))
                .thenReturn(Optional.of(locality("Morteros", 40)));

        useCase.execute(current.getId());

        assertEquals(Reservation.TravelStatus.ONBOARDED, current.getTravelStatus());
        verify(whatsApp).sendProximoEnCaminoTemplate("222", "Siguiente", driver.getFullName(), 30);
    }

    @Test
    void repeatedOnboardWebhookDoesNotNotifyNextPassengerTwice() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        LocalityRepository localities = mock(LocalityRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        OnboardPassengerUseCase useCase =
                new OnboardPassengerUseCase(reservations, drivers, localities, whatsApp);
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        Reservation alreadyOnboard =
                reservation(driver, LocalDate.of(2026, 7, 23), 1, "Morteros", "111", "Actual");
        alreadyOnboard.setTravelStatus(Reservation.TravelStatus.ONBOARDED);
        when(reservations.findById(alreadyOnboard.getId()))
                .thenReturn(Optional.of(alreadyOnboard));
        when(reservations.findByIdForUpdate(alreadyOnboard.getId()))
                .thenReturn(Optional.of(alreadyOnboard));
        when(drivers.findAllByIdForUpdate(java.util.Set.of(driver.getId())))
                .thenReturn(List.of(driver));

        useCase.execute(alreadyOnboard.getId());

        verify(reservations, never()).saveAndFlush(alreadyOnboard);
        verify(whatsApp, never()).sendProximoEnCaminoTemplate(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void skipsCanceledAndOtherLegPassengersToNotifyNextPendingPassenger() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        LocalityRepository localities = mock(LocalityRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        OnboardPassengerUseCase useCase =
                new OnboardPassengerUseCase(reservations, drivers, localities, whatsApp);
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        LocalDate returnDate = LocalDate.of(2026, 7, 23);
        Reservation current =
                reservation(driver, returnDate, 1, "Porteña", "111", "Actual");
        current.setReservationCode("RES-1-VUELTA");
        current.setReturnDate(returnDate);
        Reservation canceled =
                reservation(driver, returnDate, 2, "Morteros", "222", "Ida");
        canceled.setReservationCode("RES-2-VUELTA");
        canceled.setReturnDate(returnDate);
        canceled.setTravelStatus(Reservation.TravelStatus.CANCELED);
        Reservation outbound =
                reservation(driver, returnDate, 2, "Morteros", "224", "Otra ida");
        outbound.setReservationCode("RES-2-IDA");
        Reservation sequenceThree =
                reservation(driver, returnDate, 3, "Morteros", "333", "Tercero");
        sequenceThree.setReservationCode("RES-3-VUELTA");
        sequenceThree.setReturnDate(returnDate);

        when(reservations.findById(current.getId())).thenReturn(Optional.of(current));
        when(reservations.findByIdForUpdate(current.getId())).thenReturn(Optional.of(current));
        when(drivers.findAllByIdForUpdate(java.util.Set.of(driver.getId())))
                .thenReturn(List.of(driver));
        when(reservations.findRouteByEffectiveDate(driver.getId(), returnDate))
                .thenReturn(List.of(current, canceled, outbound, sequenceThree));
        when(localities.findFirstByNameIgnoreCase("Porteña"))
                .thenReturn(Optional.of(locality("Porteña", 70)));
        when(localities.findFirstByNameIgnoreCase("Morteros"))
                .thenReturn(Optional.of(locality("Morteros", 40)));

        useCase.execute(current.getId());

        verify(whatsApp).sendProximoEnCaminoTemplate(
                "333", "Tercero", driver.getFullName(), 30);
    }

    @Test
    void rejectsCallbackForDriverNotAssignedToReservation() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        OnboardPassengerUseCase useCase = new OnboardPassengerUseCase(
                reservations,
                drivers,
                mock(LocalityRepository.class),
                mock(WhatsAppService.class));
        Driver assigned = new Driver();
        assigned.setId(UUID.randomUUID());
        Reservation reservation = reservation(
                assigned, LocalDate.of(2026, 7, 23), 1, "Morteros", "111", "Actual");
        Driver actor = new Driver();
        actor.setId(UUID.randomUUID());
        actor.setPhone("543512222222");
        actor.setActive(true);
        when(drivers.findFirstByPhone(actor.getPhone())).thenReturn(Optional.of(actor));
        when(reservations.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(reservation.getId(), actor.getPhone()));

        verify(reservations, never()).saveAndFlush(reservation);
    }

    @Test
    void canonicalStatusUpdatePersistsNonOnboardStatusWithoutNotification() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        OnboardPassengerUseCase useCase = new OnboardPassengerUseCase(
                reservations,
                mock(DriverRepository.class),
                mock(LocalityRepository.class),
                mock(WhatsAppService.class));
        Reservation reservation = Reservation.builder().id(UUID.randomUUID()).build();
        when(reservations.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservations.saveAndFlush(reservation)).thenReturn(reservation);

        Reservation updated = useCase.updateTravelStatus(
                reservation.getId(), Reservation.TravelStatus.REALIZED);

        assertEquals(Reservation.TravelStatus.REALIZED, updated.getTravelStatus());
        verify(reservations).saveAndFlush(reservation);
    }

    private Reservation reservation(
            Driver driver, LocalDate date, Integer sequence, String locality, String phone, String name) {
        return Reservation.builder()
                .id(UUID.randomUUID())
                .driver(driver)
                .travelDate(date)
                .routeSequence(sequence)
                .status("CONFIRMED")
                .pickupLocality(locality)
                .passenger(Passenger.builder().firstName(name).lastName("Pasajero").phone(phone).build())
                .build();
    }

    private Locality locality(String name, int minutes) {
        return Locality.builder().name(name).minutesFromOrigin(minutes).build();
    }
}
