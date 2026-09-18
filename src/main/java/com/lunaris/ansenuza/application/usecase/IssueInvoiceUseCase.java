package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort.StoredInvoice;
import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.model.service.CuilCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sube el PDF de la factura (armada aparte por la operadora), lo registra y lo envía
 * por WhatsApp al pasajero. Permite además reenviar una factura ya emitida.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IssueInvoiceUseCase {

    private static final String DEFAULT_PUBLIC_BASE_URL =
            "https://lunaris-backend-nn6s.onrender.com";

    public record InvoiceDocument(String invoiceNumber, byte[] content) {
        public InvoiceDocument {
            content = content.clone();
        }
    }

    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStoragePort invoiceStorage;
    private final InvoicePersistenceService invoicePersistenceService;
    private final ManualReservationNotificationService notifications;

    @Value("${lunaris.public-base-url:" + DEFAULT_PUBLIC_BASE_URL + "}")
    private String publicBaseUrl = DEFAULT_PUBLIC_BASE_URL;

    /** Emite (o re-sube) la factura de una reserva y la envía por WhatsApp. */
    public Invoice issue(UUID reservationId, byte[] pdfBytes) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        List<Reservation> group = invoiceGroup(reservation);
        if (group.stream().anyMatch(item -> !Boolean.TRUE.equals(item.getPaymentVerified())
                || !"CONFIRMED".equals(item.getStatus()))) {
            throw new IllegalStateException("La factura solo puede emitirse después de confirmar el pago.");
        }
        BigDecimal invoiceAmount = com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.total(group);
        if (invoiceAmount.signum() <= 0) {
            throw new IllegalStateException("No se emiten facturas fiscales para reservas bonificadas al 100%.");
        }

        Reservation primary = primaryReservation(group, reservation);
        String invoiceNumber = invoicePersistenceService.findInvoiceNumber(primary.getId())
                .orElseGet(this::nextInvoiceNumber);

        String fileName = "factura_" + invoiceNumber.replace("-", "_") + ".pdf";
        StoredInvoice stored = invoiceStorage.store(pdfBytes, fileName);

        InvoicePersistenceService.InvoiceData invoiceData = new InvoicePersistenceService.InvoiceData(
                primary.getId(), invoiceNumber,
                reservation.getPassenger().getFirstName() + " "
                        + reservation.getPassenger().getLastName(),
                CuilCalculator.suggestCuil(reservation.getPassenger().getCuil()),
                invoiceAmount, stored.webUrl());
        Invoice invoice = persistWithConcurrentRetry(invoiceData);

        boolean sent = sendByWhatsApp(reservation, publicInvoiceUrl(invoice));
        return invoicePersistenceService.updateDeliveryStatus(
                invoice.getId(), sent,
                sent ? com.lunaris.ansenuza.shared.ArgentinaTime.now() : null);
    }

    private Invoice persistWithConcurrentRetry(InvoicePersistenceService.InvoiceData invoiceData) {
        try {
            return invoicePersistenceService.persistUploadedInvoice(invoiceData);
        } catch (DataIntegrityViolationException concurrentInsert) {
            log.info("Otra transacción creó la factura de la reserva {}. Se actualiza la fila existente.",
                    invoiceData.reservationId());
            return invoicePersistenceService.persistUploadedInvoice(invoiceData);
        }
    }

    /** Reenvía por WhatsApp una factura ya registrada. */
    public Invoice resend(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + invoiceId));
        Reservation reservation = reservationRepository.findById(invoice.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada para la factura " + invoiceId));

        boolean sent = sendByWhatsApp(reservation, publicInvoiceUrl(invoice));
        boolean delivered = sent || Boolean.TRUE.equals(invoice.getSentViaWhatsapp());
        LocalDateTime sentAt = sent
                ? com.lunaris.ansenuza.shared.ArgentinaTime.now()
                : invoice.getSentAt();
        return invoicePersistenceService.updateDeliveryStatus(
                invoice.getId(), delivered, sentAt);
    }

    @Transactional(readOnly = true)
    public InvoiceDocument download(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + invoiceId));
        return new InvoiceDocument(
                invoice.getInvoiceNumber(), invoiceStorage.load(invoice.getPdfUrl()));
    }

    private boolean sendByWhatsApp(
            Reservation reservation, String publicDocumentUrl) {
        try {
            return notifications.invoiceReady(reservation.getId(), publicDocumentUrl);
        } catch (RuntimeException exception) {
            log.error("La factura de la reserva {} quedó guardada pero no se pudo programar el envío", reservation.getId(), exception);
            return false;
        }
    }

    private String publicInvoiceUrl(Invoice invoice) {
        String baseUrl = publicBaseUrl == null || publicBaseUrl.isBlank()
                ? DEFAULT_PUBLIC_BASE_URL
                : publicBaseUrl.strip();
        baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return baseUrl + "/public/invoices/" + invoice.getId() + ".pdf";
    }

    private String nextInvoiceNumber() {
        long sequence = invoiceRepository.count() + 1;
        return String.format("F-%d-%05d", Year.now().getValue(), sequence);
    }

    private List<Reservation> invoiceGroup(Reservation reservation) {
        String groupCode = com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.groupCode(reservation);
        if (groupCode.startsWith("UUID:")) return List.of(reservation);
        List<Reservation> group = reservationRepository.findReservationGroup(groupCode);
        return group.isEmpty() ? List.of(reservation) : group;
    }

    private Reservation primaryReservation(List<Reservation> group, Reservation fallback) {
        return group.stream()
                .filter(item -> item.getReservationCode() != null
                        && item.getReservationCode().endsWith("-IDA"))
                .findFirst()
                .orElse(fallback);
    }

}
