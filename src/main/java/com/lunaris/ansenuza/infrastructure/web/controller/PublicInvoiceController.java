package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.IssueInvoiceUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint público y directo para que Meta descargue documentos PDF sin redirecciones. */
@RestController
@RequiredArgsConstructor
public class PublicInvoiceController {

    private final IssueInvoiceUseCase issueInvoiceUseCase;

    @GetMapping(value = "/public/invoices/{invoiceId}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable UUID invoiceId) {
        IssueInvoiceUseCase.InvoiceDocument document = issueInvoiceUseCase.download(invoiceId);
        String fileName = document.invoiceNumber().replaceAll("[^A-Za-z0-9_-]", "_");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"factura-" + fileName + ".pdf\"")
                .body(document.content());
    }
}
