package com.lunaris.ansenuza.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.application.usecase.BotMonitorService;
import java.time.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@DataJpaTest
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class BusinessIncidentRepositoryTest {
    @Autowired ConversationSessionRepository sessions;
    @Autowired PassengerRepository passengers;
    @Autowired ChatMessageRepository messages;
    @Autowired ReservationRepository reservations;
    @Autowired InvoiceRepository invoices;
    @Autowired ReservationEventRepository events;

    @Test
    void monitorIncludesUnassignedSessionsAndOnlyLatestMessageEvenWithDuplicatePassengerPhone() {
        passengers.saveAndFlush(Passenger.builder().firstName("Ana").lastName("Pérez").phone("123").build());
        passengers.saveAndFlush(Passenger.builder().firstName("Ana").lastName("Pérez").phone("123").build());
        var session = sessions.saveAndFlush(ConversationSession.builder().phoneNumber("123")
                .currentStep("ASK_DATE").lastInteraction(LocalDateTime.now()).build());
        sessions.saveAndFlush(ConversationSession.builder().phoneNumber("456").build());
        messages.saveAndFlush(ChatMessage.builder().phoneNumber("123").messageText("viejo")
                .timestamp(LocalDateTime.now().minusMinutes(1)).build());
        messages.saveAndFlush(ChatMessage.builder().phoneNumber("123").messageText("nuevo")
                .timestamp(LocalDateTime.now()).build());
        var rows = sessions.findMonitorRows();
        assertThat(rows).hasSize(2);
        var row = rows.stream().filter(r -> r.getId().equals(session.getId())).findFirst().orElseThrow();
        assertThat(row.getPassengerName()).isEqualTo("Ana Pérez");
        assertThat(row.getLastMessage()).isEqualTo("nuevo");
        assertThat(row.getCurrentStep()).isEqualTo("ASK_DATE");
    }

    @Test
    void pauseAndResumeAreExplicitAndIdempotent() {
        var session = sessions.saveAndFlush(ConversationSession.builder().phoneNumber("123").build());
        var service = new BotMonitorService(sessions, mock(SimpMessagingTemplate.class));
        service.setPaused(session.getId(), true);
        service.setPaused(session.getId(), true);
        assertThat(sessions.findById(session.getId()).orElseThrow().isBotPaused()).isTrue();
        assertThat(session.isManuallyPaused()).isTrue();
        service.setPaused(session.getId(), false);
        assertThat(sessions.findById(session.getId()).orElseThrow().isBotPaused()).isFalse();
        assertThat(session.isManuallyPaused()).isFalse();
    }

    @Test
    void invoiceGroupUsesBookingCodeEvenWhenLegCodesDoNotSharePrefix() {
        Reservation outbound = leg("A", "BOOKING-1", "CONFIRMED", 1);
        Reservation returned = leg("B", "BOOKING-1", "CONFIRMED", 1);
        leg("C", "BOOKING-2", "CONFIRMED", 1);
        assertThat(reservations.findReservationGroup("BOOKING-1"))
                .extracting(Reservation::getId).containsExactlyInAnyOrder(outbound.getId(), returned.getId());
        invoices.saveAndFlush(Invoice.builder().reservationId(outbound.getId()).reservation(outbound).sentViaWhatsapp(false)
                .invoiceNumber("F-1").amount(new BigDecimal("89000")).build());
        assertThat(reservations.findPendingInvoiceReservations())
                .extracting(Reservation::getId).doesNotContain(outbound.getId(), returned.getId());
    }

    @Test
    void occupiedSeatsExcludeCanceledAndAcceptScheduleWithOrWithoutSuffix() {
        leg("A", "G1", "CONFIRMED", 17);
        leg("B", "G2", "CANCELLED", 4);
        assertThat(reservations.countReservedSeats(LocalDate.of(2030, 1, 1), "08:00 AM")).isEqualTo(17);
    }

    @Test
    void manualBookingForPassengerWithCanceledHistoryCreatesScheduledReservationAndPreservesEvents() {
        var cancelled = leg("OLD", "OLD-GROUP", "CANCELLED", 1);
        var eventRepository = this.events;
        var historicalEvent = eventRepository.saveAndFlush(ReservationEvent.builder()
                .reservationId(cancelled.getId()).eventType("RESERVATION_CANCELLED")
                .description("Cancelación anterior").triggeredBy("ADMIN_PANEL").build());
        var service = new com.lunaris.ansenuza.domain.model.service.ReservationService(
                reservations, eventRepository, passengers,
                mock(com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase.class));
        var draft = Reservation.builder().passenger(cancelled.getPassenger())
                .pickupLocality("Morteros").destination("Córdoba").travelDate(LocalDate.of(2030, 1, 2))
                .passengerCount(1).amount(new BigDecimal("89000"))
                .paymentVerified(false).status("CANCELLED").travelStatus(Reservation.TravelStatus.CANCELED).build();
        var saved = service.saveManualReservationFlow(draft, null).getFirst();
        assertThat(saved.getId()).isNotNull().isNotEqualTo(cancelled.getId());
        assertThat(saved.getStatus()).isEqualTo("CONFIRMED");
        assertThat(saved.getTravelStatus()).isEqualTo(Reservation.TravelStatus.SCHEDULED);
        assertThat(reservations.findById(cancelled.getId()).orElseThrow().getStatus()).isEqualTo("CANCELLED");
        assertThat(eventRepository.findById(historicalEvent.getId())).isPresent();
        assertThat(eventRepository.count()).isEqualTo(2);
    }

    private Reservation leg(String code, String group, String status, int count) {
        var passenger = passengers.saveAndFlush(Passenger.builder().firstName("Ana").lastName("Pérez")
                .phone(code).build());
        return reservations.saveAndFlush(Reservation.builder().passenger(passenger)
                .reservationCode(code).bookingGroupCode(group).pickupLocality("Morteros").destination("Córdoba")
                .travelDate(LocalDate.of(2030, 1, 1)).departureSchedule("08:00").status(status)
                .paymentVerified(true).passengerCount(count).amount(new BigDecimal("44500")).build());
    }
}
