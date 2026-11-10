package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.ChatbotTelemetryPort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.WhatsAppConversationWindowService;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
@Import({SendOperatorMessageUseCase.class, TakeOverConversationUseCase.class,
        AssistedChatService.class, WhatsAppConversationWindowService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SendOperatorMessageTransactionTest {
    @Autowired SendOperatorMessageUseCase useCase;
    @Autowired ConversationSessionRepository sessions;
    @Autowired ChatMessageRepository messages;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean WhatsAppService whatsApp;
    @MockitoBean ChatbotTelemetryPort telemetry;

    @Test
    void createsHumanSessionAndHistoryBeforeDispatchingAfterCommit() {
        String phone = "5493562123401";
        new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
            var saved = useCase.execute("+54 9 (3562) 123-401", " Hola, confirmamos el traslado ");
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.isFromOperator()).isTrue();
            var session = sessions.findByPhoneNumber(phone).orElseThrow();
            assertThat(session.isBotPaused()).isTrue();
            assertThat(session.isManuallyPaused()).isTrue();
            assertThat(session.getCurrentStep()).isEqualTo("START");
            verifyNoInteractions(whatsApp);
        });
        assertThat(messages.findByPhoneNumberOrderByTimestampAsc(phone)).hasSize(1);
        verify(whatsApp).sendMessage(phone, "Hola, confirmamos el traslado");
    }

    @Test
    void rollbackDoesNotLeaveSessionOrHistoryAndDoesNotSend() {
        String phone = "5493562123402";
        new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
            useCase.execute(phone, "Este mensaje se revierte");
            tx.setRollbackOnly();
        });
        assertThat(sessions.findByPhoneNumber(phone)).isEmpty();
        assertThat(messages.findByPhoneNumberOrderByTimestampAsc(phone)).isEmpty();
        verifyNoInteractions(whatsApp);
    }

    @Test
    void existingManuallyPausedSessionIsReused() {
        String phone = "5493562123403";
        var session = sessions.saveAndFlush(ConversationSession.builder().phoneNumber(phone)
                .currentStep("ASK_DATE").botPaused(true).manuallyPaused(true).build());
        useCase.execute(phone, "Hola");
        useCase.execute(phone, "Confirmamos");
        var existing = sessions.findByPhoneNumber(phone).orElseThrow();
        assertThat(existing.getId()).isEqualTo(session.getId());
        assertThat(existing.getCurrentStep()).isEqualTo("ASK_DATE");
        assertThat(messages.findByPhoneNumberOrderByTimestampAsc(phone)).hasSize(2);
        verify(whatsApp).sendMessage(phone, "Hola");
        verify(whatsApp).sendMessage(phone, "Confirmamos");
    }

    @Test
    void existingExpiredUnmanagedSessionIsRejectedWithoutHistoryOrDelivery() {
        String phone = "5493562123404";
        sessions.saveAndFlush(ConversationSession.builder().phoneNumber(phone).currentStep("START").build());
        assertThatThrownBy(() -> useCase.execute(phone, "Hola")).isInstanceOf(DomainValidationException.class);
        assertThat(messages.findByPhoneNumberOrderByTimestampAsc(phone)).isEmpty();
        verifyNoInteractions(whatsApp);
    }

    @Test
    void invalidInputDoesNotCreateConversation() {
        assertThatThrownBy(() -> useCase.execute("5493562123405", " "))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> useCase.execute("9", "Hola"))
                .isInstanceOf(DomainValidationException.class);
        assertThat(sessions.findByPhoneNumber("5493562123405")).isEmpty();
        verifyNoInteractions(whatsApp);
    }
}
