package com.lunaris.ansenuza.application.conversation.steps;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.service.OperationControlService; // 👈 NUEVO IMPORT
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;

/**
 * START / saludo: muestra el menú principal, notifica saldo corriente y transiciona a MAIN_MENU.
 */
@Component
@RequiredArgsConstructor
public class StartHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerRepository passengerRepository;
    private final MessagingPort messaging;
    private final OperationControlService operationControlService; // 👈 NUEVO SERVICIO INYECTADO

    @Override
    public String step() {
        return "START";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        session.setCurrentStep("MAIN_MENU");
        session.setLastInteraction(com.lunaris.ansenuza.shared.ArgentinaTime.now());
        conversationSessionRepository.saveAndFlush(session);

        Optional<Passenger> existingPassenger = passengerRepository.findByPhone(phoneNumber);

        StringBuilder saludoBuilder = new StringBuilder();
        if (existingPassenger.isPresent()) {
            Passenger passenger = existingPassenger.get();
            saludoBuilder.append("¡Hola de nuevo, *").append(passenger.getFirstName())
                    .append("*! 👋\n");

            // 💰 CUENTA CORRIENTE: Si el pasajero tiene saldo a favor, se lo recordamos al inicio
            if (passenger.getCurrentBalance() != null
                    && passenger.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
                saludoBuilder.append("\n💵 *Tenés un saldo a favor de $")
                        .append(String.format("%,.2f", passenger.getCurrentBalance()))
                        .append("* en tu cuenta. Se aplicará automáticamente como descuento en tu próxima reserva.\n");
            }
        } else {
            saludoBuilder.append("¡Bienvenido a Lunaris Ansenuza! 🚐\n");
        }

        saludoBuilder.append("\n¿En qué te podemos ayudar hoy? Por favor, elegí una opción enviando el número:\n");
        saludoBuilder.append("1️⃣ 🚐 *Reservar un viaje* (Flujo rápido)\n");
        saludoBuilder.append("2️⃣ 💸 *Ver precios y cotizar*\n");

        // 🕒 MUTACIÓN DINÁMICA: Solo muestra la opción 3 si la jornada humana está habilitada
        if (operationControlService.isHumanActionEnabled()) {
            saludoBuilder.append("3️⃣ 👨‍💼 *Hablar con un operador* (Soporte humano)\n");
        }

        saludoBuilder.append("4️⃣ 📋 *Consultar mis reservas*\n");
        saludoBuilder.append("5️⃣ ❌ *Cancelar un viaje*\n");

        messaging.sendText(phoneNumber, saludoBuilder.toString());
    }
}
