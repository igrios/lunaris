package com.lunaris.ansenuza.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lunaris.ansenuza.application.port.NewsBannerStoragePort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryNewsBannerStorageAdapter implements NewsBannerStoragePort {

    private final Cloudinary cloudinary;

    public CloudinaryNewsBannerStorageAdapter(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    @Override
    public String upload(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new DomainValidationException("La imagen del flyer es obligatoria.");
        }
        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new DomainValidationException("El flyer debe ser un archivo de imagen.");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap(
                    "folder", "novedades",
                    "public_id", "flyer_" + UUID.randomUUID(),
                    "resource_type", "image"));
            return (String) result.get("secure_url");
        } catch (Exception exception) {
            throw new DomainValidationException("No se pudo subir el flyer a Cloudinary.");
        }
    }
}
