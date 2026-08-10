package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.model.Reservation; // 🚐 Importación de tu modelo de Reserva
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.application.usecase.LocalityService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.model.service.WhatsAppConversationWindowService;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatMessageRepository messageRepository;
    private final ConversationSessionRepository sessionRepository;
    private final LocalityService localityService;
    private final PassengerRepository passengerRepository;
    private final WhatsAppConversationWindowService conversationWindowService;
    private final WhatsAppService whatsAppService;

    @GetMapping("/{phoneNumber}")
    public String openChat(@PathVariable String phoneNumber, Model model) {
        List<ChatMessage> historial = messageRepository.findByPhoneNumberOrderByTimestampAsc(phoneNumber);

        // 1. Datos del chat originales (Intactos para tu WebSocket actual)
        sessionRepository.findByPhoneNumber(phoneNumber)
                .ifPresent(session -> model.addAttribute("session", session));
        model.addAttribute("historial", historial);
        model.addAttribute("phone", phoneNumber);
        model.addAttribute("chatWindowActive", conversationWindowService.isActive(phoneNumber));
        model.addAttribute("chatWindowExpiresAt",
                conversationWindowService.expirationFor(phoneNumber).orElse(null));

        // 2. Datos dinámicos para habilitar la Nueva Reserva Asistida en espejo
        model.addAttribute("localities", localityService.findAllWithActiveFare());
        model.addAttribute("reservation", new Reservation()); 

        return "admin/chat-room";
    }

    @PostMapping("/{phoneNumber}/contactar")
    public String reopenConversation(@PathVariable String phoneNumber,
            RedirectAttributes redirectAttributes) {
        String passengerName = passengerRepository.findByPhone(phoneNumber)
                .map(passenger -> passenger.getFirstName())
                .filter(name -> !name.isBlank())
                .orElse("Pasajero");
        whatsAppService.sendContactoPasajeroTemplate(phoneNumber, passengerName);
        redirectAttributes.addFlashAttribute("successMessage",
                "Plantilla contacto_pasajero enviada. El chat se habilitará cuando el pasajero responda.");
        return "redirect:/admin/chat/" + phoneNumber;
    }
}
