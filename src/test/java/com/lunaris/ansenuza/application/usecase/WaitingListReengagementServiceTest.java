package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WaitingListReengagementServiceTest {

    @Test
    void marksEntryNotifiedCreatesReentrySessionAndSendsButtons() {
        WaitingListRepository waitingList = mock(WaitingListRepository.class);
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        WaitingListEntry entry = WaitingListEntry.builder()
                .id(7L)
                .phoneNumber("543511112222")
                .passengerName("Ana Pérez")
                .travelDate(LocalDate.of(2026, 8, 20))
                .pickupLocality("Morteros")
                .destination("Córdoba")
                .passengerCount(2)
                .status(WaitingListEntry.WAITING)
                .build();
        when(waitingList.findByIdForUpdate(7L)).thenReturn(Optional.of(entry));
        when(sessions.findByPhoneNumber(entry.getPhoneNumber())).thenReturn(Optional.empty());
        WaitingListReengagementService service =
                new WaitingListReengagementService(waitingList, sessions, messaging);

        service.promote(7L);

        assertEquals(WaitingListEntry.NOTIFIED, entry.getStatus());
        ArgumentCaptor<ConversationSession> captor = ArgumentCaptor.forClass(ConversationSession.class);
        verify(sessions).saveAndFlush(captor.capture());
        assertEquals("CONFIRMING_WAITING_LIST_BOOKING", captor.getValue().getCurrentStep());
        assertEquals(7L, captor.getValue().getWaitingListEntryId());
        verify(messaging).sendButtons(
                org.mockito.ArgumentMatchers.eq(entry.getPhoneNumber()),
                org.mockito.ArgumentMatchers.eq("Lugar disponible"),
                org.mockito.ArgumentMatchers.contains("¡Hola Ana Pérez!"),
                org.mockito.ArgumentMatchers.anyList());
    }
}
