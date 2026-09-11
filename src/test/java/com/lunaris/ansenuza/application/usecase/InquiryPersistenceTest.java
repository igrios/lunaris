package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.steps.WaitingForInquiryMessageHandler;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.InquiryStatus;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.InquiryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:inquiry-persistence;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "spring.datasource.username=sa", "spring.datasource.password="
})
@Import({InquiryService.class, WaitingForInquiryMessageHandler.class})
class InquiryPersistenceTest {
    @Autowired WaitingForInquiryMessageHandler handler;
    @Autowired InquiryRepository inquiries;
    @Autowired ConversationSessionRepository sessions;
    @Autowired EntityManager entityManager;
    @MockitoBean MessagingPort messaging;

    @Test
    void commitsInquiryWithGeneratedUuidAndSessionReset() {
        var session = sessions.saveAndFlush(ConversationSession.builder().phoneNumber("543511234567")
                .passengerName("Ana Pérez").currentStep(WaitingForInquiryMessageHandler.STEP).build());
        handler.handle(session, new IncomingMessage(session.getPhoneNumber(), IncomingMessage.MessageType.TEXT,
                "Viaje especial para 8 pasajeros", null));
        entityManager.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
        try {
            var saved = inquiries.findAll();
            assertEquals(1, saved.size());
            var inquiry = saved.getFirst();
            assertNotNull(inquiry.getId());
            assertEquals(InquiryStatus.PENDING, inquiry.getStatus());
            assertEquals("Viaje especial para 8 pasajeros", inquiry.getMessage());
            assertEquals("Ana Pérez", inquiry.getPassengerName());
            assertEquals("START", sessions.findByPhoneNumber(session.getPhoneNumber()).orElseThrow().getCurrentStep());
            verify(messaging).sendText(session.getPhoneNumber(), WaitingForInquiryMessageHandler.CONFIRMATION);
        } finally {
            inquiries.deleteAllInBatch();
            sessions.deleteAllInBatch();
            TestTransaction.flagForCommit();
            TestTransaction.end();
        }
    }
}
