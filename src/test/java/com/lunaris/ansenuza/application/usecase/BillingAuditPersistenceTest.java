package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
@Import({GetBillingPanelUseCase.class, InvoicePersistenceService.class})
class BillingAuditPersistenceTest {
    @Autowired ReservationRepository reservations;
    @Autowired PassengerRepository passengers;
    @Autowired InvoiceRepository invoices;
    @Autowired GetBillingPanelUseCase panel;
    @Autowired InvoicePersistenceService persistence;
    @Autowired jakarta.persistence.EntityManager entityManager;

    Reservation leg(String code, boolean requested) {
        var passenger = passengers.saveAndFlush(Passenger.builder().firstName("Auditoria")
                .lastName("Local").phone("5490000000000").build());
        return reservations.saveAndFlush(Reservation.builder().passenger(passenger)
                .reservationCode(code).bookingGroupCode("AUDIT")
                .travelDate(LocalDate.of(2090, 1, 1)).pickupLocality("Origen").destination("Destino")
                .status("CONFIRMED").paymentVerified(true).requiresInvoice(requested)
                .amount(new BigDecimal("79000")).build());
    }
    InvoicePersistenceService.InvoiceData data(Reservation reservation) {
        return new InvoicePersistenceService.InvoiceData(reservation.getId(), "F-AUDIT", "Auditoria Local",
                null, new BigDecimal("158000"), "/audit.pdf");
    }
    @Test void paidGroupWithoutInvoiceRequestIsVisibleWithCompleteAmount() {
        leg("AUDIT-IDA", false); leg("AUDIT-VUELTA", false);
        assertThat(panel.execute().pendientes()).hasSize(1);
        assertThat(panel.execute().pendientes().getFirst().amount()).isEqualByComparingTo("158000");
        assertThat(invoices.count()).isZero();
    }
    @Test void billingAlwaysShowsWholeGroupAndPersistenceMarksEveryLeg() {
        var outbound = leg("AUDIT-IDA", true); var back = leg("AUDIT-VUELTA", false);
        assertThat(panel.execute().pendientes().getFirst().amount()).isEqualByComparingTo("158000");
        persistence.persistUploadedInvoice(data(outbound));
        assertThat(reservations.findById(back.getId()).orElseThrow().isInvoiceIssued()).isTrue();
        // La redirección del navegador abre un contexto de persistencia nuevo.
        entityManager.flush();
        entityManager.clear();
        assertThat(panel.execute().pendientes()).isEmpty();
    }
    @Test void persistenceAllowsDistinctInvoiceRowsForEachLegOfSameGroup() {
        var outbound = leg("AUDIT-IDA", true); var back = leg("AUDIT-VUELTA", true);
        persistence.persistUploadedInvoice(data(outbound));
        persistence.persistUploadedInvoice(data(back));
        assertThat(invoices.count()).isEqualTo(2);
    }

    @Test void issuedInvoiceExcludesLegacySpecialGroupEvenWithoutUpdatedFlags() {
        var outbound = leg("ESP-C60C259A7-IDA", true);
        outbound.setBookingGroupCode("ESP-C60C259A7");
        var back = leg("ESP-C60C259A7-VUELTA", true);
        back.setBookingGroupCode(null);
        invoices.saveAndFlush(Invoice.builder().reservationId(back.getId())
                .invoiceNumber("0001-123").amount(new BigDecimal("158000"))
                .sentViaWhatsapp(false).build());
        var pending = leg("ESP-C60C259A70", true);
        pending.setBookingGroupCode("ESP-C60C259A70");
        entityManager.flush();
        entityManager.clear();

        assertThat(reservations.findPendingInvoiceReservations())
                .extracting(Reservation::getId).containsExactly(pending.getId());
        assertThat(panel.execute().pendientes()).extracting(row -> row.reservationCode())
                .containsExactly("ESP-C60C259A70");
        assertThat(panel.execute().emitidas()).hasSize(1);
    }

    @Test void issuedMarkerOnOneLegExcludesWholeGroupWithoutInvoiceRecord() {
        var outbound = leg("AUDIT-IDA", true);
        leg("AUDIT-VUELTA", true);
        outbound.setInvoiceIssued(true);
        entityManager.flush();
        entityManager.clear();

        assertThat(reservations.findPendingInvoiceReservations()).isEmpty();
        assertThat(panel.execute().pendientes()).isEmpty();
    }

    @Test void individualWithIssuedInvoiceIsNotPendingRegardlessOfDelivery() {
        var individual = leg("INDIVIDUAL", true);
        individual.setBookingGroupCode(null);
        invoices.saveAndFlush(Invoice.builder().reservationId(individual.getId())
                .invoiceNumber("0001-124").amount(new BigDecimal("79000"))
                .sentViaWhatsapp(false).build());
        entityManager.flush();
        entityManager.clear();

        assertThat(reservations.findPendingInvoiceReservations()).isEmpty();
        assertThat(panel.execute().pendientes()).isEmpty();
        assertThat(panel.execute().emitidas()).hasSize(1);
    }
}
