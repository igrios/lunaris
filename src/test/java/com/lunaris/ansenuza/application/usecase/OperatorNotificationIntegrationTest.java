package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.OperatorNotification;
import com.lunaris.ansenuza.domain.repository.OperatorNotificationPhoneRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import java.util.concurrent.Executor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:operator-alerts;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "spring.datasource.username=sa", "spring.datasource.password="
})
@Import({OperatorPhoneService.class, OperatorNotificationService.class, InquiryService.class})
class OperatorNotificationIntegrationTest {
    @Autowired OperatorPhoneService operators;
    @Autowired OperatorNotificationPhoneRepository phones;
    @Autowired InquiryService inquiries;
    @Autowired ApplicationEventPublisher events;
    @MockitoBean WhatsAppService whatsApp;
    @MockitoBean(name = "taskExecutor") Executor executor;

    @Test
    void committedInquirySchedulesAlertButRollbackDoesNot() {
        inquiries.register("5493512282251", "Ana", "Consulta");
        verifyNoInteractions(executor);
        TestTransaction.flagForRollback();
        TestTransaction.end();
        verifyNoInteractions(executor);
        TestTransaction.start();
        inquiries.register("5493512282251", "Ana", "Consulta confirmada");
        TestTransaction.flagForCommit();
        TestTransaction.end();
        verify(executor).execute(any(Runnable.class));
        verifyNoInteractions(whatsApp);
    }

    @Test
    void operatorCanBeCreatedDeactivatedAndDeletedWithGeneratedUuid() {
        operators.add("Ignacio", "+5493512282251");
        phones.flush();
        var operator = operators.list().getFirst();
        assertNotNull(operator.getId());
        assertEquals("5493512282251", operator.getPhone());
        assertTrue(operator.isActive());
        operators.setActive(operator.getId(), false);
        phones.flush();
        assertTrue(phones.findByActiveTrueOrderByCreatedAtAsc().isEmpty());
        operators.delete(operator.getId());
        phones.flush();
        assertEquals(0, phones.count());
    }

    @Test
    void duplicatePhoneAndInvalidInputAreRejected() {
        operators.add("Ignacio", "5493512282251");
        assertThrows(com.lunaris.ansenuza.domain.exception.DomainValidationException.class,
                () -> operators.add("Otro", "+5493512282251"));
        assertThrows(com.lunaris.ansenuza.domain.exception.DomainValidationException.class,
                () -> operators.add("", "abc"));
    }
}
