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

    @Test
    void pendingInvoicesIncludeBothOutboundAndReturnLegs() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        Passenger passenger = Passenger.builder().firstName("Ana").lastName("Pérez")
                .phone("543511111111").build();
        Reservation outbound = Reservation.builder()
                .passenger(passenger)
                .reservationCode("LUN-001-IDA")
                .pickupLocality("San Guillermo")
                .destination("Córdoba")
                .travelDate(LocalDate.of(2026, 8, 10))
                .amount(new BigDecimal("12500"))
                .paymentVerified(true)
                .requiresInvoice(true)
                .build();
        Reservation returnLeg = Reservation.builder()
                .passenger(passenger)
                .reservationCode("LUN-001-VUELTA")
                .pickupLocality("Córdoba")
                .destination("San Guillermo")
                .travelDate(LocalDate.of(2026, 8, 12))
                .amount(new BigDecimal("12500"))
                .paymentVerified(true)
                .requiresInvoice(true)
                .build();
        when(reservations.findPendingInvoiceReservations())
                .thenReturn(List.of(outbound, returnLeg));
        when(reservations.findReservationGroup("LUN-001"))
                .thenReturn(List.of(outbound, returnLeg));
        when(invoices.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        var panel = new GetBillingPanelUseCase(reservations, invoices).execute();

        assertEquals(2, panel.pendientes().size());
        assertEquals(List.of("LUN-001-IDA", "LUN-001-VUELTA"),
                panel.pendientes().stream().map(row -> row.reservationCode()).toList());
        assertEquals(List.of(LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12)),
                panel.pendientes().stream().map(row -> row.travelDate()).toList());
    }

    @Test
    void pendingOutboundWithoutPassengerLinkRemainsVisible() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        Reservation outbound = Reservation.builder()
                .reservationCode("LUN-002-IDA")
                .pickupLocality("Morteros")
                .destination("Córdoba")
                .travelDate(LocalDate.of(2026, 8, 15))
                .amount(new BigDecimal("10000"))
                .paymentVerified(true)
                .requiresInvoice(true)
                .build();
        when(reservations.findPendingInvoiceReservations()).thenReturn(List.of(outbound));
        when(reservations.findReservationGroup("LUN-002")).thenReturn(List.of(outbound));
        when(invoices.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        var panel = new GetBillingPanelUseCase(reservations, invoices).execute();

        assertEquals(1, panel.pendientes().size());
        assertEquals("LUN-002-IDA", panel.pendientes().getFirst().reservationCode());
        assertEquals("Pasajero sin vincular", panel.pendientes().getFirst().passengerName());
    }
}
