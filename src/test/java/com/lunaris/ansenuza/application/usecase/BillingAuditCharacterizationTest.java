package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Caracterización de auditoría: sin Spring, red ni bases externas. */
class BillingAuditCharacterizationTest {
    final ReservationRepository reservations = mock(ReservationRepository.class);
    final InvoiceRepository invoices = mock(InvoiceRepository.class);
    final InvoiceStoragePort storage = mock(InvoiceStoragePort.class);
    final InvoicePersistenceService persistence = mock(InvoicePersistenceService.class);
    final ManualReservationNotificationService notifications = mock(ManualReservationNotificationService.class);
    final IssueInvoiceUseCase useCase = new IssueInvoiceUseCase(reservations, invoices, storage, persistence, notifications);
    final Reservation leg = Reservation.builder().id(UUID.randomUUID())
            .passenger(Passenger.builder().firstName("Auditoria").lastName("Local").build())
            .status("CONFIRMED").paymentVerified(true).paymentStatus(PaymentStatus.PAID)
            .requiresInvoice(false).amount(new BigDecimal("79000")).passengerCount(2).build();
    final Invoice invoice = Invoice.builder().id(UUID.randomUUID()).reservationId(leg.getId())
            .invoiceNumber("F-TEST").sentViaWhatsapp(false).build();

    BillingAuditCharacterizationTest() {
        when(reservations.findById(leg.getId())).thenReturn(Optional.of(leg));
        when(storage.store(any(), anyString())).thenReturn(new InvoiceStoragePort.StoredInvoice("/audit.pdf", "/audit.pdf"));
        when(persistence.persistUploadedInvoice(any())).thenReturn(invoice);
        when(persistence.updateDeliveryStatus(any(), anyBoolean(), any())).thenReturn(invoice);
    }

    @Test void explicitUploadAcceptsRequiresInvoiceFalseAndPaidStatusIsNotChecked() {
        leg.setPaymentStatus(PaymentStatus.PENDING);
        useCase.issue(leg.getId(), new byte[]{1});
        verify(persistence).persistUploadedInvoice(argThat(d -> d.amount().compareTo(new BigDecimal("79000")) == 0));
    }
    @Test void pendingPaymentIsRejectedBeforeStorage() {
        leg.setPaymentVerified(false);
        assertThatThrownBy(() -> useCase.issue(leg.getId(), new byte[]{1})).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(storage, persistence, notifications);
    }
    @Test void cancelledReservationIsRejectedBeforeStorage() {
        leg.setStatus("CANCELLED");
        assertThatThrownBy(() -> useCase.issue(leg.getId(), new byte[]{1})).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(storage, persistence, notifications);
    }
    @Test void roundTripSumsLegsWithoutMultiplyingPassengers() {
        leg.setBookingGroupCode("AUDIT-GROUP"); leg.setReservationCode("AUDIT-GROUP-IDA");
        Reservation back = Reservation.builder().id(UUID.randomUUID()).status("CONFIRMED")
                .paymentVerified(true).amount(new BigDecimal("79000")).passengerCount(2).build();
        when(reservations.findReservationGroup("AUDIT-GROUP")).thenReturn(List.of(leg, back));
        useCase.issue(leg.getId(), new byte[]{1});
        verify(persistence).persistUploadedInvoice(argThat(d -> d.amount().compareTo(new BigDecimal("158000")) == 0));
    }
    @Test void alreadyIssuedInvoiceStillUploadsAndRequestsPersistence() {
        leg.setInvoiceIssued(true);
        when(persistence.findInvoiceNumber(leg.getId())).thenReturn(Optional.of("F-EXISTING"));
        useCase.issue(leg.getId(), new byte[]{1});
        verify(storage).store(any(), eq("factura_F_EXISTING.pdf"));
        verify(persistence).persistUploadedInvoice(any());
    }
    @Test void storageFailurePreventsInvoiceRegistration() {
        when(storage.store(any(), anyString())).thenThrow(new IllegalStateException("stub storage failure"));
        assertThatThrownBy(() -> useCase.issue(leg.getId(), new byte[]{1})).hasMessage("stub storage failure");
        verify(persistence, never()).persistUploadedInvoice(any()); verifyNoInteractions(notifications);
    }
    @Test void databaseFailureOccursAfterDocumentUpload() {
        when(persistence.persistUploadedInvoice(any())).thenThrow(new IllegalStateException("stub database failure"));
        assertThatThrownBy(() -> useCase.issue(leg.getId(), new byte[]{1})).hasMessage("stub database failure");
        verify(storage).store(any(), anyString()); verifyNoInteractions(notifications);
    }
    @Test void whatsappFailurePreservesRegistrationAndMarksUnsent() {
        when(notifications.invoiceReady(any(), any())).thenThrow(new IllegalStateException("stub notification failure"));
        useCase.issue(leg.getId(), new byte[]{1});
        verify(persistence).persistUploadedInvoice(any());
        verify(persistence).updateDeliveryStatus(invoice.getId(), false, null);
    }
    @Test void deliveryStatusFailurePropagatesAfterRegistration() {
        when(persistence.updateDeliveryStatus(any(), anyBoolean(), any())).thenThrow(new IllegalStateException("stub status failure"));
        assertThatThrownBy(() -> useCase.issue(leg.getId(), new byte[]{1})).hasMessage("stub status failure");
        verify(persistence).persistUploadedInvoice(any());
    }
    @Test void timeoutRetryRepeatsStorageWhenNoInvoiceWasRegistered() {
        when(storage.store(any(), anyString())).thenThrow(new IllegalStateException("stub timeout"))
                .thenReturn(new InvoiceStoragePort.StoredInvoice("/audit.pdf", "/audit.pdf"));
        assertThatThrownBy(() -> useCase.issue(leg.getId(), new byte[]{1})).hasMessage("stub timeout");
        useCase.issue(leg.getId(), new byte[]{1});
        verify(storage, times(2)).store(any(), anyString());
    }
    @Test void simultaneousRequestsUploadBeforePersistenceLock() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);
        when(storage.store(any(), anyString())).thenAnswer(call -> {
            barrier.await(5, TimeUnit.SECONDS);
            return new InvoiceStoragePort.StoredInvoice("/audit.pdf", "/audit.pdf");
        });
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> useCase.issue(leg.getId(), new byte[]{1}));
            var second = executor.submit(() -> useCase.issue(leg.getId(), new byte[]{2}));
            first.get(10, TimeUnit.SECONDS); second.get(10, TimeUnit.SECONDS);
        }
        verify(storage, times(2)).store(any(), anyString());
        verify(persistence, times(2)).persistUploadedInvoice(any());
    }
}
