package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(InvoicePersistenceService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:invoice-persistence;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class InvoicePersistenceServiceTest {

    @Autowired private InvoicePersistenceService service;
    @Autowired private InvoiceRepository invoices;
    @Autowired private PassengerRepository passengers;
    @Autowired private ReservationRepository reservations;
    @Autowired private EntityManager entityManager;

    @Test
    void updatesManagedInvoiceWithoutCreatingOrMergingAnotherRow() {
        Passenger passenger = passengers.saveAndFlush(Passenger.builder()
                .firstName("Ana").lastName("Pérez").phone("543511112222").build());
        Reservation reservation = reservations.saveAndFlush(Reservation.builder()
                .passenger(passenger).travelDate(LocalDate.of(2026, 9, 15))
                .pickupLocality("Morteros").destination("Córdoba")
                .paymentVerified(true).status("CONFIRMED").build());
        InvoicePersistenceService.InvoiceData original = data(
                reservation, "F-2026-00001", "/invoice/old.pdf");
        Invoice created = service.persistUploadedInvoice(original);
        entityManager.clear();

        Invoice updated = service.persistUploadedInvoice(data(
                reservation, "F-2026-99999", "/invoice/new.pdf"));
        entityManager.flush();

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getInvoiceNumber()).isEqualTo("F-2026-00001");
        assertThat(updated.getPdfUrl()).isEqualTo("/invoice/new.pdf");
        assertThat(invoices.count()).isEqualTo(1);
    }

    private InvoicePersistenceService.InvoiceData data(
            Reservation reservation, String number, String url) {
        return new InvoicePersistenceService.InvoiceData(
                reservation.getId(), number, "Ana Pérez", "27123456789",
                new BigDecimal("10000.00"), url);
    }
}
