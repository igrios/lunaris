package com.lunaris.ansenuza.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

/** Almacenamiento principal de facturas, con compatibilidad local si Cloudinary no está configurado. */
@Service
@Primary
@Slf4j
public class CloudinaryInvoiceStorageService implements InvoiceStoragePort {

    private static final String PDF_DOWNLOAD_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int PDF_CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final int PDF_READ_TIMEOUT_MILLIS = 10_000;

    private final Cloudinary cloudinary;
    private final LocalInvoiceStorageService localStorage;
    private final Environment environment;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    @Autowired
    public CloudinaryInvoiceStorageService(
            Cloudinary cloudinary,
            @Qualifier("localInvoiceStorageService") ObjectProvider<LocalInvoiceStorageService> localStorage,
            Environment environment,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudinary = cloudinary;
        this.localStorage = localStorage.getIfAvailable();
        this.environment = environment;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    CloudinaryInvoiceStorageService(Cloudinary cloudinary, LocalInvoiceStorageService localStorage,
            String cloudName, String apiKey, String apiSecret) {
        this(cloudinary, localStorage, new org.springframework.core.env.StandardEnvironment(),
                cloudName, apiKey, apiSecret);
    }

    CloudinaryInvoiceStorageService(Cloudinary cloudinary, LocalInvoiceStorageService localStorage,
            Environment environment, String cloudName, String apiKey, String apiSecret) {
        this.cloudinary = cloudinary;
        this.localStorage = localStorage;
        this.environment = environment;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @PostConstruct
    void validateProductionConfiguration() {
        if (isProduction() && !isConfigured()) {
            throw new IllegalStateException(
                    "Cloudinary debe estar configurado para almacenar facturas en producción.");
        }
    }

    @Override
    public StoredInvoice store(byte[] content, String desiredFileName) {
        if (isConfigured()) {
            try {
                String publicId = ensurePdfExtension(desiredFileName);
                Map<?, ?> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                        "resource_type", "raw",
                        "access_mode", "public",
                        "type", "upload",
                        "folder", "facturas",
                        "public_id", publicId,
                        "overwrite", true));
                Object secureUrl = result.get("secure_url");
                if (secureUrl != null && !secureUrl.toString().isBlank()) {
                    String url = normalizePdfUrl(secureUrl.toString());
                    log.info("Factura {} guardada en Cloudinary.", desiredFileName);
                    return new StoredInvoice(url, url);
                }
                throw new IllegalStateException("Cloudinary no devolvió una URL persistente para la factura.");
            } catch (Exception exception) {
                throw new IllegalStateException(
                        "No se pudo persistir la factura en Cloudinary.", exception);
            }
        }
        if (isProduction() || localStorage == null) {
            throw new IllegalStateException(
                    "Cloudinary es obligatorio para almacenar facturas en este entorno.");
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
            try {
                try {
                    return download(pdfUrl);
                } catch (CloudinaryDownloadException exception) {
                    if (!exception.isAuthenticationFailure() || !isConfigured()) {
                        throw exception;
                    }
                    log.info("Cloudinary rechazó la URL pública de la factura; se reintenta con descarga firmada.");
                    return download(createSignedDownloadUrl(pdfUrl));
                }
            } catch (IOException | IllegalArgumentException exception) {
                throw new IllegalStateException("No se pudo descargar el PDF desde Cloudinary.", exception);
            }
        }
        return localStorage.load(pdfUrl);
    }

    private byte[] download(String url) throws IOException {
        HttpURLConnection connection = openConnection(url);
        configurePdfConnection(connection);
        try {
            int status = connection.getResponseCode();
            if (status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new CloudinaryDownloadException(status);
            }
            try (java.io.InputStream input = connection.getInputStream()) {
                return input.readAllBytes();
            }
        } finally {
            connection.disconnect();
        }
    }

    HttpURLConnection openConnection(String url) throws IOException {
        return (HttpURLConnection) URI.create(url).toURL().openConnection();
    }

    static void configurePdfConnection(HttpURLConnection connection) {
        connection.setRequestProperty("User-Agent", PDF_DOWNLOAD_USER_AGENT);
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(PDF_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(PDF_READ_TIMEOUT_MILLIS);
    }

    private String createSignedDownloadUrl(String pdfUrl) {
        String[] segments = URI.create(pdfUrl).getPath().split("/");
        int rawIndex = Arrays.asList(segments).indexOf("raw");
        if (rawIndex < 0 || rawIndex + 2 >= segments.length) {
            throw new IllegalArgumentException("La URL no corresponde a un recurso raw de Cloudinary.");
        }
        String deliveryType = segments[rawIndex + 1];
        int publicIdStart = rawIndex + 2;
        if (segments[publicIdStart].startsWith("s--")) {
            publicIdStart++;
        }
        if (publicIdStart < segments.length && segments[publicIdStart].matches("v\\d+")) {
            publicIdStart++;
        }
        if (publicIdStart >= segments.length) {
            throw new IllegalArgumentException("La URL de Cloudinary no contiene un public_id.");
        }
        String publicId = String.join("/", Arrays.copyOfRange(segments, publicIdStart, segments.length));
        try {
            return cloudinary.privateDownload(publicId, null, Map.<String, Object>of(
                    "resource_type", "raw",
                    "type", deliveryType));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo firmar la descarga de la factura.", exception);
        }
    }

    private static final class CloudinaryDownloadException extends IOException {
        private final int status;

        private CloudinaryDownloadException(int status) {
            super("Falló la descarga de Cloudinary, HTTP code: " + status);
            this.status = status;
        }

        private boolean isAuthenticationFailure() {
            return status == HttpURLConnection.HTTP_UNAUTHORIZED
                    || status == HttpURLConnection.HTTP_FORBIDDEN;
        }
    }

    private boolean isConfigured() {
        return hasValue(cloudName) && hasValue(apiKey) && hasValue(cloudSecret());
    }

    private boolean isProduction() {
        return environment.acceptsProfiles(Profiles.of("prod", "production"));
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
