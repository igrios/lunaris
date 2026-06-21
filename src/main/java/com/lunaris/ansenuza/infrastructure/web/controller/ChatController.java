package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation; // 🚐 Importación de tu modelo de Reserva
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository; // 📍 Tu repositorio de localidades
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatMessageRepository messageRepository;
    private final ConversationSessionRepository sessionRepository;
    private final LocalityRepository localityRepository; // 👈 Agregado para soportar el formulario de la derecha

    @GetMapping("/{phoneNumber}")
    public String openChat(@PathVariable String phoneNumber, Model model) {
        ConversationSession session = sessionRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("No hay sesión activa para el teléfono: " + phoneNumber));

        List<ChatMessage> historial = messageRepository.findByPhoneNumberOrderByTimestampAsc(phoneNumber);

        // 1. Datos del chat originales (Intactos para tu WebSocket actual)
        model.addAttribute("session", session);
        model.addAttribute("historial", historial);
        model.addAttribute("phone", phoneNumber);

        // 2. Datos dinámicos para habilitar la Nueva Reserva Asistida en espejo
        model.addAttribute("localities", localityRepository.findAll()); 
        model.addAttribute("reservation", new Reservation()); 

        return "admin/chat-room";
    }
}