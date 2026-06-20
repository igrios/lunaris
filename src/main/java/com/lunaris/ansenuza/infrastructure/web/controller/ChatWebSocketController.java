package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService; // 👈 Importación exacta de tu servicio
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@AllArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatMessageRepository chatMessageRepository;
    private final WhatsAppService whatsAppService; // 👈 Inyectamos tu servicio real

    @MessageMapping("/chat.send/{phoneNumber}")
    @SendTo("/topic/messages/{phoneNumber}")
    public ChatMessage sendMessage(@DestinationVariable String phoneNumber, ChatMessage message) {
        
        message.setPhoneNumber(phoneNumber);
        message.setTimestamp(LocalDateTime.now());
        message.setFromOperator(true); // Marcamos que el mensaje sale de la web
        
        // 1. Guardamos localmente en la base de datos (lo que genera el Insert de Hibernate)
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // 2. 🚀 EL PUENTE DEFINITIVO: Enviamos el texto real a los servidores de Meta
        try {
            // Usamos el método exacto de tu clase: sendMessage(String, String)
            whatsAppService.sendMessage(phoneNumber, message.getMessageText()); 
            log.info("[WEB CHAT] Mensaje despachado a WhatsApp con éxito hacia el número: {}", phoneNumber);
        } catch (Exception e) {
            log.error("[CRÍTICO] Error al intentar enviar el mensaje de operador a la API de WhatsApp para: {}", phoneNumber, e);
        }
        
        // 3. Se distribuye al WebSocket para pintarse en caliente en tu pantalla
        return savedMessage;
    }
}