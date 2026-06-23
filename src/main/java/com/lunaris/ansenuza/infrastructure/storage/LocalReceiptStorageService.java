package com.lunaris.ansenuza.infrastructure.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import lombok.extern.slf4j.Slf4j;

@Service
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
            Path destinationPath = Paths.get(localDir + fileName);
            Files.write(destinationPath, imageBytes);

            log.info("Comprobante guardado localmente en: {}", destinationPath.toAbsolutePath());

            // Devolvemos la ruta web relativa con la que Martín va a acceder desde el navegador
            return "/comprobantes/" + fileName;

        } catch (Exception e) {
            log.error("Error al descargar e impactar el archivo local de WhatsApp con ID: " + mediaId, e);
            return null;
        }
    }
}