package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WaitingListServiceTest {

    @Test
    void joinsPassengerUsingConversationData() {
        WaitingListRepository repository = mock(WaitingListRepository.class);
        WaitingListService service = new WaitingListService(repository);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .passengerName("Ana Pérez")
                .travelDate(LocalDate.of(2026, 8, 20))
                .pickupLocality("Morteros")
                .destination("Córdoba")
                .passengerCount(2)
                .build();

        service.join(session);

        ArgumentCaptor<WaitingListEntry> captor = ArgumentCaptor.forClass(WaitingListEntry.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals(WaitingListEntry.WAITING, captor.getValue().getStatus());
        assertEquals(2, captor.getValue().getPassengerCount());
    }

    @Test
    void updatesOnlySupportedStatuses() {
        WaitingListRepository repository = mock(WaitingListRepository.class);
        WaitingListService service = new WaitingListService(repository);
        WaitingListEntry entry = WaitingListEntry.builder().id(1L).status("WAITING").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entry));

        service.updateStatus(1L, "contacted");

        assertEquals(WaitingListEntry.CONTACTED, entry.getStatus());
        assertThrows(DomainValidationException.class,
                () -> service.updateStatus(1L, "UNKNOWN"));
    }
}
