package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort.StoredInvoice;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import jakarta.persistence.EntityManager;

class IssueInvoiceUseCaseTest {

    @Test
    void issuingRoundTripInvoiceLinksFullAmountToOutboundReservation() {
        UUID outboundId = UUID.randomUUID();
        UUID returnId = UUID.randomUUID();
        Passenger passenger = Passenger.builder()
                .firstName("Ana")
                .lastName("Pérez")
                .phone("543511112222")
                .cuil("27123456789")
                .build();
        Reservation outbound = paidLeg(outboundId, "MOR-COR-001-IDA", passenger);
        Reservation returnLeg = paidLeg(returnId, "MOR-COR-001-VUELTA", passenger);
        ReservationRepository reservations = mock(ReservationRepository.class);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        InvoiceStoragePort storage = mock(InvoiceStoragePort.class);
        MessagingPort messaging = mock(MessagingPort.class);
        EntityManager entityManager = mock(EntityManager.class);
        when(reservations.findById(returnId)).thenReturn(Optional.of(returnLeg));
        when(reservations.findReservationGroup("MOR-COR-001"))
                .thenReturn(List.of(outbound, returnLeg));
        when(invoices.findByReservationId(outboundId)).thenReturn(Optional.empty());
        when(invoices.findByReservationIdForUpdate(outboundId)).thenReturn(Optional.empty());
        when(invoices.count()).thenReturn(0L);
        when(storage.store(any(byte[].class), anyString()))
                .thenReturn(new StoredInvoice("/invoices/factura.pdf", "/tmp/factura.pdf"));
        AtomicReference<Invoice> persisted = new AtomicReference<>();
        doAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(UUID.randomUUID());
            persisted.set(invoice);
            return null;
        }).when(entityManager).persist(any(Invoice.class));
        when(invoices.findByIdForUpdate(any(UUID.class)))
                .thenAnswer(invocation -> Optional.ofNullable(persisted.get()));

        Invoice issued = new IssueInvoiceUseCase(reservations, invoices, storage, messaging,
                new InvoicePersistenceService(invoices, entityManager))
                .issue(returnId, new byte[] {1});

        assertEquals(outboundId, issued.getReservationId());
        assertEquals(new BigDecimal("20000.00"), issued.getAmount());
        verify(invoices).findByReservationId(outboundId);
        verify(messaging).sendDocumentUrl(
                eq("543511112222"),
                eq("https://lunaris-backend-nn6s.onrender.com/public/invoices/"
                        + issued.getId() + ".pdf"),
                eq("Factura-" + issued.getInvoiceNumber() + ".pdf"),
                anyString());
    }

    @Test
    void invoicesDiscountedNetAmountPlusExtrasWithoutSubtractingDiscountTwice() {
        UUID reservationId = UUID.randomUUID();
        Passenger passenger = Passenger.builder()
                .firstName("Ana").lastName("Pérez").phone("543511112222")
                .cuil("27123456789").build();
        Reservation reservation = paidLeg(reservationId, "MOR-COR-002", passenger);
        reservation.setAmount(new BigDecimal("7500.00"));
        reservation.setDiscountAmount(new BigDecimal("2500.00"));
        reservation.setExtraAmount(new BigDecimal("1000.00"));
        ReservationRepository reservations = mock(ReservationRepository.class);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        InvoiceStoragePort storage = mock(InvoiceStoragePort.class);
        MessagingPort messaging = mock(MessagingPort.class);
        EntityManager entityManager = mock(EntityManager.class);
        when(reservations.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(invoices.findByReservationId(reservationId)).thenReturn(Optional.empty());
        when(invoices.findByReservationIdForUpdate(reservationId)).thenReturn(Optional.empty());
        when(storage.store(any(byte[].class), anyString()))
                .thenReturn(new StoredInvoice("/invoices/factura.pdf", "/tmp/factura.pdf"));
        AtomicReference<Invoice> persisted = new AtomicReference<>();
        doAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(UUID.randomUUID());
            persisted.set(invoice);
            return null;
        }).when(entityManager).persist(any(Invoice.class));
        when(invoices.findByIdForUpdate(any(UUID.class)))
                .thenAnswer(invocation -> Optional.ofNullable(persisted.get()));

        Invoice issued = new IssueInvoiceUseCase(reservations, invoices, storage, messaging,
                new InvoicePersistenceService(invoices, entityManager))
                .issue(reservationId, new byte[] {1});

        assertEquals(new BigDecimal("8500.00"), issued.getAmount());
    }

    @Test
    void reuploadUpdatesManagedInvoiceWithoutSavingDetachedEntity() {
        UUID reservationId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        Passenger passenger = Passenger.builder()
                .firstName("Ana").lastName("Pérez").phone("543511112222")
                .cuil("27123456789").build();
        Reservation reservation = paidLeg(reservationId, "MOR-COR-003", passenger);
        Invoice managed = Invoice.builder()
                .id(invoiceId).reservationId(reservationId).invoiceNumber("F-2026-00003")
                .pdfUrl("/old.pdf").sentViaWhatsapp(true).build();
        ReservationRepository reservations = mock(ReservationRepository.class);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        InvoiceStoragePort storage = mock(InvoiceStoragePort.class);
        MessagingPort messaging = mock(MessagingPort.class);
        EntityManager entityManager = mock(EntityManager.class);
        when(reservations.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(invoices.findByReservationId(reservationId)).thenReturn(Optional.of(managed));
        when(invoices.findByReservationIdForUpdate(reservationId)).thenReturn(Optional.of(managed));
        when(invoices.findByIdForUpdate(invoiceId)).thenReturn(Optional.of(managed));
        when(storage.store(any(byte[].class), anyString()))
                .thenReturn(new StoredInvoice("/invoices/new.pdf", "/tmp/new.pdf"));

        Invoice issued = new IssueInvoiceUseCase(reservations, invoices, storage, messaging,
                new InvoicePersistenceService(invoices, entityManager))
                .issue(reservationId, new byte[] {1});

        assertEquals(invoiceId, issued.getId());
        assertEquals("/invoices/new.pdf", issued.getPdfUrl());
        assertEquals("F-2026-00003", issued.getInvoiceNumber());
        verify(invoices, never()).saveAndFlush(any(Invoice.class));
        verify(invoices, never()).save(any(Invoice.class));
    }

    private Reservation paidLeg(UUID id, String code, Passenger passenger) {
        return Reservation.builder()
                .id(id)
                .reservationCode(code)
                .passenger(passenger)
                .amount(new BigDecimal("10000.00"))
                .extraAmount(BigDecimal.ZERO)
                .paymentVerified(true)
                .requiresInvoice(true)
                .status("CONFIRMED")
                .build();
    }
}
