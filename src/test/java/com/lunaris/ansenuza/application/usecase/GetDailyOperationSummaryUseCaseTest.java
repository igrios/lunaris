package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetDailyOperationSummaryUseCaseTest {
    @Test
    void usesDistinctPassengerCountsForOutboundAndReturnLegs() {
        ReservationRepository repository = mock(ReservationRepository.class);
        LocalDate today = LocalDate.of(2026, 9, 11);
        when(repository.findByTravelDate(today)).thenReturn(List.of(new com.lunaris.ansenuza.domain.model.Reservation(),
                new com.lunaris.ansenuza.domain.model.Reservation()));
        // Ida y vuelta del mismo pasajero ya se deduplican en las consultas SQL del repositorio.
        when(repository.countDistinctPassengersByTravelDate(today)).thenReturn(1L);
        when(repository.countDistinctPaidPassengersByTravelDate(today)).thenReturn(1L);
        when(repository.countDistinctPendingPassengersByTravelDate(today)).thenReturn(0L);

        DailyOperationSummaryResponse summary = new GetDailyOperationSummaryUseCase(repository).execute(today);

        assertEquals(1L, summary.totalPassengers());
        assertEquals(1L, summary.paidReservations());
        assertEquals(0L, summary.pendingPayments());
        verify(repository).countDistinctPassengersByTravelDate(today);
        verify(repository).countDistinctPaidPassengersByTravelDate(today);
        verify(repository).countDistinctPendingPassengersByTravelDate(today);
    }
}
