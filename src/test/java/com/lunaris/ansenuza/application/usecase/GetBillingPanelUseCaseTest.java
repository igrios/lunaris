package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetBillingPanelUseCaseTest {

    @Test
    void pendingInvoicesComeFromPaidRequiredAndNotYetIssuedRepositoryQuery() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        Reservation pending = Reservation.builder()
                .passenger(Passenger.builder().firstName("Ana").lastName("Pérez")
                        .phone("543511111111").build())
                .pickupLocality("San Guillermo")
                .destination("Córdoba")
                .travelDate(LocalDate.of(2026, 8, 10))
                .amount(new BigDecimal("25000"))
                .paymentVerified(true)
                .requiresInvoice(true)
                .build();
        when(reservations.findPendingInvoiceReservations()).thenReturn(List.of(pending));
        when(invoices.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        var panel = new GetBillingPanelUseCase(reservations, invoices).execute();

        assertEquals(1, panel.pendientes().size());
        assertEquals(new BigDecimal("25000"), panel.pendientes().getFirst().amount());
        verify(reservations).findPendingInvoiceReservations();
    }
}
