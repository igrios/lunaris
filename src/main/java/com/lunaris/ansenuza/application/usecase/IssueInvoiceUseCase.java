package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort.StoredInvoice;
import com.lunaris.ansenuza.application.port.MessagingPort;
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
    private final MessagingPort messaging;

    @Value("${lunaris.public-base-url:" + DEFAULT_PUBLIC_BASE_URL + "}")
    private String publicBaseUrl = DEFAULT_PUBLIC_BASE_URL;

    /** Emite (o re-sube) la factura de una reserva y la envía por WhatsApp. */
    @Transactional
    public Invoice issue(UUID reservationId, byte[] pdfBytes) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        List<Reservation> group = invoiceGroup(reservation);
        if (group.stream().anyMatch(item -> !Boolean.TRUE.equals(item.getPaymentVerified())
                || !"CONFIRMED".equals(item.getStatus()))) {
            throw new IllegalStateException("La factura solo puede emitirse después de confirmar el pago.");
        }
        BigDecimal invoiceAmount = group.stream()
                .map(this::totalReservationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (invoiceAmount.signum() <= 0) {
            throw new IllegalStateException("No se emiten facturas fiscales para reservas bonificadas al 100%.");
        }

        Reservation primary = primaryReservation(group, reservation);
        Invoice invoice = invoiceRepository.findByReservationId(primary.getId()).orElseGet(Invoice::new);
        if (invoice.getInvoiceNumber() == null) {
            invoice.setInvoiceNumber(nextInvoiceNumber());
        }

        String fileName = "factura_" + invoice.getInvoiceNumber().replace("-", "_") + ".pdf";
        StoredInvoice stored = invoiceStorage.store(pdfBytes, fileName);

        invoice.setReservationId(primary.getId());
        invoice.setPassengerName(reservation.getPassenger().getFirstName() + " " + reservation.getPassenger().getLastName());
        invoice.setPassengerCuil(CuilCalculator.suggestCuil(reservation.getPassenger().getCuil()));
        invoice.setAmount(invoiceAmount);
        invoice.setPdfUrl(stored.webUrl());
        invoice.setSentViaWhatsapp(false);
        if (invoice.getId() == null) {
            invoice.setId(UUID.randomUUID());
        }
        invoice = invoiceRepository.saveAndFlush(invoice);

        boolean sent = sendByWhatsApp(reservation, invoice, publicInvoiceUrl(invoice));
        invoice.setSentViaWhatsapp(sent);
        if (sent) {
            invoice.setSentAt(com.lunaris.ansenuza.shared.ArgentinaTime.now());
        }
        return invoiceRepository.save(invoice);
    }

    /** Reenvía por WhatsApp una factura ya registrada. */
    public Invoice resend(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + invoiceId));
        Reservation reservation = reservationRepository.findById(invoice.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada para la factura " + invoiceId));

        boolean sent = sendByWhatsApp(reservation, invoice, publicInvoiceUrl(invoice));
        invoice.setSentViaWhatsapp(sent || Boolean.TRUE.equals(invoice.getSentViaWhatsapp()));
        if (sent) {
            invoice.setSentAt(com.lunaris.ansenuza.shared.ArgentinaTime.now());
        }
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceDocument download(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + invoiceId));
        return new InvoiceDocument(
                invoice.getInvoiceNumber(), invoiceStorage.load(invoice.getPdfUrl()));
    }

    private boolean sendByWhatsApp(
            Reservation reservation, Invoice invoice, String publicDocumentUrl) {
        try {
            String phone = reservation.getPassenger().getPhone();
            String caption = """
                    🧾 *Factura %s - Lunaris Ansenuza*

                    Hola %s, te adjuntamos la factura correspondiente a tu reserva *%s*. \
                    ¡Gracias por viajar con nosotros!"""
                    .formatted(invoice.getInvoiceNumber(),
                            reservation.getPassenger().getFirstName(),
                            reservation.getReservationCode());
            String fileName = "Factura-" + invoice.getInvoiceNumber() + ".pdf";
            messaging.sendDocumentUrl(phone, publicDocumentUrl, fileName, caption);
            return true;
        } catch (Exception e) {
            log.error("No se pudo enviar la factura {} por WhatsApp. Queda guardada para reenviar.",
                    invoice.getInvoiceNumber(), e);
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

    private BigDecimal totalReservationAmount(Reservation reservation) {
        BigDecimal amount = reservation.getAmount() == null ? BigDecimal.ZERO : reservation.getAmount();
        BigDecimal extraAmount = reservation.getExtraAmount() == null ? BigDecimal.ZERO : reservation.getExtraAmount();
        return amount.add(extraAmount);
    }

    private List<Reservation> invoiceGroup(Reservation reservation) {
        String code = reservation.getReservationCode();
        if (code == null || !(code.endsWith("-IDA") || code.endsWith("-VUELTA"))) {
            return List.of(reservation);
        }
        String groupCode = code.replaceFirst("-(IDA|VUELTA)$", "");
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
