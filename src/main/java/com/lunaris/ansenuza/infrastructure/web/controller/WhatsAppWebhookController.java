package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/whatsapp")
@AllArgsConstructor
@Slf4j
public class WhatsAppWebhookController {

    private final WhatsAppService whatsAppService;

    @GetMapping("/webhook")
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {

        if ("lunaris123".equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(
            @RequestBody Map<String, Object> payload) {

        try {

            List<Map<String, Object>> entry =
                    (List<Map<String, Object>>) payload.get("entry");

            Map<String, Object> change =
                    (Map<String, Object>)
                            ((List<?>) entry.get(0).get("changes"))
                                    .get(0);

            Map<String, Object> value =
                    (Map<String, Object>) change.get("value");

            List<Map<String, Object>> messages =
                    (List<Map<String, Object>>) value.get("messages");

            if (messages == null || messages.isEmpty()) {
                return ResponseEntity.ok().build();
            }

            Map<String, Object> message =
                    messages.get(0);

            String from =
                    (String) message.get("from");

            Map<String, Object> text =
                    (Map<String, Object>) message.get("text");

            if (text == null) {
                return ResponseEntity.ok().build();
            }

            String body =
                    (String) text.get("body");

            System.out.println("=================================");
            System.out.println("FROM: " + from);
            System.out.println("MESSAGE: " + body);
            System.out.println("=================================");

            String destination =
                    normalizeWhatsAppNumber(from);

            System.out.println("DESTINATION: "
                    + destination);

            processMessage(
                    destination,
                    body);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }

    private void processMessage(
            String phoneNumber,
            String message) {

        if (message == null) {
            return;
        }

        String body =
                message.trim().toLowerCase();

        if ("hola".equals(body)) {

            System.out.println(
                    "RESPONDIENDO MENU PRINCIPAL...");

            whatsAppService.sendMessage(
                    phoneNumber,
                    """
                    🚐 Bienvenido a Lunaris

                    1️⃣ Reservar viaje

                    2️⃣ Consultar reserva

                    3️⃣ Hablar con Martín

                    Responda con el número.
                    """
            );

            return;
        }

        if ("1".equals(body)) {

            whatsAppService.sendMessage(
                    phoneNumber,
                    """
                    📍 ¿Desde dónde viaja?

                    1️⃣ San Guillermo
                    2️⃣ Suardi
                    3️⃣ Villa Trinidad
                    4️⃣ Arrufó
                    5️⃣ Morteros

                    Responda con el número.
                    """
            );

            return;
        }
    }

    private String normalizeWhatsAppNumber(
            String phone) {

        if (phone != null
                && phone.startsWith("549")) {

            return "54"
                    + phone.substring(3);
        }

        return phone;
    }
}