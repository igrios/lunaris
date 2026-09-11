package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.OperatorNotification;
import com.lunaris.ansenuza.domain.model.OperatorNotificationPhone;
import com.lunaris.ansenuza.domain.repository.OperatorNotificationPhoneRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.Executor;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class OperatorNotificationServiceTest {
    @Test
    void schedulesWorkAndContinuesAfterOneRecipientFails() {
        var phones = mock(OperatorNotificationPhoneRepository.class);
        var whatsapp = mock(WhatsAppService.class);
        var executor = mock(Executor.class);
        var service = new OperatorNotificationService(phones, whatsapp, executor);
        var first = new OperatorNotificationPhone(); first.setPhone("5493512282251");
        var second = new OperatorNotificationPhone(); second.setPhone("5493512282252");
        when(phones.findByActiveTrueOrderByCreatedAtAsc()).thenReturn(List.of(first, second));
        doThrow(new IllegalStateException("Meta unavailable")).when(whatsapp).sendMessage(first.getPhone(), "alerta");
        service.notifyAfterCommit(new OperatorNotification("alerta"));
        verifyNoInteractions(phones, whatsapp);
        var task = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(task.capture());
        assertDoesNotThrow(() -> task.getValue().run());
        verify(whatsapp).sendMessage(second.getPhone(), "alerta");
        verify(phones).findByActiveTrueOrderByCreatedAtAsc();
    }

    @Test
    void noActiveOperatorsMeansNoMessages() {
        var phones = mock(OperatorNotificationPhoneRepository.class);
        var whatsapp = mock(WhatsAppService.class);
        new OperatorNotificationService(phones, whatsapp, Runnable::run)
                .notifyAfterCommit(new OperatorNotification("alerta"));
        verifyNoInteractions(whatsapp);
    }

    @Test
    void repositoryAndExecutorFailuresDoNotEscape() {
        var phones = mock(OperatorNotificationPhoneRepository.class);
        var whatsapp = mock(WhatsAppService.class);
        when(phones.findByActiveTrueOrderByCreatedAtAsc()).thenThrow(new IllegalStateException("DB unavailable"));
        assertDoesNotThrow(() -> new OperatorNotificationService(phones, whatsapp, Runnable::run)
                .notifyAfterCommit(new OperatorNotification("alerta")));
        Executor rejected = task -> { throw new java.util.concurrent.RejectedExecutionException(); };
        assertDoesNotThrow(() -> new OperatorNotificationService(phones, whatsapp, rejected)
                .notifyAfterCommit(new OperatorNotification("alerta")));
    }
}
