package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.usecase.IssueInvoiceUseCase;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class PublicInvoiceControllerTest {

    @Test
    void servesPdfBytesDirectlyWithoutRedirect() {
        IssueInvoiceUseCase invoices = mock(IssueInvoiceUseCase.class);
        UUID id = UUID.randomUUID();
        byte[] pdf = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);
        when(invoices.download(id)).thenReturn(
                new IssueInvoiceUseCase.InvoiceDocument("F-2026-00003", pdf));

        var response = new PublicInvoiceController(invoices).download(id);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("inline; filename=\"factura-F-2026-00003.pdf\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertArrayEquals(pdf, response.getBody());
    }
}
