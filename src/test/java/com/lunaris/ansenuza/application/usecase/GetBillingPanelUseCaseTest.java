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
                .reservationCode("BRI-COR-001")
                .pickupLocality("San Guillermo")
                .destination("Córdoba")
                .travelDate(LocalDate.of(2026, 8, 10))
                .amount(new BigDecimal("50500"))
                .paymentVerified(true)
                .requiresInvoice(true)
                .build();
        when(reservations.findPendingInvoiceReservations()).thenReturn(List.of(pending));
        when(invoices.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        var panel = new GetBillingPanelUseCase(reservations, invoices).execute();

        assertEquals(1, panel.pendientes().size());
        assertEquals("BRI-COR-001", panel.pendientes().getFirst().reservationCode());
        assertEquals(new BigDecimal("50500"), panel.pendientes().getFirst().amount());
        verify(reservations).findPendingInvoiceReservations();
    }

    @Test
    void pendingInvoicesConsolidateOutboundAndReturnLegs() {
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
                .amount(new BigDecimal("52500"))
                .paymentVerified(true)
                .requiresInvoice(true)
                .build();
        Reservation returnLeg = Reservation.builder()
                .passenger(passenger)
                .reservationCode("LUN-001-VUELTA")
                .pickupLocality("Córdoba")
                .destination("San Guillermo")
                .travelDate(LocalDate.of(2026, 8, 12))
                .amount(new BigDecimal("52500"))
                .paymentVerified(true)
                .requiresInvoice(true)
                .build();
        when(reservations.findPendingInvoiceReservations())
                .thenReturn(List.of(outbound, returnLeg));
        when(reservations.findReservationGroup("LUN-001"))
                .thenReturn(List.of(outbound, returnLeg));
        when(invoices.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        var panel = new GetBillingPanelUseCase(reservations, invoices).execute();

        assertEquals(1, panel.pendientes().size());
        var row = panel.pendientes().getFirst();
        assertEquals("LUN-001", row.reservationCode());
        assertEquals(LocalDate.of(2026, 8, 10), row.travelDate());
        assertEquals(new BigDecimal("105000"), row.amount());
        assertEquals(true, row.route().contains("Ida y Vuelta"));
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
        assertEquals("LUN-002", panel.pendientes().getFirst().reservationCode());
        assertEquals("Pasajero sin vincular", panel.pendientes().getFirst().passengerName());
    }
}
