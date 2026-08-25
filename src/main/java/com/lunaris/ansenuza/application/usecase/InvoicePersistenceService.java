package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoicePersistenceService {

    private final InvoiceRepository invoiceRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Optional<String> findInvoiceNumber(UUID reservationId) {
        return invoiceRepository.findByReservationId(reservationId)
                .map(Invoice::getInvoiceNumber);
    }

    @Transactional
    public Invoice persistUploadedInvoice(InvoiceData data) {
        Invoice invoice = invoiceRepository.findByReservationIdForUpdate(data.reservationId())
                .orElseGet(Invoice::new);
        boolean newInvoice = invoice.getId() == null;
        if (newInvoice) {
            invoice.setReservationId(data.reservationId());
        }
        if (invoice.getInvoiceNumber() == null) {
            invoice.setInvoiceNumber(data.invoiceNumber());
        }
        invoice.setPassengerName(data.passengerName());
        invoice.setPassengerCuil(data.passengerCuil());
        invoice.setAmount(data.amount());
        invoice.setPdfUrl(data.pdfUrl());
        invoice.setSentViaWhatsapp(false);
        invoice.setSentAt(null);
        if (newInvoice) {
            entityManager.persist(invoice);
            entityManager.flush();
        }
        return invoice;
    }

    @Transactional
    public Invoice updateDeliveryStatus(UUID invoiceId, boolean sent, LocalDateTime sentAt) {
        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> new IllegalStateException(
                        "La factura desapareció durante la emisión: " + invoiceId));
        invoice.setSentViaWhatsapp(sent);
        invoice.setSentAt(sent ? sentAt : null);
        return invoice;
    }

    public record InvoiceData(
            UUID reservationId,
            String invoiceNumber,
            String passengerName,
            String passengerCuil,
            BigDecimal amount,
            String pdfUrl) {
    }
}
