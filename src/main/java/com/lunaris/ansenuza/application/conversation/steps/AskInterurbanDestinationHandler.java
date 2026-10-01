package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.*;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.service.interurban.InterurbanCatalog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true")
public class AskInterurbanDestinationHandler implements ConversationStepHandler {
    private final InterurbanCatalog catalog;
    private final ConversationSessionRepository sessions;
    private final MessagingPort messaging;
    private final LocalityRepository localities;
    private final AskLocalityHandler conventional;

    @Override
    public String step() { return "ASK_INTERURBAN_DESTINATION"; }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String input = message.body().trim();
        if ("0".equals(input)) {
            session.setCurrentStep("MAIN_MENU");
            sessions.saveAndFlush(session);
            messaging.sendText(session.getPhoneNumber(), "1) Reservar un viaje\n2) Ver precios\n3) Operador");
            return;
        }
        if ("c".equalsIgnoreCase(input)) {
            var origins = localities.findAllWithActiveFare().stream()
                    .filter(l -> !BotRoute.fromCordoba(l.getName())).toList();
            for (int i = 0; i < origins.size(); i++) {
                if (origins.get(i).getName().equalsIgnoreCase(session.getPickupLocality())) {
                    conventional.handleConventional(session, new IncomingMessage(message.from(),
                            IncomingMessage.MessageType.TEXT, Integer.toString(i + 1), null)
                            .withTelemetry(message.telemetry()));
                    return;
                }
            }
            messaging.sendText(session.getPhoneNumber(), "No hay tarifa convencional para este origen. Elegí un destino interurbano o 0 para volver.");
            return;
        }
        var selected = catalog.destinations(session.getPickupLocality()).stream()
                .filter(d -> ("i_" + d.stop()).equalsIgnoreCase(input)).findFirst();
        if (selected.isEmpty()) {
            messaging.sendText(session.getPhoneNumber(), "Destino inválido. Respondé con el código del destino o 0 para volver.");
            return;
        }
        var destination = selected.get();
        session.setDestination(destination.name());
        // No reutilizar ASK_DATE/CONFIRM: persisten reservas y capacidad del esquema public.
        session.setCurrentStep("ASK_INTERURBAN_CONFIRMATION");
        sessions.saveAndFlush(session);
        messaging.sendButtons(session.getPhoneNumber(), "Viaje interurbano",
                "💰 *%s → %s*: $%,.2f ARS por pasajero, solo ida. Tarifa vigente de referencia.\n¿Querés coordinar la reserva con un operador? Confirmará horario, disponibilidad y tarifa para tu fecha."
                        .formatted(session.getPickupLocality(), destination.name(), destination.fare()),
                java.util.List.of(new com.lunaris.ansenuza.application.port.Button("interurban_reserve", "Coordinar reserva"),
                        new com.lunaris.ansenuza.application.port.Button("interurban_cancel", "Volver al menú")));
    }
}
