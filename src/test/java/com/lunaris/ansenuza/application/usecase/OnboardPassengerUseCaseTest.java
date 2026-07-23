package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;

class OnboardPassengerUseCaseTest {

    @Test
    void marksOnboardAndNotifiesNextPassengerWithLocalityEtaDifference() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        LocalityRepository localities = mock(LocalityRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        OnboardPassengerUseCase useCase =
                new OnboardPassengerUseCase(reservations, localities, whatsApp);
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setFullName("Juan Chofer");
        LocalDate date = LocalDate.of(2026, 7, 23);
        Reservation current = reservation(driver, date, 1, "Morteros", "111", "Actual");
        Reservation next = reservation(driver, date, 2, "Porteña", "222", "Siguiente");

        when(reservations.findById(current.getId())).thenReturn(Optional.of(current));
        when(reservations.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(driver.getId(), date))
                .thenReturn(List.of(current, next));
        when(localities.findFirstByNameIgnoreCase("Morteros"))
                .thenReturn(Optional.of(locality("Morteros", 40)));
        when(localities.findFirstByNameIgnoreCase("Porteña"))
                .thenReturn(Optional.of(locality("Porteña", 70)));

        useCase.execute(current.getId());

        assertEquals(Reservation.TravelStatus.ONBOARD, current.getTravelStatus());
        verify(whatsApp).sendProximoEnCaminoTemplate("222", "Siguiente", driver.getFullName(), 30);
    }

    private Reservation reservation(
            Driver driver, LocalDate date, int sequence, String locality, String phone, String name) {
        return Reservation.builder()
                .id(UUID.randomUUID())
                .driver(driver)
                .travelDate(date)
                .routeSequence(sequence)
                .pickupLocality(locality)
                .passenger(Passenger.builder().firstName(name).lastName("Pasajero").phone(phone).build())
                .build();
    }

    private Locality locality(String name, int minutes) {
        return Locality.builder().name(name).minutesFromOrigin(minutes).build();
    }
}
