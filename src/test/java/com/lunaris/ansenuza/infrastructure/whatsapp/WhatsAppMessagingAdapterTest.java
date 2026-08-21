package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class WhatsAppMessagingAdapterTest {

    @Test
    void delegatesRemotePdfAsUrlInsteadOfTreatingItAsLocalFile() {
        WhatsAppService service = mock(WhatsAppService.class);
        WhatsAppMessagingAdapter adapter = new WhatsAppMessagingAdapter(service);

        adapter.sendDocumentUrl("543511112222", "https://cdn.example/factura.pdf",
                "Factura-F-1.pdf", "Factura");

        verify(service).sendDocumentUrl("543511112222", "https://cdn.example/factura.pdf",
                "Factura-F-1.pdf", "Factura");
    }
}
