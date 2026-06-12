package com.lunaris.ansenuza.infrastructure.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @GetMapping("/whatsapp/test")
    public String test() {

        whatsAppService.sendMessage(
                "543512282251",
                "Hola desde Lunaris 🚐");

        return "Mensaje enviado";
    }
}