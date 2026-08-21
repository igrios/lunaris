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
                String publicId = ensurePdfExtension(desiredFileName);
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

    @Override
    public byte[] load(String pdfUrl) {
        if (pdfUrl != null && pdfUrl.startsWith("https://")) {
            try (java.io.InputStream input = java.net.URI.create(pdfUrl).toURL().openStream()) {
                return input.readAllBytes();
            } catch (java.io.IOException | IllegalArgumentException exception) {
                throw new IllegalStateException("No se pudo descargar el PDF desde Cloudinary.", exception);
            }
        }
        return localStorage.load(pdfUrl);
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

    private static String ensurePdfExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "factura.pdf";
        }
        return fileName.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                ? fileName
                : fileName + ".pdf";
    }

    private static String normalizePdfUrl(String url) {
        String normalized = url.replace("/image/upload/", "/raw/upload/");
        int queryStart = normalized.indexOf('?');
        String path = queryStart >= 0 ? normalized.substring(0, queryStart) : normalized;
        String query = queryStart >= 0 ? normalized.substring(queryStart) : "";
        return path.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                ? normalized
                : path + ".pdf" + query;
    }
}
