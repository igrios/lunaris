package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.exception.AgendaTripNotFoundException;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.List;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoicePersistenceService {

    private final InvoiceRepository invoiceRepository;
    private final EntityManager entityManager;
    private final ReservationRepository reservations;

    @Transactional(readOnly = true)
    public Optional<String> findInvoiceNumber(UUID reservationId) {
        return invoiceRepository.findByReservationId(reservationId)
                .map(Invoice::getInvoiceNumber);
    }

    @Transactional
    public Invoice persistUploadedInvoice(InvoiceData data) {
        String groupCode = reservations.findBillingGroupCodeById(data.reservationId()).orElse(null);
        var group = groupCode == null
                ? List.of(reservations.findByIdForUpdate(data.reservationId())
                        .orElseThrow(() -> new AgendaTripNotFoundException(data.reservationId())))
                : reservations.findReservationGroupForUpdate(groupCode);
        if (group.isEmpty()) {
            group = List.of(reservations.findByIdForUpdate(data.reservationId())
                    .orElseThrow(() -> new AgendaTripNotFoundException(data.reservationId())));
        }
        // OpenEntityManagerInView puede haber cargado las reservas antes del bloqueo.
        // Refrescar con las filas ya bloqueadas evita facturar un estado de pago anterior.
        group.forEach(entityManager::refresh);
        if (group.stream().anyMatch(r -> !Boolean.TRUE.equals(r.getPaymentVerified())
                || !"CONFIRMED".equals(r.getStatus()))) {
            throw new DomainValidationException(
                    "La factura solo puede registrarse después de confirmar el pago de todos los tramos.");
        }
        if (group.stream().anyMatch(com.lunaris.ansenuza.domain.model.Reservation::isSpecialTrip)
                && com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.total(group).compareTo(data.amount()) != 0) {
            throw new DomainValidationException("El precio del viaje cambió durante la emisión. Volvé a intentar.");
        }
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
        group.forEach(r -> r.setInvoiceIssued(true));
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
