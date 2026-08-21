package com.lunaris.ansenuza.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort.StoredInvoice;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CloudinaryInvoiceStorageServiceTest {

    @Test
    void uploadsPdfAndReturnsSecureUrl() throws Exception {
        Cloudinary cloudinary = mock(Cloudinary.class);
        Uploader uploader = mock(Uploader.class);
        LocalInvoiceStorageService local = mock(LocalInvoiceStorageService.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/facturas/factura.pdf"));

        CloudinaryInvoiceStorageService service = new CloudinaryInvoiceStorageService(
                cloudinary, local, "demo", "key", "secret");

        StoredInvoice stored = service.store(new byte[] {1, 2}, "factura.pdf");

        assertEquals("https://res.cloudinary.com/demo/raw/upload/facturas/factura.pdf", stored.webUrl());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> params = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), params.capture());
        assertEquals("raw", params.getValue().get("resource_type"));
        assertEquals("factura.pdf", params.getValue().get("public_id"));
    }

    @Test
    void appendsPdfExtensionWhenCloudinaryOmitsItFromSecureUrl() throws Exception {
        Cloudinary cloudinary = mock(Cloudinary.class);
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url",
                        "https://res.cloudinary.com/demo/raw/upload/facturas/factura"));
        CloudinaryInvoiceStorageService service = new CloudinaryInvoiceStorageService(
                cloudinary, mock(LocalInvoiceStorageService.class), "demo", "key", "secret");

        StoredInvoice stored = service.store(new byte[] {1, 2}, "factura.pdf");

        assertEquals("https://res.cloudinary.com/demo/raw/upload/facturas/factura.pdf",
                stored.webUrl());
    }

    @Test
    void fallsBackToLocalWhenCredentialsAreMissing() {
        Cloudinary cloudinary = mock(Cloudinary.class);
        LocalInvoiceStorageService local = mock(LocalInvoiceStorageService.class);
        when(local.store(any(byte[].class), any(String.class)))
                .thenReturn(new StoredInvoice("/facturas/factura.pdf", "/tmp/factura.pdf"));
        CloudinaryInvoiceStorageService service = new CloudinaryInvoiceStorageService(
                cloudinary, local, "", "", "");

        StoredInvoice stored = service.store(new byte[] {1}, "factura.pdf");

        assertEquals("/facturas/factura.pdf", stored.webUrl());
        verify(local).store(any(byte[].class), any(String.class));
    }
}
