package com.lunaris.ansenuza.application.conversation.steps;

import java.util.List;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.lunaris.ansenuza.application.conversation.BotRoute;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;

@Component
@RequiredArgsConstructor
public class AskTownDestinationHandler implements ConversationStepHandler {
    private final LocalityRepository localities;
    private final ConversationSessionRepository sessions;
    private final PricingAndScheduleService pricing;
    private final MessagingPort messaging;

    @Override
    public String step() { return "ASK_TOWN_DESTINATION"; }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        List<Locality> destinations = BotRoute.destinations(localities);
        int index;
        try {
            index = Integer.parseInt(message.body() == null ? "" : message.body().trim()) - 1;
        } catch (NumberFormatException exception) {
            index = -1;
        }
        if (index < 0 || index >= destinations.size()) {
            messaging.sendText(session.getPhoneNumber(), "Ingresá un número válido de la lista de destinos.");
            return;
        }
        session.setDestination(destinations.get(index).getName());
        session.setCurrentStep("ASK_MARKETING_CONFIRMATION");
        sessions.saveAndFlush(session);
        var fare = pricing.calculateTripPrice(session.getDestination(), true, 1);
        messaging.sendButtons(session.getPhoneNumber(), "LUNARIS - Cotización",
                "💰 Tarifa de referencia (ida y vuelta) a " + session.getDestination()
                        + ": $" + fare + ". ¿Deseás reservar?",
                List.of(new Button("yes_reserve", "Reservar ✅"), new Button("no_cancel", "En otro momento ❌")));
    }
}
