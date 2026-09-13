package com.lunaris.ansenuza.domain.model.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.*;
import org.junit.jupiter.api.Test;

class PlanningWindowTest {
    private final LocalDate date = LocalDate.of(2030, 1, 1);
    private final ReservationRepository reservations = mock(ReservationRepository.class);
    private PricingAndScheduleService at(int hour, int minute) {
        return new PricingAndScheduleService(mock(FareRepository.class), mock(LocalityRepository.class),
                mock(BusinessParameterRepository.class), reservations,
                Clock.fixed(date.atTime(hour, minute).atZone(ArgentinaTime.ZONE_ID).toInstant(), ArgentinaTime.ZONE_ID));
    }
    @Test void allowsExactlyOneHourAndRejectsLessIncludingMidnight() {
        assertThat(at(7, 0).isWithinPlanningWindow(date, "08:00 AM")).isTrue();
        assertThat(at(7, 1).isWithinPlanningWindow(date, "08:00 AM")).isFalse();
        assertThat(at(23, 30).isWithinPlanningWindow(date, "23:59")).isFalse();
        assertThat(at(23, 30).isWithinPlanningWindow(date.plusDays(1), "03:00 AM")).isTrue();
    }
    @Test void hidesDepartedAndInsufficientCapacityForWholeParty() {
        when(reservations.countReservedSeats(date, "08:00 AM")).thenReturn(17L);
        assertThat(at(6, 0).availableDepartureSchedules("Morteros", "Córdoba", date, 3)).isEmpty();
        assertThat(at(6, 0).availableDepartureSchedules("Morteros", "Córdoba", date, 2)).containsExactly("08:00 AM");
    }
    @Test void returnOptionsApplyPlanningWindowAndWholePartyCapacity() {
        when(reservations.findReturnCapacityCandidates(date)).thenReturn(java.util.List.of(
                com.lunaris.ansenuza.domain.model.Reservation.builder().passengerCount(17)
                        .departureSchedule("14:00").build()));
        var service = new com.lunaris.ansenuza.application.usecase.ScheduleService(at(12, 0),
                mock(com.lunaris.ansenuza.application.usecase.LocalityService.class));
        assertThat(service.getSchedulesForBot("Córdoba", "Morteros", date, 3))
                .containsExactly("17:30");
        var late = new com.lunaris.ansenuza.application.usecase.ScheduleService(at(17, 0),
                mock(com.lunaris.ansenuza.application.usecase.LocalityService.class));
        assertThat(late.getSchedulesForBot("Córdoba", "Morteros", date, 1)).isEmpty();
    }
}
