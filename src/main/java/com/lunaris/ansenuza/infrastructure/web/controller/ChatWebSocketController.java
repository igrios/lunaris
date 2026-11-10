package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.Map; // 👈 Agregado para leer el ID
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload; // 👈 Agregado para interceptar el
                                                                 // JSON
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate; // 👈 Agregado para enviar la
                                                                 // alerta global
import org.springframework.stereotype.Controller;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.application.usecase.SendOperatorMessageUseCase;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository; // 👈 Agregado para el
                                                                             // monitor
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@AllArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SendOperatorMessageUseCase sendOperatorMessage;
    private final ConversationSessionRepository conversationSessionRepository; // 👈 Inyectado para
                                                                               // la Torre de
                                                                               // Control
    private final SimpMessagingTemplate messagingTemplate; // 👈 Inyectado para refrescar el monitor
                                                           // en vivo

    @MessageMapping("/chat.send/{phoneNumber}")
    @SendTo("/topic/messages/{phoneNumber}")
    public ChatMessage sendMessage(@DestinationVariable String phoneNumber, ChatMessage message) {

        return sendOperatorMessage.execute(phoneNumber, message == null ? null : message.getMessageText());
    }

    // 🎯 EL CONTROLADOR CORREGIDO PASANDO EL ID A LONG:
    @MessageMapping("/bot.toggle")
    public void handleBotToggle(@Payload Map<String, String> payload) {
        String rawId = payload.get("id");

        try {
            // 🪙 Convertimos el String de JavaScript al Long que exige tu repositorio
            Long sessionId = Long.parseLong(rawId);

            conversationSessionRepository.findById(sessionId).ifPresent(session -> {
                session.setBotPaused(!session.isBotPaused());
                session.setManuallyPaused(session.isBotPaused());
                conversationSessionRepository.saveAndFlush(session);

                // 📢 Alerta global a la pantalla del monitor
                if (this.messagingTemplate != null) {
                    this.messagingTemplate.convertAndSend("/topic/system-alerts", "REFRESH");
                    log.info("[WebSocket] Alerta enviada a la pantalla para el ID sesión: {}",
                            sessionId);
                }
            });
        } catch (NumberFormatException e) {
            log.error("[Torre de Control] El ID recibido ('{}') no se pudo convertir a Long: ",
                    rawId, e);
        } catch (Exception e) {
            log.error("[Torre de Control] Error inesperado en el toggle del bot: ", e);
        }
    }
}
