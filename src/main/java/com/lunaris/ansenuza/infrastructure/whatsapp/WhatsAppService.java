package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WhatsAppService {

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
}