package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppMessagingAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:manual-bookings;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "lunaris.public-base-url=https://lunaris.test", "lunaris.manual-notification.retry-ms=3600000"
})
@Import({CreateManualReservationUseCase.class, ReservationService.class, ManualReservationNotificationService.class,
        IssueInvoiceUseCase.class, InvoicePersistenceService.class,
        WhatsAppConversationWindowService.class, MarkTripAsPaidService.class})
class ManualReservationIntegrationTest {
    @Autowired ReservationService service;
    @Autowired CreateManualReservationUseCase manual;
    @Autowired ReservationRepository reservations;
    @Autowired PassengerRepository passengers;
    @Autowired InvoiceRepository invoices;
    @Autowired ChatMessageRepository chats;
    @Autowired ManualReservationNotificationService notifications;
    @Autowired IssueInvoiceUseCase invoicing;
    @Autowired ApplicationEventPublisher events;
    @Autowired MarkTripAsPaidService payments;
    @Autowired ReservationEventRepository audit;
    @MockitoBean WhatsAppMessagingAdapter messaging;
    @MockitoBean InvoiceStoragePort storage;
    @MockitoBean OnboardPassengerUseCase onboarding;
    @MockitoBean CapacityLockRepository capacity;
    @MockitoBean PricingAndScheduleService pricing;
    private static int nextPhone;

