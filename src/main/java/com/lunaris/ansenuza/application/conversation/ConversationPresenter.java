package com.lunaris.ansenuza.application.conversation;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import lombok.RequiredArgsConstructor;

/**
 * Construye y envía los mensajes de presentación reutilizados por varios pasos del bot
 * (listado de localidades y resumen del itinerario), evitando duplicar lógica entre handlers.
 */
@Component
@RequiredArgsConstructor
public class ConversationPresenter {

    private final LocalityRepository localityRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final MessagingPort messaging;

    public void sendAllLocalitiesList(String phoneNumber, String saludo) {
        List<Locality> localities = localityRepository.findLocalitiesWithFares();
        StringBuilder menu = new StringBuilder(saludo)
                .append("📍 *¿Desde qué localidad salís?*\n\n");
        int index = 1;
        for (Locality locality : localities) {
            menu.append("*").append(index).append(")* ").append(locality.getName()).append("\n");
            index++;
        }
        menu.append("\n*0)* Volver al Menú Principal\n\n_Respondé escribiendo únicamente el número que corresponda a tu pueblo de origen._");
        messaging.sendText(phoneNumber, menu.toString());
    }

    public void sendReservationSummaryWithButtons(String phoneNumber, ConversationSession session) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dates = "*Ida:* " + session.getTravelDate().format(formatter);

        if (Boolean.TRUE.equals(session.getRoundTrip())) {
            if (session.getReturnDate() != null) {
                dates += " | *Vuelta:* " + session.getReturnDate().format(formatter);
            } else {
                dates += " | *Vuelta:* 🔄 _ABIERTA_";
            }
        }

        String blockInfo = (session.getCurrentCompanionIndex() != null
                && session.getCurrentCompanionIndex() == 8) ? "08:00 AM" : "03:00 AM";
        String estimatedPickupTime = pricingAndScheduleService.calculateEstimatedPickupTime(
                session.getPickupLocality(),
                (session.getCurrentCompanionIndex() != null
                        && session.getCurrentCompanionIndex() == 8) ? "08:00" : "03:00");

        int totalAsientos =
                session.getPassengerCount() != null ? session.getPassengerCount() : 1;

        BigDecimal price = pricingAndScheduleService.calculateTripPrice(
                session.getPickupLocality(), session.getRoundTrip(), totalAsientos);

        String paxLine = session.getPassengerName();
        if (session.getCompanionNames() != null && !session.getCompanionNames().isBlank()) {
            paxLine += "\n👥 *Acompañantes:* " + session.getCompanionNames();
        }

        String summary = """
                👤 *Pasajero titular:* %s
                🔢 *Asientos a ocupar:* %d
                📍 *Origen:* %s (%s)
                🎯 *Destino:* %s
                🕒 *Horario de cabecera:* %s
                ⏱ *Hora de retiro por tu domicilio:* %s
                🔄 *Modalidad:* %s
                📅 %s
                🧾 *Documento Factura:* %s
                💰 *Valor Total del Traslado:* $%,.2f
                """.formatted(paxLine, totalAsientos, session.getPickupLocality(),
                session.getPickupAddress(), session.getDestination(), blockInfo,
                estimatedPickupTime,
                Boolean.TRUE.equals(session.getRoundTrip()) ? "Ida y vuelta" : "Solo ida",
                dates, session.getCuil(), price);

        messaging.sendButtons(phoneNumber, "Verificación del Itinerario", summary,
                List.of(new Button("confirm_ok", "Confirmar 👍"),
                        new Button("confirm_cancel", "Cancelar ❌")));
    }
}
