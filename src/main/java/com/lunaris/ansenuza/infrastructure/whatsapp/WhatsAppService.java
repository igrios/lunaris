package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j


public class WhatsAppService {

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    @Value("${whatsapp.phone-number-id}")
    private String whatsappPhoneNumberId;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    // MENSAJE TEXTO TRADICIONAL
    public void sendMessage(String phoneNumber, String message) {
        String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
        HttpHeaders headers = createHeaders();
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", phoneNumber,
                "type", "text",
                "text", Map.of("body", message)
        );
        executePostCall(url, headers, body, "TEXTO");
    }

    // SOBRECARGA 1: BOTONES INTERACTIVOS COMUNES (3 ARGUMENTOS)
    public void sendInteractiveButtons(String phoneNumber, String bodyText, List<Map<String, String>> buttons) {
        sendInteractiveButtons(phoneNumber, "Lunaris Ansenuza", bodyText, buttons);
    }

    // SOBRECARGA 2: BOTONES INTERACTIVOS PREMIUM CON TÍTULO DESTACADO (4 ARGUMENTOS)
    public void sendInteractiveButtons(String phoneNumber, String headerText, String bodyText, List<Map<String, String>> buttons) {
        String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
        HttpHeaders headers = createHeaders();

        try {
            List<Map<String, Object>> buttonObjects = new ArrayList<>();
            for (Map<String, String> btn : buttons) {
                buttonObjects.add(Map.of(
                    "type", "reply",
                    "reply", Map.of("id", btn.get("id"), "title", btn.get("title"))
                ));
            }

            Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", phoneNumber,
                "type", "interactive",
                "interactive", Map.of(
                    "type", "button",
                    "header", Map.of("type", "text", "text", headerText),
                    "body", Map.of("text", bodyText),
                    "action", Map.of("buttons", buttonObjects)
                )
            );

            executePostCall(url, headers, body, "BOTONES INTERACTIVOS");
        } catch (Exception e) {
            log.error("Error en botones interactivos: ", e);
        }
    }

    // MENÚ DESPLEGABLE PREMIUM MULTI-SECCIÓN
    public void sendInteractiveList(String phoneNumber, String headerText, String bodyText, String buttonLabel, List<Map<String, Object>> sections) {
        String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
        HttpHeaders headers = createHeaders();

        try {
            Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", phoneNumber,
                "type", "interactive",
                "interactive", Map.of(
                    "type", "list",
                    "header", Map.of("type", "text", "text", headerText),
                    "body", Map.of("text", bodyText),
                    "action", Map.of(
                        "button", buttonLabel,
                        "sections", sections
                    )
                )
            );

            executePostCall(url, headers, body, "LISTA GEOGRÁFICA");
        } catch (Exception e) {
            log.error("Error en lista desplegable: ", e);
        }
    }

    // 🧾 ENVÍO DE DOCUMENTO (PDF) — sube el archivo local a Meta y luego lo manda por su media id
    public void sendDocument(String phoneNumber, String absoluteFilePath, String fileName, String caption) {
        try {
            // Paso 1: Subir el PDF a la Media API (multipart) para obtener un media id
            String uploadUrl = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/media";
            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.setBearerAuth(accessToken);
            uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("messaging_product", "whatsapp");
            parts.add("type", "application/pdf");
            parts.add("file", new FileSystemResource(absoluteFilePath));

            HttpEntity<MultiValueMap<String, Object>> uploadRequest = new HttpEntity<>(parts, uploadHeaders);
            ResponseEntity<JsonNode> uploadResponse =
                    restTemplate.postForEntity(uploadUrl, uploadRequest, JsonNode.class);
            String mediaId = uploadResponse.getBody().get("id").asText();

            // Paso 2: Enviar el documento usando el media id
            String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
            Map<String, Object> documentNode = new HashMap<>();
            documentNode.put("id", mediaId);
            documentNode.put("filename", fileName);
            if (caption != null && !caption.isBlank()) {
                documentNode.put("caption", caption);
            }

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", phoneNumber,
                    "type", "document",
                    "document", documentNode
            );

            executePostCall(url, createHeaders(), body, "DOCUMENTO");
        } catch (Exception e) {
            log.error("Error al enviar documento por WhatsApp a {}: ", phoneNumber, e);
            throw new RuntimeException("No se pudo enviar el documento por WhatsApp", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void executePostCall(String url, HttpHeaders headers, Map<String, Object> body, String tipoMensaje) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String destination = (String) body.get("to");
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("Éxito Meta [{}]: Envío hacia {}. Status: {}", tipoMensaje, destination, response.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("Error de Meta HTTP [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Falla de red en HTTP call Meta: ", e);
        }
    }
// 📦 Agregá este método al final de tu archivo WhatsAppService.java
public void sendMediaMessage(String to, String type, String mediaUrl, String caption) {
    if (mediaUrl == null || "null".equals(mediaUrl)) {
        log.warn("[WhatsApp API] Intento de enviar mensaje multimedia sin URL válida.");
        return;
    }

    try {
        // 🌐 URL usando tu variable exacta: phoneNumberId
        String url = "https://graph.facebook.com/v20.0/" + this.phoneNumberId + "/messages";

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", to);
        body.put("type", "image");

        java.util.Map<String, String> imageNode = new java.util.HashMap<>();
        imageNode.put("link", mediaUrl);
        imageNode.put("caption", caption);
        body.put("image", imageNode);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(this.accessToken); // 👈 Corregido con tu variable: accessToken

        org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(body, headers);
        
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        restTemplate.postForEntity(url, entity, String.class);
        
        log.info("[WhatsApp API] Comprobante manual enviado con éxito al número: {}", to);

    } catch (Exception e) {
        log.error("[CRÍTICO] Error al enviar el comprobante por WhatsApp API al número {}: ", to, e);
    }
}





}