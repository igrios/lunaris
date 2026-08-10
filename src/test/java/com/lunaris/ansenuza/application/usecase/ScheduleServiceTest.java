package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.dto.ScheduleDto;
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
        ScheduleService service = new ScheduleService(pricing);

        assertEquals(List.of(
                new ScheduleDto("03:00", "03:00", 7, true),
                new ScheduleDto("08:00", "08:00", 0, false)),
                service.getSchedulesForWeb(date));
    }

    @Test
    void botContractRemainsFormattedDomainStrings() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        LocalDate date = LocalDate.of(2026, 8, 20);
        when(pricing.availableDepartureSchedules("Arrufó", "Córdoba", date))
                .thenReturn(List.of("03:00 AM", "08:00 AM"));
        ScheduleService service = new ScheduleService(pricing);

        assertEquals(List.of("03:00 AM", "08:00 AM"),
                service.getSchedulesForBot("Arrufó", "Córdoba", date));
    }

    @Test
    void botReturnsConfiguredBlocksBeforeTravelDateIsSelected() {
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        when(pricing.departureSchedules()).thenReturn(List.of("03:00 AM", "08:00 AM"));
        ScheduleService service = new ScheduleService(pricing);

        assertEquals(List.of("03:00 AM", "08:00 AM"),
                service.getSchedulesForBot("Arrufó", "Córdoba", null));
    }
}
