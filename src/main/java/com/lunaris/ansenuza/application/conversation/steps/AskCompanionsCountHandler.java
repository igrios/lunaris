package com.lunaris.ansenuza.application.conversation.steps;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.PassengerAddressResolver;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_COMPANIONS_COUNT: cantidad de acompañantes (0 a 3); ramifica a carga individual o dirección. */
@Component
@RequiredArgsConstructor
public class AskCompanionsCountHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerAddressResolver passengerAddressResolver;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_COMPANIONS_COUNT";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        try {
            int count = Integer.parseInt(body);
            if (count < 0 || count > 3) {
                messaging.sendText(phoneNumber,
                        "❌ Podés registrar hasta un máximo de 3 acompañantes directos. Ingresá entre 0 y 3:");
                return;
            }

            if (count == 0) {
                session.setPassengerCount(1);
                session.setCompanionNames(null);
                passengerAddressResolver.resolve(phoneNumber, session);
            } else {
                session.setTotalCompanions(count);
                session.setPassengerCount(1);
                session.setCurrentStep("ASK_INDIVIDUAL_COMPANION");
                session.setCompanionNames("");
                
                // 🛠️ CORRECCIÓN: Usamos el campo semántico correcto en lugar de setCuil("1")
                session.setCurrentCompanionIndex(1);
                
                conversationSessionRepository.saveAndFlush(session);
                messaging.sendText(phoneNumber,
                        "👤 *Ingresá Nombre y Apellido de tu acompañante 1:*");
            }
        } catch (Exception e) {
            messaging.sendText(phoneNumber,
                    "⚠️ Respondé únicamente con el número digital (Ej: 2).");
        }
    }
}