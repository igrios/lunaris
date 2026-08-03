package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateInvoiceUseCaseTest {

    @Test
    void createsInvoiceRecordImmediatelyAndIdempotently() {
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .passenger(Passenger.builder().firstName("Ana").lastName("Pérez").build())
                .amount(new BigDecimal("12000"))
                .extraAmount(new BigDecimal("500"))
                .build();
        when(invoices.findByReservationId(reservation.getId()))
                .thenReturn(Optional.empty());
        when(invoices.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Invoice created = new CreateInvoiceUseCase(invoices).execute(reservation);

        assertEquals(reservation.getId(), created.getReservationId());
        assertEquals("Ana Pérez", created.getPassengerName());
        assertEquals(new BigDecimal("12500"), created.getAmount());
        assertFalse(created.getSentViaWhatsapp());
        verify(invoices).save(created);
    }
}
