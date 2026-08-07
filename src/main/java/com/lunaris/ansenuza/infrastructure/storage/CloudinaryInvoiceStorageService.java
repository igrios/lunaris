package com.lunaris.ansenuza.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** Almacenamiento principal de facturas, con compatibilidad local si Cloudinary no está configurado. */
@Service
@Primary
@Slf4j
public class CloudinaryInvoiceStorageService implements InvoiceStoragePort {

    private final Cloudinary cloudinary;
    private final LocalInvoiceStorageService localStorage;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryInvoiceStorageService(
            Cloudinary cloudinary,
            @Qualifier("localInvoiceStorageService") LocalInvoiceStorageService localStorage,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudinary = cloudinary;
        this.localStorage = localStorage;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public StoredInvoice store(byte[] content, String desiredFileName) {
        if (isConfigured()) {
            try {
                String publicId = withoutExtension(desiredFileName);
                Map<?, ?> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                        "resource_type", "raw",
                        "folder", "facturas",
                        "public_id", publicId,
                        "overwrite", true));
                Object secureUrl = result.get("secure_url");
                if (secureUrl != null && !secureUrl.toString().isBlank()) {
                    String url = normalizePdfUrl(secureUrl.toString());
                    log.info("Factura {} guardada en Cloudinary.", desiredFileName);
                    return new StoredInvoice(url, url);
                }
                log.warn("Cloudinary no devolvió secure_url para {}. Se usa almacenamiento local.", desiredFileName);
            } catch (Exception exception) {
                log.warn("No se pudo subir la factura {} a Cloudinary; se usa fallback local.",
                        desiredFileName, exception);
            }
        }
        return localStorage.store(content, desiredFileName);
    }

    @Override
    public String resolveAbsolutePath(String pdfUrl) {
        if (pdfUrl != null && pdfUrl.startsWith("https://")) {
            return pdfUrl;
        }
        return localStorage.resolveAbsolutePath(pdfUrl);
    }

    private boolean isConfigured() {
        return hasValue(cloudName) && hasValue(apiKey) && hasValue(cloudSecret());
    }

    private String cloudSecret() {
        return apiSecret;
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private static String withoutExtension(String fileName) {
        int extension = fileName == null ? -1 : fileName.lastIndexOf('.');
        return extension > 0 ? fileName.substring(0, extension) : fileName;
    }

    private static String normalizePdfUrl(String url) {
        return url.replace("/image/upload/", "/raw/upload/");
    }
}
