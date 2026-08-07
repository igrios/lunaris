package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ResolveEffectiveTripOriginServiceTest {
    @Test
    void filtersByScheduleAndRebasesMinuteOffsetsAtEffectiveOrigin() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        LocalityRepository localities = mock(LocalityRepository.class);
        LocalDate date = LocalDate.of(2026, 8, 10);
        when(reservations.findConfirmedActiveByTravelDate(date)).thenReturn(List.of(
                reservation("Morteros", "08:00", 2),
                reservation("Arrufó", "03:00 AM", 3)));
        when(localities.findFirstByNameIgnoreCase("Morteros"))
                .thenReturn(Optional.of(locality("Morteros", 300)));
        when(localities.findFirstByNameIgnoreCase("Brinkmann"))
                .thenReturn(Optional.of(locality("Brinkmann", 280)));

        var result = new ResolveEffectiveTripOriginService(reservations, localities).resolve(date, "08:00 AM");

        assertThat(result.effectiveOrigin()).isEqualTo("Morteros");
        assertThat(result.minuteOffsets()).containsEntry("Morteros", 0).containsEntry("Brinkmann", 20);
        assertThat(result.summary()).contains("San Guillermo");
    }

    private Reservation reservation(String locality, String schedule, int passengers) {
        return Reservation.builder().pickupLocality(locality).departureSchedule(schedule)
                .passengerCount(passengers).build();
    }

    private Locality locality(String name, int minutes) {
        return Locality.builder().name(name).minutesFromOrigin(minutes).build();
    }
}
