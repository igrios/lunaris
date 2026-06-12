package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsAppService {

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate =
            new RestTemplate();

    public void sendMessage(
            String phoneNumber,
            String message) {

        String url =
                "https://graph.facebook.com/v25.0/"
                        + phoneNumberId
                        + "/messages";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setBearerAuth(accessToken);

        headers.setContentType(
                MediaType.APPLICATION_JSON);

        Map<String, Object> body =
                Map.of(
                        "messaging_product", "whatsapp",
                        "to", phoneNumber,
                        "type", "text",
                        "text", Map.of(
                                "body", message
                        )
                );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {

            System.out.println("=================================");
            System.out.println("ENVIANDO A: " + phoneNumber);
            System.out.println("MENSAJE:");
            System.out.println(message);
            System.out.println("=================================");

            System.out.println("URL: " + url);
System.out.println("BODY: " + body);
            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            String.class);

            System.out.println("STATUS: "
                    + response.getStatusCode());

            System.out.println("BODY: "
                    + response.getBody());

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}