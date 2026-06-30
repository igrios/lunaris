package com.lunaris.ansenuza.infrastructure.storage;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;

@Service
public class CloudinaryReceiptStorageAdapter implements ReceiptStoragePort {

    private final Cloudinary cloudinary;
    private final RestTemplate restTemplate;

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    // 🔐 Constructor limpio mapeando properties
    public CloudinaryReceiptStorageAdapter(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        
        this.restTemplate = new RestTemplate();
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    // 🔥 IMPLEMENTACIÓN NUEVA: Sube el archivo físico del navegador web a Cloudinary
    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "null";
        }
        try {
            String uniqueFileName = "comprobante_manual_" + UUID.randomUUID();
            Map uploadParams = ObjectUtils.asMap(
                "folder", "comprobantes",
                "public_id", uniqueFileName,
                "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                .error("❌ Falló el almacenamiento en Cloudinary desde el formulario web", e);
            return "null";
        }
    }

    // 📥 MÉTODO ORIGINAL ADAPTADO: Descarga multimedia desde la API de Meta
    @Override
    public String downloadAndSaveReceipt(String mediaId) {
        try {
            String urlMetadata = "https://graph.facebook.com/v17.0/" + mediaId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> mediaResponse = restTemplate.exchange(
                    urlMetadata, HttpMethod.GET, entity, JsonNode.class);

            if (mediaResponse.getBody() == null || !mediaResponse.getBody().has("url")) {
                return null;
            }
            
            String whatsappDownloadUrl = mediaResponse.getBody().get("url").asText();

            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(
                    whatsappDownloadUrl, HttpMethod.GET, entity, byte[].class);

            byte[] imageBytes = imageResponse.getBody();
            if (imageBytes == null) {
                return null;
            }

            String uniqueFileName = "comprobante_" + UUID.randomUUID();
            Map uploadParams = ObjectUtils.asMap(
                "folder", "comprobantes",
                "public_id", uniqueFileName,
                "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(imageBytes, uploadParams);
            return (String) uploadResult.get("secure_url");

        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                .error("❌ Falló el proceso de almacenamiento en Cloudinary para el mediaId: " + mediaId, e);
            return null;
        }
    }
}