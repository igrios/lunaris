package com.lunaris.ansenuza.infrastructure.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador local que guarda los PDF de facturas en disco y los expone bajo /facturas/**.
 */
@Service
@Slf4j
public class LocalInvoiceStorageService implements InvoiceStoragePort {

    @Value("${storage.invoices-dir}")
    private String invoicesDir;

    @Override
    public StoredInvoice store(byte[] content, String desiredFileName) {
        try {
            File directory = new File(invoicesDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            Path destination = Paths.get(invoicesDir, desiredFileName);
            Files.write(destination, content);
            log.info("Factura guardada localmente en: {}", destination.toAbsolutePath());
            return new StoredInvoice("/facturas/" + desiredFileName, destination.toAbsolutePath().toString());
        } catch (Exception e) {
            log.error("Error al guardar el PDF de la factura: {}", desiredFileName, e);
            throw new RuntimeException("No se pudo guardar el PDF de la factura", e);
        }
    }

    @Override
    public String resolveAbsolutePath(String pdfUrl) {
        String fileName = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1);
        return Paths.get(invoicesDir, fileName).toAbsolutePath().toString();
    }
}
