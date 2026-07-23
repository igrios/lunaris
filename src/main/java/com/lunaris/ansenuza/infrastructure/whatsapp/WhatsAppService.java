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

    private static final Map<String, String> TEMPLATE_LANGUAGES = Map.of(
            "despierta_chofer", "en",
            "proximo_en_camino", "en",
            "chofer_asignado", "es",
            "contacto_pasajero", "es");

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

    public void sendLocationRequest(String phoneNumber, String message) {
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", phoneNumber,
                "type", "interactive",
                "interactive", Map.of(
                        "type", "location_request_message",
                        "body", Map.of("text", message),
                        "action", Map.of("name", "send_location")));
        executePostCall("https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages",
                createHeaders(), body, "SOLICITUD DE UBICACIÓN");
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
            if (isTemplateUnavailable(
                    tipoMensaje, e.getStatusCode().value(), e.getResponseBodyAsString())) {
                log.warn(
                        "Plantilla de Meta no disponible o en revisión [{}] para {}. "
                                + "La operación principal continúa. Respuesta: {}",
                        tipoMensaje, destination, e.getResponseBodyAsString());
                return;
            }
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
        log.error("[CRÍTICO] Error al enviar the comprobante por WhatsApp API al número {}: ", to, e);
    }
}

public void sendDespiertaChoferTemplate(String to, String nombreChofer) {
    try {
        String url = "https://graph.facebook.com/v25.0/" + this.phoneNumberId + "/messages";
        org.springframework.http.HttpHeaders headers = createHeaders();

        // Validamos que si llega nulo o vacío, use un valor por defecto para que Meta no rebote
        String nombreValido = (nombreChofer != null && !nombreChofer.isBlank()) ? nombreChofer : "Chofer";

        java.util.Map<String, Object> bodyParam = java.util.Map.of(
            "type", "text",
            "parameter_name", "nombre_chofer", // <--- ¡CLAVE OBLIGATORIA DE META PARA NAMED VARIABLES!
            "text", nombreValido
        );

        java.util.Map<String, Object> bodyComponent = java.util.Map.of(
            "type", "body",
            "parameters", java.util.List.of(bodyParam)
        );

        java.util.Map<String, Object> templateMap = java.util.Map.of(
            "name", "despierta_chofer",
            "language", java.util.Map.of("code", templateLanguageFor("despierta_chofer")),
            "components", java.util.List.of(bodyComponent)
        );

        java.util.Map<String, Object> body = java.util.Map.of(
            "messaging_product", "whatsapp",
            "recipient_type", "individual",
            "to", to,
            "type", "template",
            "template", templateMap
        );

        executePostCall(url, headers, body, "TEMPLATE DESPIERTA CHOFER");
    } catch (Exception e) {
        log.error("Error al enviar la plantilla despierta_chofer a {}: ", to, e);
    }
}

    public void sendContactoPasajeroTemplate(String to, String passengerName) {
        sendTemplate(to, "contacto_pasajero", List.of(safeTemplateValue(passengerName, "Pasajero")));
    }

    public void sendChoferAsignadoTemplate(String to, String passengerName, String driverName) {
        sendTemplate(to, "chofer_asignado", List.of(
                safeTemplateValue(passengerName, "Pasajero"),
                safeTemplateValue(driverName, "Chofer")));
    }

    public void sendProximoEnCaminoTemplate(
            String to, String passengerName, String driverName, int etaMinutes) {
        sendTemplate(to, "proximo_en_camino", List.of(
                safeTemplateValue(passengerName, "Pasajero"),
                safeTemplateValue(driverName, "Chofer"),
                String.valueOf(Math.max(0, etaMinutes))));
    }

    private void sendTemplate(String to, String templateName, List<String> values) {
        List<Map<String, Object>> parameters = values.stream()
                .map(value -> Map.<String, Object>of("type", "text", "text", value))
                .toList();
        Map<String, Object> template = Map.of(
                "name", templateName,
                "language", Map.of("code", templateLanguageFor(templateName)),
                "components", List.of(Map.of("type", "body", "parameters", parameters)));
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "template",
                "template", template);
        executePostCall("https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages",
                createHeaders(), body, "TEMPLATE " + templateName.toUpperCase());
    }

    private String safeTemplateValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    static String templateLanguageFor(String templateName) {
        return TEMPLATE_LANGUAGES.getOrDefault(templateName, "es");
    }

    static boolean isTemplateUnavailable(String messageType, int httpStatus, String responseBody) {
        if (messageType == null || !messageType.startsWith("TEMPLATE")) {
            return false;
        }
        return httpStatus == 404 || responseBody != null && responseBody.contains("132001");
    }
}
