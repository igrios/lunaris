package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;
import org.springframework.stereotype.Service;
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

    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStoragePort invoiceStorage;
    private final MessagingPort messaging;

    /** Emite (o re-sube) la factura de una reserva y la envía por WhatsApp. */
    public Invoice issue(UUID reservationId, byte[] pdfBytes) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));

        Invoice invoice = invoiceRepository.findByReservationId(reservationId).orElseGet(Invoice::new);
        if (invoice.getInvoiceNumber() == null) {
            invoice.setInvoiceNumber(nextInvoiceNumber());
        }

        String fileName = "factura_" + invoice.getInvoiceNumber().replace("-", "_") + ".pdf";
        StoredInvoice stored = invoiceStorage.store(pdfBytes, fileName);

        invoice.setReservationId(reservationId);
        invoice.setPassengerName(reservation.getPassenger().getFirstName() + " " + reservation.getPassenger().getLastName());
        invoice.setPassengerCuil(CuilCalculator.suggestCuil(reservation.getPassenger().getCuil()));
        invoice.setAmount(reservation.getAmount());
        invoice.setPdfUrl(stored.webUrl());

        boolean sent = sendByWhatsApp(reservation, invoice, stored.absolutePath());
        invoice.setSentViaWhatsapp(sent);
        if (sent) {
            invoice.setSentAt(LocalDateTime.now());
        }
        return invoiceRepository.save(invoice);
    }

    /** Reenvía por WhatsApp una factura ya registrada. */
    public Invoice resend(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + invoiceId));
        Reservation reservation = reservationRepository.findById(invoice.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada para la factura " + invoiceId));

        String absolutePath = invoiceStorage.resolveAbsolutePath(invoice.getPdfUrl());
        boolean sent = sendByWhatsApp(reservation, invoice, absolutePath);
        invoice.setSentViaWhatsapp(sent || Boolean.TRUE.equals(invoice.getSentViaWhatsapp()));
        if (sent) {
            invoice.setSentAt(LocalDateTime.now());
        }
        return invoiceRepository.save(invoice);
    }

    private boolean sendByWhatsApp(Reservation reservation, Invoice invoice, String absolutePath) {
        try {
            String phone = reservation.getPassenger().getPhone();
            String caption = """
                    🧾 *Factura %s - Lunaris Ansenuza*

                    Hola %s, te adjuntamos la factura correspondiente a tu reserva *%s*. \
                    ¡Gracias por viajar con nosotros!"""
                    .formatted(invoice.getInvoiceNumber(),
                            reservation.getPassenger().getFirstName(),
                            reservation.getReservationCode());
            messaging.sendDocument(phone, absolutePath, "Factura-" + invoice.getInvoiceNumber() + ".pdf", caption);
            return true;
        } catch (Exception e) {
            log.error("No se pudo enviar la factura {} por WhatsApp. Queda guardada para reenviar.",
                    invoice.getInvoiceNumber(), e);
            return false;
        }
    }

    private String nextInvoiceNumber() {
        long sequence = invoiceRepository.count() + 1;
        return String.format("F-%d-%05d", Year.now().getValue(), sequence);
    }
}
