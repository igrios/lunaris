package com.lunaris.ansenuza.application.conversation;

import com.lunaris.ansenuza.application.conversation.steps.*;
import com.lunaris.ansenuza.application.port.*;
import com.lunaris.ansenuza.application.usecase.*;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class BotInquiryFlowTest {
    private final ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
    private final MessagingPort messaging = mock(MessagingPort.class);
    private final InquiryService inquiries = mock(InquiryService.class);
    private final OperationControlService operations = mock(OperationControlService.class);
    private final ConversationSession session = ConversationSession.builder()
            .phoneNumber("5493515550101").currentStep("MAIN_MENU").build();
    private final WaitingForInquiryMessageHandler inquiryHandler = new WaitingForInquiryMessageHandler(inquiries, sessions, messaging);
    private final MainMenuHandler menuHandler = new MainMenuHandler(sessions, mock(ReservationRepository.class),
            mock(ConversationPresenter.class), messaging, operations, mock(CancelReservationHandler.class));

    private ConversationOrchestrator orchestrator() {
        when(sessions.findByPhoneNumber(session.getPhoneNumber())).thenReturn(Optional.of(session));
        return new ConversationOrchestrator(List.of(menuHandler, inquiryHandler), sessions,
                mock(LiveChatPort.class), operations, mock(ReservationCancellationService.class),
                mock(DriverRepository.class), mock(ReservationRepository.class), mock(WhatsAppService.class),
                mock(ProcessPromotionCommandUseCase.class), mock(OnboardPassengerUseCase.class));
    }

    private IncomingMessage text(String body) {
        return new IncomingMessage(session.getPhoneNumber(), IncomingMessage.MessageType.TEXT, body, null);
    }

    @Test
    void optionFourWorksOutsideOfficeHoursAndRegistersMessageThenResets() {
        var bot = orchestrator();
        bot.process(text("4"));
        assertEquals(WaitingForInquiryMessageHandler.STEP, session.getCurrentStep());
        verify(messaging).sendText(session.getPhoneNumber(), WaitingForInquiryMessageHandler.PROMPT);
        bot.process(text("Necesito viajar el viernes de Córdoba a Miramar, 8 pasajeros"));
        verify(inquiries).register(session.getPhoneNumber(), null, "Necesito viajar el viernes de Córdoba a Miramar, 8 pasajeros");
        assertEquals("START", session.getCurrentStep());
        verify(messaging).sendText(session.getPhoneNumber(), WaitingForInquiryMessageHandler.CONFIRMATION);
    }

    @Test
    void greetingIsStoredAsInquiryInsteadOfResettingFlow() {
        session.setCurrentStep(WaitingForInquiryMessageHandler.STEP);
        orchestrator().process(text("hola"));
        verify(inquiries).register(session.getPhoneNumber(), null, "hola");
        assertEquals("START", session.getCurrentStep());
    }

    @Test
    void blankOrNonTextMessageKeepsWaitingWithoutSaving() {
        session.setCurrentStep(WaitingForInquiryMessageHandler.STEP);
        inquiryHandler.handle(session, text(" "));
        inquiryHandler.handle(session, new IncomingMessage(session.getPhoneNumber(), IncomingMessage.MessageType.IMAGE, "Foto", "media"));
        verifyNoInteractions(inquiries);
        assertEquals(WaitingForInquiryMessageHandler.STEP, session.getCurrentStep());
    }

    @Test
    void persistenceFailureDoesNotConfirmOrReset() {
        session.setCurrentStep(WaitingForInquiryMessageHandler.STEP);
        doThrow(new IllegalStateException("DB unavailable")).when(inquiries).register(anyString(), isNull(), anyString());
        assertThrows(IllegalStateException.class, () -> inquiryHandler.handle(session, text("Consulta")));
        assertEquals(WaitingForInquiryMessageHandler.STEP, session.getCurrentStep());
        verifyNoInteractions(messaging);
    }

    @Test
    void startMenuIncludesInquiryAndMovedReservationOption() {
        new StartHandler(sessions, mock(PassengerRepository.class), messaging, operations).handle(session, text("hola"));
        verify(messaging).sendText(eq(session.getPhoneNumber()), argThat(body ->
                body.contains("4️⃣ 💬 *Deja tu consulta / Viaje Especial*") && body.contains("6️⃣ 📋 *Consultar mis reservas*")));
    }
}
