package com.lunaris.ansenuza.application.conversation;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;

/**
 * Construye y envía los mensajes de presentation reutilizados por varios pasos del bot
 * (listado de localidades y resumen del itinerario), evitando duplicar lógica entre handlers.
 */
@Component
@RequiredArgsConstructor
public class ConversationPresenter {

    private final LocalityRepository localityRepository;
    private final PassengerRepository passengerRepository; // 💳 Inyectamos el repositorio para leer la billetera virtual
    private final PricingAndScheduleService pricingAndScheduleService;
    private final MessagingPort messaging;

    public void sendAllLocalitiesList(String phoneNumber, String saludo) {
        List<Locality> localities = localityRepository.findAllWithActiveFare();
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

        // 🕒 REPARACIÓN FASE 3: Obtenemos el bloque de horario real guardado en la sesión
        String blockInfo = session.getScheduleBlock() != null ? session.getScheduleBlock() : "03:00 AM";
        
        // Limpiamos el string para el calculador (ej: de "08:00 AM" a "08:00")
        String rawHour = blockInfo.replace(" AM", "").replace(" PM", "").trim();

        String estimatedPickupTime = pricingAndScheduleService.calculateEstimatedPickupTime(
                session.getPickupLocality(), rawHour);

        int totalAsientos =
                session.getPassengerCount() != null ? session.getPassengerCount() : 1;

        // 💰 Cálculo del precio bruto base del viaje
        BigDecimal priceBase = pricingAndScheduleService.calculateTripPrice(
                session.getPickupLocality(), session.getRoundTrip(), totalAsientos);

        // 💳 Verificamos el saldo corriente a favor del pasajero
        BigDecimal saldoAplicado = BigDecimal.ZERO;
        Passenger passenger = passengerRepository.findByPhone(phoneNumber).orElse(null);
        if (passenger != null && passenger.getCurrentBalance() != null) {
            saldoAplicado = passenger.getCurrentBalance();
        }

        // Calculamos el neto final a pagar (sin bajar de cero)
        BigDecimal totalNeto = priceBase.subtract(saldoAplicado);
        if (totalNeto.compareTo(BigDecimal.ZERO) < 0) {
            totalNeto = BigDecimal.ZERO;
        }

        // Si el saldo cubrió más del costo, mostramos solo lo que se restó de forma efectiva
        if (saldoAplicado.compareTo(priceBase) > 0) {
            saldoAplicado = priceBase;
        }

        BigDecimal descuentoPromo = BigDecimal.ZERO;
        if (session.getPromotionDiscountPercentage() != null) {
            descuentoPromo = priceBase.multiply(BigDecimal.valueOf(session.getPromotionDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            totalNeto = totalNeto.subtract(descuentoPromo).max(BigDecimal.ZERO);
        }

        String paxLine = session.getPassengerName();
        if (session.getCompanionNames() != null && !session.getCompanionNames().isBlank()) {
            paxLine += "\n👥 *Acompañantes:* " + session.getCompanionNames();
        }

        // Si la sesión ya precalculó el código base o nexo temporal lo exponemos, sino dejamos el marcador
        String displayCode = session.getReservationCode() != null ? session.getReservationCode().replace("-IDA", "") : "Pendiente asignación";

        String summary = """
                📌 *Nro. de Grupo/Reserva:* %s
                👤 *Pasajero titular:* %s
                🔢 *Asientos a ocupar:* %d
                📍 *Origen:* %s (%s)
                🎯 *Destino:* %s
                🕒 *Horario de cabecera:* %s
                ⏱ *Hora de retiro por tu domicilio:* %s
                🔄 *Modalidad:* %s
                📅 %s
                🧾 *Documento Factura:* %s
                💵 *Precio Base del Viaje:* $%,.2f
                📉 *Saldo a Favor Aplicado:* -$%,.2f
                🎟️ *Descuento promocional:* -$%,.2f
                💰 *Total Neto a Transferir:* $%,.2f
                """.formatted(displayCode, paxLine, totalAsientos, session.getPickupLocality(),
                session.getPickupAddress(), session.getDestination(), blockInfo,
                estimatedPickupTime,
                Boolean.TRUE.equals(session.getRoundTrip()) ? "Ida y vuelta" : "Solo ida",
                dates, session.getCuil(), priceBase, saldoAplicado, descuentoPromo, totalNeto);

        messaging.sendButtons(phoneNumber, "Verificación del Itinerario", summary,
                List.of(new Button("confirm_ok", "Confirmar 👍"),
                        new Button("confirm_cancel", "Cancelar ❌")));
    }
}
