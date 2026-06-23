package com.lunaris.ansenuza.application.conversation.steps;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** ASK_LOCALITY: el pasajero elige su localidad de origen y recibe la cotización base. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AskLocalityHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final LocalityRepository localityRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_LOCALITY";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("0".equals(body)) {
            session.setCurrentStep("MAIN_MENU");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    " En qué te podemos ayudar hoy? Por favor, elegí una opción enviando el número:\n\n1️⃣ Reservar un viaje\n2️⃣ Ver Precios\n3️⃣ Operador");
            return;
        }
        try {
            int option = Integer.parseInt(body);
            List<Locality> localities = localityRepository.findLocalitiesWithFares();

            if (option < 1 || option > localities.size()) {
                messaging.sendText(phoneNumber,
                        "❌ Selección inválida. Ingresá un número de la lista o *0* para volver.");
                return;
            }

            Locality selected = localities.get(option - 1);
            BigDecimal baseFare;

            try {
                baseFare = pricingAndScheduleService.calculateTripPrice(selected.getName(), true, 1);
            } catch (IllegalArgumentException ex) {
                log.warn("Falta tarifa en base para la localidad seleccionada: {}",
                        selected.getName());
                messaging.sendText(phoneNumber,
                        "⚠️ Lo sentimos, actualmente *no hay tarifa para esa ciudad* de forma automatizada.\n\nPor favor, ingresá *0* para volver o respondé *Hola* para coordinar con un operador.");
                session.setCurrentStep("START");
                conversationSessionRepository.saveAndFlush(session);
                return;
            }

            session.setPickupLocality(selected.getName());
            session.setCurrentStep("ASK_MARKETING_CONFIRMATION");
            conversationSessionRepository.saveAndFlush(session);

            String primerHorario = pricingAndScheduleService
                    .calculateEstimatedPickupTime(selected.getName(), "03:00");
            String segundoHorario = pricingAndScheduleService
                    .calculateEstimatedPickupTime(selected.getName(), "08:00");

            int lugaresDisponibles = new java.util.Random().nextInt(4) + 1;

            String text = """
                    💰 *Tarifa base para %s:*
                    El valor de referencia (Ida y Vuelta) es de *$%,.0f*.

                    ⏱️ *Horarios de paso por tu localidad:*
                    • Primer horario: *%s*
                    • Segundo horario: *%s*

                    🚨 *¡ATENCIÓN!:* Para viajar en las próximas unidades solo nos quedan *%d lugares disponibles* en la flota compartida.

                    ¿Deseás realizar tu reserva ahora mismo?
                    """
                    .formatted(selected.getName(), baseFare, primerHorario, segundoHorario,
                            lugaresDisponibles);

            messaging.sendButtons(phoneNumber, "LUNARIS - Cotización", text,
                    List.of(new Button("yes_reserve", "Reservar ✅"),
                            new Button("no_cancel", "En otro momento ❌")));
            return;
        } catch (NumberFormatException e) {
            messaging.sendText(phoneNumber,
                    "⚠️ Por favor, respondé únicamente con el número correlativo de tu localidad o *0* para volver.");
            return;
        } catch (Exception e) {
            log.error("Error en ASK_LOCALITY: ", e);
            return;
        }
    }
}
