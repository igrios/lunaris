package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.List;
import java.util.Map;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin/bot")
@AllArgsConstructor
public class BotMonitorController {

    private final ConversationSessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 🖥️ Muestra la lista de conversaciones
    @GetMapping("/monitor")
    public String getMonitor(Model model) {
        List<ConversationSession> sesiones = sessionRepository.findAll();
        model.addAttribute("sesiones", sesiones);
        return "admin/bot-monitor";
    }
// 🛑 Acción para mutear/pausar o despausar el bot (Todo con Long)
@PostMapping("/toggle-bot")
public String toggleBot(@RequestParam("id") Long sessionId) {
    ConversationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada con ID: " + sessionId));
            
    boolean currentState = session.isBotPaused(); 
    session.setBotPaused(!currentState);
    
    sessionRepository.saveAndFlush(session); // 🌟 Usá saveAndFlush para impactar de inmediato en la DB
    
    // 🚨 LA SOLUCIÓN EN TIEMPO REAL: Le avisa al WebSocket del monitor que se actualice solo
    messagingTemplate.convertAndSend("/topic/system-alerts", Map.of(
        "action", "TOGGLE_BOT",
        "sessionId", sessionId,
        "isPaused", !currentState
    ));
    
    return "redirect:/admin/bot/monitor";
}
}