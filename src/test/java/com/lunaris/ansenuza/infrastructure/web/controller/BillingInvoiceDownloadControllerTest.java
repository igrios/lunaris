package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.usecase.GetBillingPanelUseCase;
import com.lunaris.ansenuza.application.usecase.IssueInvoiceUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class BillingInvoiceDownloadControllerTest {

    @Test
    void servesInvoiceInlineWithPdfHeadersAndFileExtension() {
        IssueInvoiceUseCase invoices = mock(IssueInvoiceUseCase.class);
        UUID invoiceId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.7".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        when(invoices.download(invoiceId)).thenReturn(
                new IssueInvoiceUseCase.InvoiceDocument("F-2026-00001", pdf));
        BillingViewController controller = new BillingViewController(
                mock(GetBillingPanelUseCase.class), invoices);

        var response = controller.downloadPdf(invoiceId);

        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("inline; filename=\"factura-F-2026-00001.pdf\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertArrayEquals(pdf, response.getBody());
    }
}
