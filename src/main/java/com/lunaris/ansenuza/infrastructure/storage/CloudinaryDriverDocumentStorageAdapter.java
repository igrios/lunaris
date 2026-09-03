package com.lunaris.ansenuza.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lunaris.ansenuza.application.port.DriverDocumentStoragePort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.util.Map;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary
public class CloudinaryDriverDocumentStorageAdapter implements DriverDocumentStoragePort {

    private final Cloudinary cloudinary;
    private final LocalDriverDocumentStorageAdapter localStorage;
    private final Environment environment;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryDriverDocumentStorageAdapter(
            Cloudinary cloudinary,
            ObjectProvider<LocalDriverDocumentStorageAdapter> localStorage,
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

    @PostConstruct
    void validateProductionConfiguration() {
        if (isProduction() && !isConfigured()) {
            throw new IllegalStateException(
                    "Cloudinary debe estar configurado para documentos de choferes en producción.");
        }
    }

    @Override
    public String store(String documentType, MultipartFile file) {
        validate(documentType, file);
        if (isConfigured()) {
            try {
                Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "resource_type", "raw",
                        "access_mode", "public",
                        "folder", "driver-applications",
                        "public_id", documentType + "_" + UUID.randomUUID(),
                        "overwrite", false));
                Object secureUrl = result.get("secure_url");
                if (secureUrl == null || secureUrl.toString().isBlank()) {
                    throw new IllegalStateException("Cloudinary no devolvió una URL persistente.");
                }
                return secureUrl.toString();
            } catch (Exception exception) {
                throw new DomainValidationException(
                        "No se pudo persistir el documento " + documentType + " en Cloudinary.");
            }
        }
        if (isProduction() || localStorage == null) {
            throw new DomainValidationException(
                    "Cloudinary es obligatorio para almacenar documentos de choferes.");
        }
        return localStorage.store(documentType, file);
    }

    private void validate(String documentType, MultipartFile file) {
        if (documentType == null || documentType.isBlank() || file == null || file.isEmpty()) {
            throw new DomainValidationException("El documento solicitado es obligatorio.");
        }
    }

    private boolean isConfigured() {
        return hasValue(cloudName) && hasValue(apiKey) && hasValue(apiSecret);
    }

    private boolean isProduction() {
        return environment.acceptsProfiles(Profiles.of("prod", "production"));
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
