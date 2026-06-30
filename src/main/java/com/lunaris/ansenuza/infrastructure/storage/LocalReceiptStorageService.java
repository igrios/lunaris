package com.lunaris.ansenuza.infrastructure.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import lombok.extern.slf4j.Slf4j;

// 🛠️ DESACTIVADO: Comentamos @Service para que Spring Boot use únicamente el adaptador de Cloudinary
// @Service
@Slf4j
public class LocalReceiptStorageService implements ReceiptStoragePort {

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    @Value("${storage.local-dir}")
    private String localDir;

    @Override
    public String downloadAndSaveReceipt(String mediaId) {
        try {
            // Asegurar que el directorio exista físicamente
            File directory = new File(localDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Paso A: Consultar metadatos a Meta para obtener la URL de descarga efímera
            String metaUrl = "https://graph.facebook.com/v20.0/" + mediaId;
            ResponseEntity<JsonNode> mediaResponse = restTemplate.exchange(
                    metaUrl, HttpMethod.GET, entity, JsonNode.class);
            
            String actualDownloadUrl = mediaResponse.getBody().get("url").asText();

            // Paso B: Descargar los bytes reales del archivo de los servidores de Meta
            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(
                    actualDownloadUrl, HttpMethod.GET, entity, byte[].class);

            byte[] imageBytes = imageResponse.getBody();

            // Paso C: Guardar el archivo en el disco local
            String fileName = "comprobante_" + mediaId + ".jpg";
            // Ajustado para combinar las rutas de forma segura sin importar los separadores / o \
            Path destinationPath = Paths.get(localDir).resolve(fileName);
            Files.write(destinationPath, imageBytes);

            log.info("Comprobante guardado localmente en: {}", destinationPath.toAbsolutePath());

            // Devolvemos la ruta web relativa con la que Martín va a acceder desde el navegador
            return "/comprobantes/" + fileName;

        } catch (Exception e) {
            log.error("Error al descargar e impactar el archivo local de WhatsApp con ID: " + mediaId, e);
            return null;
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "null";
        }
        try {
            // Reemplazo del operador Elvis (?:) no soportado en Java por un operador ternario estándar
            String baseDir = (this.localDir != null) ? this.localDir : "/tmp/comprobantes/";
            
            String uniqueFileName = "comprobante_manual_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(baseDir).resolve(uniqueFileName);
            
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, file.getBytes());
            
            return targetPath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("❌ Falló el almacenamiento local desde el formulario web", e);
            return "null";
        }
    }
}