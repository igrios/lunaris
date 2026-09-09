package com.lunaris.ansenuza.application.scheduler;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class OpenReturnCutoffSchedulerTest {
    @Test
    void alertsOnlyPendingPassengersAndPersistsAuditOnce() {
        var reservations = mock(ReservationRepository.class);
        var events = mock(ReservationEventRepository.class);
        var locks = mock(CapacityLockRepository.class);
        var messages = mock(SimpMessagingTemplate.class);
        var scheduler = new OpenReturnCutoffScheduler(reservations, events, locks, messages);
        var today = ArgentinaTime.today();
        var pending = Reservation.builder().id(UUID.randomUUID()).reservationCode("TEST-VUELTA")
                .passenger(Passenger.builder().firstName("Ana").lastName("Pérez").build())
                .travelStatus(Reservation.TravelStatus.OPEN_RETURN).build();
        when(locks.findForUpdate(today + "|DAY|RETURN")).thenReturn(new CapacityLock(today + "|DAY|RETURN"));
        when(reservations.findReturnCapacityCandidates(today)).thenReturn(List.of(pending,
                Reservation.builder().travelStatus(Reservation.TravelStatus.CONFIRMED).build()));
        when(events.existsByReservationIdAndEventTypeAndCreatedAtGreaterThanEqual(
                pending.getId(), "OPEN_RETURN_CUTOFF", today.atStartOfDay())).thenReturn(false, true);
        scheduler.alertPendingReturns();
        scheduler.alertPendingReturns();
        var event = ArgumentCaptor.forClass(ReservationEvent.class);
        verify(events).save(event.capture());
        assertNotNull(event.getValue().getId());
        assertEquals(pending.getId(), event.getValue().getReservationId());
        verify(messages).convertAndSend(eq("/topic/system-alerts"), (Object) argThat(
                value -> value.toString().contains("Ana Pérez")));
    }

    @Test
    void cronUsesElevenInArgentina() throws Exception {
        var scheduled = OpenReturnCutoffScheduler.class.getMethod("alertPendingReturns")
                .getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);
        assertEquals("0 0 11 * * *", scheduled.cron());
        assertEquals("America/Argentina/Cordoba", scheduled.zone());
    }
}
