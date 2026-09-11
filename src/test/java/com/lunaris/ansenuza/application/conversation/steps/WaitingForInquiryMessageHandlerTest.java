package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Inquiry;
import com.lunaris.ansenuza.domain.model.InquiryStatus;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.InquiryRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WaitingForInquiryMessageHandlerTest {
    @Test
    void savesNewInquiryExactlyOnceBeforeResettingAndConfirming() {
        var inquiries = mock(InquiryRepository.class);
        var passengers = mock(PassengerRepository.class);
        var sessions = mock(ConversationSessionRepository.class);
        var messaging = mock(MessagingPort.class);
        var service = new InquiryService(inquiries, passengers);
        var handler = new WaitingForInquiryMessageHandler(service, sessions, messaging);
        var session = ConversationSession.builder().phoneNumber("543511234567")
                .passengerName("Ana Pérez").currentStep(WaitingForInquiryMessageHandler.STEP).build();
        when(inquiries.save(any(Inquiry.class))).thenAnswer(invocation -> {
            Inquiry inquiry = invocation.getArgument(0);
            assertNull(inquiry.getId(), "Una consulta nueva no debe llegar con ID al repositorio");
            assertEquals(InquiryStatus.PENDING, inquiry.getStatus());
            assertEquals("Viaje especial para 8 pasajeros", inquiry.getMessage());
            inquiry.setId(UUID.randomUUID());
            return inquiry;
        });

        assertDoesNotThrow(() -> handler.handle(session, new IncomingMessage(session.getPhoneNumber(),
                IncomingMessage.MessageType.TEXT, "Viaje especial para 8 pasajeros", null)));

        verify(inquiries, times(1)).save(any(Inquiry.class));
        verifyNoMoreInteractions(inquiries);
        assertEquals("START", session.getCurrentStep());
        var order = inOrder(inquiries, sessions, messaging);
        order.verify(inquiries).save(any(Inquiry.class));
        order.verify(sessions).saveAndFlush(session);
        order.verify(messaging).sendText(session.getPhoneNumber(), WaitingForInquiryMessageHandler.CONFIRMATION);
    }
}
