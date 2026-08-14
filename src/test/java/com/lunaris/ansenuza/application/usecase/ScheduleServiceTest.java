package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.dto.ScheduleDto;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScheduleServiceTest {

    @Test
    void webContractReturnsDtosIncludingUnavailableBlocks() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        LocalDate date = LocalDate.of(2026, 8, 20);
        when(pricing.departureSchedules()).thenReturn(List.of("03:00 AM", "08:00 AM"));
        when(pricing.availableSeats(date, "03:00 AM")).thenReturn(7);
        when(pricing.availableSeats(date, "08:00 AM")).thenReturn(0);
        when(pricing.calculateEstimatedPickupTime(null, "03:00", false, date))
                .thenReturn("03:00 hs");
        when(pricing.calculateEstimatedPickupTime(null, "08:00", false, date))
                .thenReturn("08:00 hs");
        ScheduleService service = new ScheduleService(pricing, mock(LocalityService.class));

        assertEquals(List.of(
                new ScheduleDto("03:00", "03:00", "03:00 hs", 7, true),
                new ScheduleDto("08:00", "08:00", "08:00 hs", 0, false)),
                service.getSchedulesForWeb(null, date));
    }

    @Test
    void webContractUsesTheSameCalculatedPickupTimeAsTheBot() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        LocalityService localities = mock(LocalityService.class);
        LocalDate date = LocalDate.of(2026, 8, 20);
        when(localities.findAllWithActiveFare()).thenReturn(List.of(
                Locality.builder().name("Morteros").build()));
        when(pricing.departureSchedules()).thenReturn(List.of("03:00 AM"));
        when(pricing.availableSeats(date, "03:00 AM")).thenReturn(7);
        when(pricing.calculateEstimatedPickupTime("Morteros", "03:00", false, date))
                .thenReturn("04:55 hs");
        ScheduleService service = new ScheduleService(pricing, localities);

        assertEquals(List.of(
                new ScheduleDto("03:00", "04:55", "04:55 hs", 7, true)),
                service.getSchedulesForWeb("Morteros", date));
    }

    @Test
    void webScheduleLabelOmitsSeatAvailability() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        LocalDate date = LocalDate.of(2026, 8, 20);
        when(pricing.departureSchedules()).thenReturn(List.of("03:00 AM"));
        when(pricing.availableSeats(date, "03:00 AM")).thenReturn(12);
        when(pricing.calculateEstimatedPickupTime(null, "03:00", false, date))
                .thenReturn("05:05 hs");
        ScheduleService service = new ScheduleService(pricing, mock(LocalityService.class));

        ScheduleDto schedule = service.getSchedulesForWeb(null, date).getFirst();

        assertEquals("05:05 hs", schedule.label());
        assertEquals(12, schedule.availableSeats());
    }

    @Test
    void returnContractUsesCalculatedReturnWindowsWithoutGenericLabels() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        LocalDate date = LocalDate.of(2026, 8, 22);
        when(pricing.availableSeats(date, "14:00")).thenReturn(8);
        when(pricing.availableSeats(date, "17:30")).thenReturn(4);
        when(pricing.calculateEstimatedPickupTime(null, "14:00", true, date))
                .thenReturn("14:00 hs");
        when(pricing.calculateEstimatedPickupTime(null, "17:30", true, date))
                .thenReturn("17:30 hs");
        ScheduleService service = new ScheduleService(pricing, mock(LocalityService.class));

        assertEquals(List.of(
                new ScheduleDto("14:00", "14:00", "14:00 hs", 8, true),
                new ScheduleDto("17:30", "17:30", "17:30 hs", 4, true)),
                service.getReturnSchedulesForWeb(date));
    }

    @Test
    void testGetSchedulesForBot_WhenTravelDateIsPresent_AppliesCapacityAndCutoff() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        LocalDate date = LocalDate.of(2026, 8, 20);
        when(pricing.availableDepartureSchedules("Arrufó", "Córdoba", date))
                .thenReturn(List.of("03:00 AM", "08:00 AM"));
        LocalityService localities = mock(LocalityService.class);
        when(localities.findAllWithActiveFare()).thenReturn(List.of(
                Locality.builder().name("Arrufó").build()));
        ScheduleService service = new ScheduleService(pricing, localities);

        assertEquals(List.of("03:00 AM", "08:00 AM"),
                service.getSchedulesForBot("Arrufó", "Córdoba", date));
        verify(pricing).availableDepartureSchedules("Arrufó", "Córdoba", date);
    }

    @Test
    void testGetSchedulesForBot_WhenTravelDateIsNull_ReturnsBaseBlocks() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        when(pricing.departureSchedules()).thenReturn(List.of("03:00 AM", "08:00 AM"));
        LocalityService localities = mock(LocalityService.class);
        when(localities.findAllWithActiveFare()).thenReturn(List.of(
                Locality.builder().name("Arrufó").build()));
        ScheduleService service = new ScheduleService(pricing, localities);

        assertEquals(List.of("03:00 AM", "08:00 AM"),
                service.getSchedulesForBot("Arrufó", "Córdoba", null));
        verify(pricing).departureSchedules();
    }

    @Test
    void webAndBotReturnNoSchedulesForLocalityWithoutActiveFare() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        LocalityService localities = mock(LocalityService.class);
        when(localities.findAllWithActiveFare()).thenReturn(List.of(
                Locality.builder().name("Morteros").build()));
        ScheduleService service = new ScheduleService(pricing, localities);
        LocalDate date = LocalDate.of(2026, 8, 20);

        assertEquals(List.of(), service.getSchedulesForWeb("Sin tarifa", date));
        assertEquals(List.of(), service.getSchedulesForBot("Sin tarifa", "Córdoba", date));
    }
}
