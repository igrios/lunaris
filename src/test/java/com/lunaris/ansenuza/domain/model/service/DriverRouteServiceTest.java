package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;

class DriverRouteServiceTest {

    @Test
    void replacesRouteAndRenumbersEveryPassengerWithoutGaps() {
        ReservationRepository repository = mock(ReservationRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        DriverRouteService service = new DriverRouteService(repository, drivers);
        LocalDate date = LocalDate.of(2026, 7, 23);
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        Reservation removed = reservation(date, driver, 1);
        Reservation moved = reservation(date, driver, 2);
        Reservation added = reservation(date, null, null);
        List<UUID> requestedOrder = List.of(added.getId(), moved.getId());

        when(repository.findAllById(requestedOrder)).thenReturn(List.of(moved, added));
        when(repository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(driver.getId(), date))
                .thenReturn(List.of(removed, moved));
        when(drivers.findAllByIdForUpdate(java.util.Set.of(driver.getId())))
                .thenReturn(List.of(driver));
        when(repository.saveAllAndFlush(org.mockito.ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Reservation> result = service.replaceRoute(driver, date, requestedOrder);

        assertNull(removed.getDriver());
        assertNull(removed.getRouteSequence());
        assertEquals(added.getId(), result.get(0).getId());
        assertEquals(1, added.getRouteSequence());
        assertEquals(2, moved.getRouteSequence());
        verify(repository).saveAll(org.mockito.ArgumentMatchers.any());
    }

    private Reservation reservation(LocalDate date, Driver driver, Integer sequence) {
        return Reservation.builder()
                .id(UUID.randomUUID())
                .travelDate(date)
                .driver(driver)
                .routeSequence(sequence)
                .build();
    }
}