    @Test
    void specialBookingUsesFreeRouteAndTotalPriceWithoutRegularCapacityOrFiscalDocument() {
        Reservation input = specialBooking();
        chats.saveAndFlush(ChatMessage.builder().phoneNumber(input.getPassenger().getPhone())
                .messageText("Hola").fromOperator(false)
                .timestamp(com.lunaris.ansenuza.shared.ArgentinaTime.now()).build());
        Reservation saved = manual.execute(input, null).getFirst();
        verifyNoInteractions(messaging);
        commit();
        Reservation stored = reservations.findById(saved.getId()).orElseThrow();
        assertThat(stored.getPickupLocality()).isEqualTo("De Suardi");
        assertThat(stored.getDestination()).isEqualTo("Alta Gracia");
        assertThat(stored.getAmount()).isEqualByComparingTo("120000.50");
        assertThat(stored.getCustomPrice()).isEqualByComparingTo("120000.50");
        assertThat(stored.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(stored.getPaymentVerified()).isFalse();
        assertThat(stored.isInvoiceIssued()).isFalse();
        assertThat(stored.getInvoiceUrl()).isNull();
        assertThat(stored.getPaymentExpiresAt()).isNull();
        assertThat(stored.getPassengerCount()).isEqualTo(20);
        assertThat(reservations.findSpecialAgendaTrips(stored.getTravelDate()))
                .extracting(Reservation::getId).contains(saved.getId());
        assertThat(reservations.countReservedSeats(stored.getTravelDate(), "08:00")).isZero();
        assertThat(reservations.findReturnCapacityCandidates(stored.getTravelDate()))
                .extracting(Reservation::getId).doesNotContain(saved.getId());
        assertThat(reservations.findActiveManifest(stored.getTravelDate(), "08:00", false))
                .extracting(Reservation::getId).doesNotContain(saved.getId());
        verifyNoInteractions(pricing, capacity, storage);
        assertThat(invoices.findByReservationId(saved.getId())).isEmpty();
        verify(messaging).sendTemplate(eq(input.getPassenger().getPhone()), eq("contacto_pasajero"), eq(List.of("Ana")), any());
        verify(messaging, never()).sendText(anyString(), anyString(), any());
        events.publishEvent(new PassengerMessageReceived(input.getPassenger().getPhone()));
        verify(messaging).sendText(eq(input.getPassenger().getPhone()), contains("Pago: Pendiente"), any());
        verify(messaging, never()).sendDocumentUrl(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void pendingSpecialCannotIssueOrPersistInvoiceAndPaymentIsIdempotent() {
        Reservation saved = manual.execute(specialBooking(), null).getFirst();
        commit();
        assertThatThrownBy(() -> invoicing.issue(saved.getId(), new byte[] {1}))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> invoicePersistence.persistUploadedInvoice(new InvoicePersistenceService.InvoiceData(
                saved.getId(), "F-SPECIAL", "Ana Pérez", "27123456789", saved.getCustomPrice(), "/test.pdf")))
                .isInstanceOf(com.lunaris.ansenuza.domain.exception.DomainValidationException.class);
        verifyNoInteractions(storage);
        Reservation paid = payments.markTripAsPaid(saved.getId(), "operador-test");
        var paidAt = paid.getPaymentConfirmedAt();
        assertThat(paid.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(paid.isInvoiceIssued()).isFalse();
        long auditCount = audit.count();
        assertThat(payments.markTripAsPaid(saved.getId(), "operador-test").getPaymentConfirmedAt()).isEqualTo(paidAt);
        assertThat(audit.count()).isEqualTo(auditCount);
        Invoice invoice = invoicing.issue(saved.getId(), new byte[] {37, 80, 68, 70});
        assertThat(invoice.getAmount()).isEqualByComparingTo("120000.50");
        assertThat(reservations.findById(saved.getId()).orElseThrow().isInvoiceIssued()).isTrue();
    }

    @Autowired InvoicePersistenceService invoicePersistence;

    @Test
    void specialRoundTripPaysBothLegsAndBillsAgreedTotalOnce() {
        Reservation input = specialBooking();
        input.setRoundTrip(true);
        input.setTripType(TripType.ROUND_TRIP);
        input.setReturnDate(input.getTravelDate().plusDays(2));
        var legs = manual.execute(input, "19:15");
        commit();
        assertThat(legs).hasSize(2);
        assertThat(BookingInvoiceAmount.total(legs)).isEqualByComparingTo("120000.50");
        assertThat(legs.getLast().getOriginCustom()).isEqualTo("Alta Gracia");
        assertThat(legs.getLast().getDestinationCustom()).isEqualTo("De Suardi");
        payments.markTripAsPaid(legs.getLast().getId(), "operador-test");
        var paid = reservations.findReservationGroup(legs.getFirst().getBookingGroupCode());
        assertThat(paid).allMatch(r -> r.getPaymentStatus() == PaymentStatus.PAID && !r.isInvoiceIssued());
        Invoice invoice = invoicing.issue(legs.getLast().getId(), new byte[] {37, 80, 68, 70});
        assertThat(invoice.getAmount()).isEqualByComparingTo("120000.50");
        assertThat(reservations.findReservationGroup(legs.getFirst().getBookingGroupCode()))
                .allMatch(Reservation::isInvoiceIssued);
        verifyNoInteractions(pricing, capacity);
    }

    @Test
    void invalidSpecialIsRejectedBeforeWritingPassengerOrSchedulingNotification() {
        Reservation input = specialBooking();
        input.setOriginCustom(" ");
        long count = passengers.count();
        assertThatThrownBy(() -> manual.execute(input, null))
                .isInstanceOf(com.lunaris.ansenuza.domain.exception.DomainValidationException.class);
        assertThat(passengers.count()).isEqualTo(count);
        verifyNoInteractions(messaging, storage, pricing, capacity);
    }

    @Test
    void specialCreationCannotClaimPaidStatus() {
        Reservation input = specialBooking();
        input.setPaymentStatus(PaymentStatus.PAID);
        assertThatThrownBy(() -> manual.execute(input, null))
                .isInstanceOf(com.lunaris.ansenuza.domain.exception.DomainValidationException.class);
    }

    @Test
    void cancelledSpecialTripCannotBePaid() {
        Reservation saved = manual.execute(specialBooking(), null).getFirst();
        saved.setStatus("CANCELLED");
        reservations.saveAndFlush(saved);
        commit();
        assertThatThrownBy(() -> payments.markTripAsPaid(saved.getId(), "operador-test"))
                .isInstanceOf(com.lunaris.ansenuza.domain.exception.DomainValidationException.class);
        assertThat(reservations.findById(saved.getId()).orElseThrow().getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void specialRollbackDoesNotSendConfirmation() {
        manual.execute(specialBooking(), null);
        TestTransaction.flagForRollback();
        TestTransaction.end();
        verifyNoInteractions(messaging, storage);
    }

    @Test
    void concurrentPaymentAttemptsCreateOneAuditEventAndPreserveOnePaymentDate() throws Exception {
        Reservation saved = manual.execute(specialBooking(), null).getFirst();
        commit();
        long eventsBefore = audit.count();
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                start.await();
                return payments.markTripAsPaid(saved.getId(), "operador-uno").getPaymentConfirmedAt();
            });
            var second = executor.submit(() -> {
                start.await();
                return payments.markTripAsPaid(saved.getId(), "operador-dos").getPaymentConfirmedAt();
            });
            start.countDown();
            assertThat(first.get(15, java.util.concurrent.TimeUnit.SECONDS))
                    .isEqualTo(second.get(15, java.util.concurrent.TimeUnit.SECONDS));
        }
        assertThat(audit.count()).isEqualTo(eventsBefore + 1);
        assertThat(reservations.findById(saved.getId()).orElseThrow().getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    private Reservation specialBooking() {
        Reservation reservation = booking(true);
        reservation.setTripCategory(TripCategory.SPECIAL);
        reservation.setOriginCustom(" De Suardi ");
        reservation.setDestinationCustom("Alta Gracia");
        reservation.setCustomPrice(new BigDecimal("120000.50"));
        reservation.setPassengerCount(20);
        reservation.setPaymentReceiptUrl(null);
        return reservation;
    }

    @BeforeEach
    void successfulMessaging() {
        when(capacity.findForUpdate(anyString())).thenAnswer(call -> new CapacityLock(call.getArgument(0)));
        doAnswer(call -> {
            assertThat(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            call.<Consumer<Boolean>>getArgument(3).accept(true);
            return null;
        })
                .when(messaging).sendTemplate(anyString(), anyString(), anyList(), any());
        doAnswer(call -> { call.<Consumer<Boolean>>getArgument(2).accept(true); return null; })
                .when(messaging).sendText(anyString(), anyString(), any());
        doAnswer(call -> { call.<Consumer<Boolean>>getArgument(4).accept(true); return null; })
                .when(messaging).sendDocumentUrl(anyString(), anyString(), anyString(), anyString(), any());
        when(storage.store(any(), anyString())).thenReturn(new InvoiceStoragePort.StoredInvoice(
                "https://storage.test/invoice.pdf", "/tmp/invoice.pdf"));
    }

    @Test
    void customAmountsSurviveCreditAndInvoiceRemainsForManualProcessing() {
        Reservation input = booking(true);
        input.getPassenger().setCurrentBalance(new BigDecimal("999999"));
        var saved = service.saveManualReservationFlow(input, null).getFirst();
        commit();
        Reservation stored = reservations.findById(saved.getId()).orElseThrow();
        assertThat(stored.getAmount()).isEqualByComparingTo("12345.67");
        assertThat(stored.getDiscountAmount()).isEqualByComparingTo("321.00");
        assertThat(stored.getExtraAmount()).isEqualByComparingTo("450.50");
        assertThat(stored.getPassenger().getCurrentBalance()).isEqualByComparingTo("999999");
        assertThat(stored.getCompanionNames()).isEqualTo("Juan Pérez");
        assertThat(stored.getInvoiceUrl()).isNull();
        assertThat(stored.getPaymentReceiptUrl()).isEqualTo("https://receipt.test/proof.jpg");
        assertThat(BookingInvoiceAmount.total(List.of(stored))).isEqualByComparingTo("12796.17");
        verifyNoInteractions(pricing, storage);
        assertThat(invoices.findByReservationId(stored.getId())).isEmpty();
    }

    @Test
    void closedWindowUsesLiveChatContactTemplateAndReplySendsDetailsOnlyOnce() {
        Reservation saved = service.saveManualReservationFlow(booking(true), null).getFirst();
        commit();
        var parameters = ArgumentCaptor.forClass(List.class);
        verify(messaging).sendTemplate(eq(saved.getPassenger().getPhone()), eq("contacto_pasajero"), parameters.capture(), any());
        assertThat(parameters.getValue()).containsExactly("Ana");
        verify(messaging, never()).sendText(anyString(), anyString(), any());
        var reply = new PassengerMessageReceived(saved.getPassenger().getPhone());
        events.publishEvent(reply);
        events.publishEvent(reply);
        assertThat(invoices.findByReservationId(saved.getId())).isEmpty();
        verify(messaging).sendText(eq(saved.getPassenger().getPhone()), contains(ManualReservationNotificationService.PROMO), any());
        verify(messaging, never()).sendDocumentUrl(anyString(), anyString(), anyString(), anyString(), any());
        assertThat(reservations.findById(saved.getId()).orElseThrow().isManualNotificationPending()).isFalse();
    }

    @Test
    void openWindowStillStartsWithApprovedTemplateAndWaitsForReply() {
        Reservation input = booking(false);
        chats.saveAndFlush(ChatMessage.builder().phoneNumber(input.getPassenger().getPhone())
                .messageText("Hola").fromOperator(false)
                .timestamp(com.lunaris.ansenuza.shared.ArgentinaTime.now().minusHours(23)).build());
        Reservation saved = service.saveManualReservationFlow(input, null).getFirst();
        verifyNoInteractions(messaging);
        commit();
        verify(messaging).sendTemplate(eq(input.getPassenger().getPhone()), eq("contacto_pasajero"), eq(List.of("Ana")), any());
        verify(messaging, never()).sendText(anyString(), anyString(), any());
        assertThat(reservations.findById(saved.getId()).orElseThrow().isManualNotificationWaitingReply()).isTrue();
        events.publishEvent(new PassengerMessageReceived(input.getPassenger().getPhone()));
        verify(messaging).sendText(eq(input.getPassenger().getPhone()), contains(ManualReservationNotificationService.PROMO), any());
        verifyNoInteractions(storage);
        assertThat(reservations.findById(saved.getId()).orElseThrow().isManualNotificationPending()).isFalse();
    }

    @Test
    void failedHsmRemainsRetryable() {
        doAnswer(call -> { call.<Consumer<Boolean>>getArgument(3).accept(false); return null; })
                .when(messaging).sendTemplate(anyString(), anyString(), anyList(), any());
        Reservation saved = service.saveManualReservationFlow(booking(false), null).getFirst();
        commit();
        Reservation stored = reservations.findById(saved.getId()).orElseThrow();
        assertThat(stored.isManualNotificationPending()).isTrue();
        assertThat(stored.isManualNotificationWaitingReply()).isFalse();
        notifications.deliver(saved.getId(), false);
        verify(messaging, times(2)).sendTemplate(anyString(), anyString(), anyList(), any());
    }

    @Test
    void rolledBackReservationDoesNotIssueInvoiceOrNotify() {
        service.saveManualReservationFlow(booking(true), null);
        TestTransaction.flagForRollback();
        TestTransaction.end();
        verifyNoInteractions(storage, messaging);
    }

    @Test
    void requestedInvoiceWithoutReceiptReportsManualProcessing() {
        Reservation input = booking(true);
        input.setPaymentReceiptUrl(null);
        Reservation saved = service.saveManualReservationFlow(input, null).getFirst();
        commit();
        assertThat(invoices.findByReservationId(saved.getId())).isEmpty();
        verifyNoInteractions(storage);
        verify(messaging).sendTemplate(anyString(), eq("contacto_pasajero"), eq(List.of("Ana")), any());
        events.publishEvent(new PassengerMessageReceived(saved.getPassenger().getPhone()));
        verify(messaging).sendText(anyString(),
                contains("Factura pendiente de emisión y envío por administración"), any());
    }

    @Test
    void operatorUploadsInvoiceWithGroupAmountAndReplyDeliversPdf() {
        Reservation saved = service.saveManualReservationFlow(booking(true), null).getFirst();
        commit();
        clearInvocations(messaging);
        Invoice invoice = uploadManually(saved);
        verify(messaging).sendTemplate(eq(saved.getPassenger().getPhone()),
                eq("contacto_pasajero"), eq(List.of("Ana")), any());
        verify(messaging, never()).sendDocumentUrl(anyString(), anyString(), anyString(), anyString(), any());
        assertThat(invoice.getAmount()).isEqualByComparingTo("12796.17");
        assertThat(reservations.findById(saved.getId()).orElseThrow().getInvoiceUrl())
                .contains("/public/invoices/");
        events.publishEvent(new PassengerMessageReceived(saved.getPassenger().getPhone()));
        verify(messaging).sendDocumentUrl(anyString(), contains("/public/invoices/"), eq("Factura.pdf"), anyString(), any());
        assertThat(invoices.findById(invoice.getId()).orElseThrow().getSentViaWhatsapp()).isTrue();
    }

    @Test
    void roundTripPreservesTotalIncludingDiscountAndExtras() {
        Reservation input = booking(true);
        input.setRoundTrip(true);
        input.setTripType(TripType.OPEN_RETURN);
        var legs = service.saveManualReservationFlow(input, null);
        commit();
        assertThat(legs).hasSize(2);
        var group = reservations.findReservationGroup(legs.getFirst().getBookingGroupCode());
        assertThat(BookingInvoiceAmount.total(group)).isEqualByComparingTo("12796.17");
        assertThat(group.stream().map(Reservation::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("321.00");
        assertThat(group).allMatch(r -> r.getInvoiceUrl() == null);
        verifyNoInteractions(storage);
        Invoice invoice = uploadManually(legs.getFirst());
        assertThat(invoice.getAmount()).isEqualByComparingTo("12796.17");
        assertThat(reservations.findReservationGroup(legs.getFirst().getBookingGroupCode()))
                .allMatch(r -> r.getInvoiceUrl() != null);
    }

    @Test
    void manualUseCaseNormalizesPhoneAndKeepsExplicitDirectionAndZeroAmount() {
        Reservation input = booking(false);
        String canonical = input.getPassenger().getPhone();
        input.getPassenger().setPhone("+54 9 " + canonical.substring(2));
        input.setAmount(BigDecimal.ZERO);
        input.setExtraAmount(BigDecimal.ZERO);
        input.setRouteDirection("VUELTA");
        Reservation saved = manual.execute(input, null).getFirst();
        commit();
        Reservation stored = reservations.findById(saved.getId()).orElseThrow();
        assertThat(stored.getPassenger().getPhone()).isEqualTo(canonical);
        assertThat(stored.getAmount()).isZero();
        assertThat(stored.getRouteDirection()).isEqualTo("VUELTA");
        assertThat(stored.getRequiresInvoice()).isFalse();
        verifyNoInteractions(pricing, storage);
    }

    @Test
    void missingAmountUsesPricingAndRejectsNegativeOverride() {
        Reservation input = booking(false);
        input.setAmount(null);
        when(pricing.calculateReservationAmount("Morteros", "Córdoba", false, 2))
                .thenReturn(new BigDecimal("9000.00"));
        assertThat(service.saveManualReservationFlow(input, null).getFirst().getAmount())
                .isEqualByComparingTo("9000");
        Reservation invalid = booking(false);
        invalid.setExtraAmount(new BigDecimal("-1"));
        assertThatThrownBy(() -> service.saveManualReservationFlow(invalid, null))
                .isInstanceOf(com.lunaris.ansenuza.domain.exception.DomainValidationException.class);
    }

    @Test
    void expiredWindowUsesTemplateAndFailedDocumentRemainsPending() {
        Reservation input = booking(true);
        chats.saveAndFlush(ChatMessage.builder().phoneNumber(input.getPassenger().getPhone())
                .messageText("Hola").fromOperator(false)
                .timestamp(com.lunaris.ansenuza.shared.ArgentinaTime.now().minusHours(25)).build());
        Reservation saved = service.saveManualReservationFlow(input, null).getFirst();
        commit();
        verify(messaging).sendTemplate(anyString(), anyString(), anyList(), any());
        uploadManually(saved);
        doAnswer(call -> { call.<Consumer<Boolean>>getArgument(4).accept(false); return null; })
                .when(messaging).sendDocumentUrl(anyString(), anyString(), anyString(), anyString(), any());
        events.publishEvent(new PassengerMessageReceived(input.getPassenger().getPhone()));
        assertThat(reservations.findById(saved.getId()).orElseThrow().isManualNotificationPending()).isTrue();
    }

    private Invoice uploadManually(Reservation reservation) {
        var group = reservations.findReservationGroup(reservation.getBookingGroupCode());
        group.forEach(leg -> leg.setPaymentVerified(true));
        reservations.saveAllAndFlush(group);
        return invoicing.issue(reservation.getId(), new byte[] {37, 80, 68, 70});
    }

    private Reservation booking(boolean invoice) {
        Passenger passenger = passengers.saveAndFlush(Passenger.builder().firstName("Ana").lastName("Pérez")
                .phone("54351" + String.format("%07d", ++nextPhone)).cuil("27123456789").build());
        return Reservation.builder().passenger(passenger).pickupLocality("Morteros").destination("Córdoba")
                .pickupAddress("Belgrano 100").travelDate(LocalDate.of(2030, 1, 2).plusDays(nextPhone)).departureSchedule("08:00")
                .passengerCount(2).companionNames("Juan Pérez").roundTrip(false).tripType(TripType.ONE_WAY)
                .amount(new BigDecimal("12345.67")).discountAmount(new BigDecimal("321.00"))
                .extraAmount(new BigDecimal("450.50")).paymentVerified(false).requiresInvoice(invoice)
                .paymentReceiptUrl("https://receipt.test/proof.jpg").build();
    }

    private void commit() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }
}
