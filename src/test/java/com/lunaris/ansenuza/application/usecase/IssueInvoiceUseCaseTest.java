package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort.StoredInvoice;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;

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
        when(reservations.findById(returnId)).thenReturn(Optional.of(returnLeg));
        when(reservations.findReservationGroup("MOR-COR-001"))
                .thenReturn(List.of(outbound, returnLeg));
        when(invoices.findByReservationId(outboundId)).thenReturn(Optional.empty());
        when(invoices.count()).thenReturn(0L);
        when(storage.store(any(byte[].class), anyString()))
                .thenReturn(new StoredInvoice("/invoices/factura.pdf", "/tmp/factura.pdf"));
        when(invoices.saveAndFlush(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(invoices.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice issued = new IssueInvoiceUseCase(reservations, invoices, storage, messaging)
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
