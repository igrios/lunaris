package com.lunaris.ansenuza.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class AssistedReservationDraftServiceTest {
    private static final String PHONE = "5493511234567";
    @Autowired AssistedReservationDraftRepository drafts;
    @Autowired ConversationSessionRepository sessions;
    @Autowired ChatMessageRepository messages;
    @Autowired PassengerRepository passengers;
    @Autowired EntityManager em;
    private AssistedReservationDraftService service;

    @BeforeEach void setup() {
        var assisted = mock(AssistedChatService.class);
        when(assisted.prefill(PHONE)).thenReturn(ConversationSession.builder().phoneNumber(PHONE).build());
        service = new AssistedReservationDraftService(drafts, sessions, messages, passengers, assisted,
                new ObjectMapper().findAndRegisterModules());
    }

    @Test void addressAndReferenceAreSuggestedFromLatestIncomingWithoutInventingRequiredData() {
        message("Dirección: Belgrano 450; Referencia: portón azul; Teléfono: 3517654321", false, 0);
        message("Dirección: Oficina 999", true, 1);
        var result = service.prefill(PHONE);
        assertThat(result.form().getPickupAddress()).isEqualTo("Belgrano 450");
        assertThat(result.form().getNotes()).isEqualTo("portón azul");
        assertThat(result.form().getPhone()).isEqualTo(PHONE);
        assertThat(result.detectedPhone()).isEqualTo("3517654321");
        assertThat(result.form().getFirstName()).isNull();
        assertThat(result.form().getTravelDate()).isNull();
        assertThat(result.form().getDepartureSchedule()).isNull();
    }

    @Test void incompleteDraftSurvivesSessionDeletionAndWinsOverNewSuggestions() {
        var session = sessions.saveAndFlush(ConversationSession.builder().phoneNumber(PHONE).build());
        var form = new CreateReservationForm();
        form.setPhone(PHONE);
        form.setPickupAddress("Puerta corregida por operador");
        form.setNotes("Llamar al llegar");
        form.setPaymentVerified(true);
        service.save(PHONE, form);
        sessions.delete(session);
        sessions.flush();
        em.clear();
        message("Dirección: Otra 123", false, 0);
        var restored = service.prefill(PHONE).form();
        assertThat(restored.getPickupAddress()).isEqualTo("Puerta corregida por operador");
        assertThat(restored.getNotes()).isEqualTo("Llamar al llegar");
        assertThat(restored.getLastName()).isNull();
        assertThat(restored.getPaymentVerified()).isTrue();
        assertThat(restored.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        restored.setFirstName("Ana");
        service.save(PHONE, restored);
        em.clear();
        assertThat(service.prefill(PHONE).form().getFirstName()).isEqualTo("Ana");
        assertThat(drafts.count()).isEqualTo(1);
    }

    private void message(String text, boolean operator, int seconds) {
        messages.saveAndFlush(ChatMessage.builder().phoneNumber(PHONE).messageText(text)
                .fromOperator(operator).timestamp(LocalDateTime.of(2030, 1, 1, 12, 0).plusSeconds(seconds)).build());
    }
}
