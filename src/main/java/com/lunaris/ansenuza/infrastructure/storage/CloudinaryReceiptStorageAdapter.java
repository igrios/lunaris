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

    // 🔐 Constructor limpio: Inyecta directamente las propiedades estructuradas de tu application.yml
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

    @Override
    public String downloadAndSaveReceipt(String mediaId) {
        if (mediaId == null || mediaId.isBlank()) {
            return null;
        }

        try {
            // 🌐 PASO 1: Le pegamos a Meta con tu Token seguro para obtener la URL del archivo
            String metaUrl = "https://graph.facebook.com/v20.0/" + mediaId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> mediaResponse = restTemplate.exchange(
                    metaUrl, HttpMethod.GET, entity, JsonNode.class);
            
            if (mediaResponse.getBody() == null || !mediaResponse.getBody().has("url")) {
                return null;
            }
            
            String whatsappDownloadUrl = mediaResponse.getBody().get("url").asText();

            // 📥 PASO 2: Descargamos el flujo de bytes de la foto desde los servidores de Meta
            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(
                    whatsappDownloadUrl, HttpMethod.GET, entity, byte[].class);

            byte[] imageBytes = imageResponse.getBody();
            if (imageBytes == null) {
                return null;
            }

            // ☁️ PASO 3: Subimos los bytes a tu cuenta de Cloudinary organizada en la carpeta 'comprobantes'
            String uniqueFileName = "comprobante_" + UUID.randomUUID();
            Map uploadParams = ObjectUtils.asMap(
                "folder", "comprobantes",
                "public_id", uniqueFileName,
                "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(imageBytes, uploadParams);

            // 🔗 PASO 4: Devolvemos la URL segura (https://res.cloudinary.com/...) lista para guardar en Postgres
            return (String) uploadResult.get("secure_url");

        } catch (Exception e) {
            // Registramos el error en la consola de Render para auditoría, pero respetamos el contrato retornando null
            org.slf4j.LoggerFactory.getLogger(getClass())
                .error("❌ Falló el proceso de almacenamiento en Cloudinary para el mediaId: " + mediaId, e);
            return null;
        }
    }
}