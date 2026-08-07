package com.lunaris.ansenuza.application.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ReturnScheduleAuditSchedulerTest {

    @Test
    void promptsPassengerAndStoresReservationContextForWindowReply() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        ReturnScheduleAuditScheduler scheduler = new ReturnScheduleAuditScheduler(
                reservations, sessions, whatsApp);
        String phone = "5493511111111";
        Reservation reservation = Reservation.builder()
                .reservationCode("ARR-COR-001-VUELTA")
                .roundTrip(true)
                .passenger(Passenger.builder().phone(phone).build())
                .build();
        when(reservations.findReturnScheduleAuditCandidates(any(), any()))
                .thenReturn(List.of(reservation));
        when(sessions.findByPhoneNumber(phone)).thenReturn(Optional.empty());

        scheduler.auditReturnSchedules();

        ArgumentCaptor<ConversationSession> captor = ArgumentCaptor.forClass(ConversationSession.class);
        verify(sessions).saveAndFlush(captor.capture());
        assertEquals("RETURN_WINDOW_SELECTION", captor.getValue().getCurrentStep());
        assertEquals("ARR-COR-001-VUELTA", captor.getValue().getReservationCode());
        verify(whatsApp).sendInteractiveButtons(
                eq(phone), eq("Horario de regreso"),
                eq("Elegí la ventana de salida desde Córdoba:"), anyList());
    }
}
