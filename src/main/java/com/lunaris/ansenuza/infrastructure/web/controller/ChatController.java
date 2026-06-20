package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatMessageRepository messageRepository;
    private final ConversationSessionRepository sessionRepository;

    @GetMapping("/{phoneNumber}")
    public String openChat(@PathVariable String phoneNumber, Model model) {
        ConversationSession session = sessionRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("No hay sesión activa para el teléfono: " + phoneNumber));

        List<ChatMessage> historial = messageRepository.findByPhoneNumberOrderByTimestampAsc(phoneNumber);

        model.addAttribute("session", session);
        model.addAttribute("historial", historial);
        return "admin/chat-room";
    }
    
    // ELIMINAMOS O COMENTAMOS EL MÉTODO @PostMapping("/send") DE ACÁ.
    // Ya no lo necesitamos porque ahora Martín envía mensajes por el WebSocket.
}