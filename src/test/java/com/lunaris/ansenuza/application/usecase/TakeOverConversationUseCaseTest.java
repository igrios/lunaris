package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TakeOverConversationUseCaseTest {
    private final ConversationSessionRepository repository = mock(ConversationSessionRepository.class);
    private final TakeOverConversationUseCase useCase = new TakeOverConversationUseCase(repository);

    @Test
    void preservesFullPhoneAndPausesBeforeOpeningChat() {
        var session = ConversationSession.builder().id(9L).phoneNumber("5493562123456").build();
        when(repository.findByPhoneNumber("5493562123456")).thenReturn(Optional.of(session));
        assertEquals("5493562123456", useCase.execute("+54 9 (3562) 123-456"));
        assertTrue(session.isBotPaused());
        verify(repository).saveAndFlush(session);
        useCase.execute("5493562123456");
        assertTrue(session.isBotPaused());
        verify(repository, times(1)).saveAndFlush(session);
    }

    @Test
    void rejectsTruncatedPhoneWithoutWriting() {
        assertThrows(DomainValidationException.class, () -> useCase.execute("9"));
        verifyNoInteractions(repository);
    }

    @Test
    void doesNotCreateUnknownConversation() {
        when(repository.findByPhoneNumber("5493562123456")).thenReturn(Optional.empty());
        assertThrows(DomainValidationException.class, () -> useCase.execute("5493562123456"));
        verify(repository, never()).saveAndFlush(any());
    }
}
