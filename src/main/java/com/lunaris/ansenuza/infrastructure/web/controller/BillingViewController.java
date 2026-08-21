package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.lunaris.ansenuza.application.usecase.GetBillingPanelUseCase;
import com.lunaris.ansenuza.application.usecase.IssueInvoiceUseCase;
import com.lunaris.ansenuza.domain.model.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 🧾 Panel de Facturación: registro de ingresos diario/mensual, reservas con pago
 * confirmado pendientes de facturar (con CUIL calculado), y carga + envío del PDF por WhatsApp.
 */
@Controller
@RequestMapping("/facturacion")
@RequiredArgsConstructor
@Slf4j
public class BillingViewController {

    private final GetBillingPanelUseCase getBillingPanelUseCase;
    private final IssueInvoiceUseCase issueInvoiceUseCase;

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("panel", getBillingPanelUseCase.execute());
        return "facturacion";
    }

    /** Sirve la factura como PDF inline, independientemente del backend de almacenamiento. */
    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID invoiceId) {
        IssueInvoiceUseCase.InvoiceDocument document = issueInvoiceUseCase.download(invoiceId);
        String safeInvoiceNumber = document.invoiceNumber().replaceAll("[^A-Za-z0-9_-]", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"factura-" + safeInvoiceNumber + ".pdf\"")
                .body(document.content());
    }

    /** Sube el PDF de la factura de una reserva y la envía por WhatsApp. */
    @PostMapping("/emitir/{reservationId}")
    public String emitir(@PathVariable UUID reservationId,
            @RequestParam("pdf") MultipartFile pdf,
            RedirectAttributes redirectAttributes) {
        try {
            if (pdf == null || pdf.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tenés que adjuntar el PDF de la factura.");
                return "redirect:/facturacion";
            }
            Invoice invoice = issueInvoiceUseCase.issue(reservationId, pdf.getBytes());
            if (Boolean.TRUE.equals(invoice.getSentViaWhatsapp())) {
                redirectAttributes.addFlashAttribute("ok",
                        "Factura " + invoice.getInvoiceNumber() + " emitida y enviada por WhatsApp.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Factura " + invoice.getInvoiceNumber()
                                + " guardada, pero falló el envío por WhatsApp. Probá 'Reenviar'.");
            }
        } catch (Exception e) {
            log.error("Error al emitir la factura para la reserva {}", reservationId, e);
            redirectAttributes.addFlashAttribute("error", "No se pudo emitir la factura: " + e.getMessage());
        }
        return "redirect:/facturacion";
    }

    /** Reenvía por WhatsApp una factura ya emitida. */
    @PostMapping("/reenviar/{invoiceId}")
    public String reenviar(@PathVariable UUID invoiceId, RedirectAttributes redirectAttributes) {
        try {
            Invoice invoice = issueInvoiceUseCase.resend(invoiceId);
            if (Boolean.TRUE.equals(invoice.getSentViaWhatsapp())) {
                redirectAttributes.addFlashAttribute("ok",
                        "Factura " + invoice.getInvoiceNumber() + " reenviada por WhatsApp.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "No se pudo reenviar la factura " + invoice.getInvoiceNumber() + ".");
            }
        } catch (Exception e) {
            log.error("Error al reenviar la factura {}", invoiceId, e);
            redirectAttributes.addFlashAttribute("error", "No se pudo reenviar la factura: " + e.getMessage());
        }
        return "redirect:/facturacion";
    }
}
