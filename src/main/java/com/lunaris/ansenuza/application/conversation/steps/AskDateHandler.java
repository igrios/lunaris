package com.lunaris.ansenuza.application.conversation.steps;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.FechaParser;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.OperationControlService; // 👈 NUEVO IMPORT
import com.lunaris.ansenuza.domain.exception.SameDayBookingClosedException;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_DATE: valida la fecha de ida de forma flexible; deriva a fecha de regreso (ida y vuelta) o a facturación. */
@Component
@RequiredArgsConstructor
public class AskDateHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;
    private final OperationControlService operationControlService; // 👈 CONTROL DE JORNADA INYECTADO
    private final SameDayBookingPolicy sameDayBookingPolicy;

    @Override
    public String step() {
        return "ASK_DATE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();

        String rawBody = message.body() == null ? "" : message.body().trim().toLowerCase();
        if ("return_other_date".equals(rawBody) || "otra fecha".equals(rawBody)
                || "otra_fecha".equals(rawBody)) {
            session.setCurrentStep("ASK_DATE");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "✍️ *Por favor, ingresá la fecha en la que querés viajar* "
                            + "(ejemplo: 12/08 o 12 de agosto).");
            return;
        }

        // Usamos el parseador flexible para extraer la fecha sin importar los ceros o el año corto
        Optional<LocalDate> fechaParseada = FechaParser.parsear(message.body());

        if (fechaParseada.isEmpty()) {
            messaging.sendText(phoneNumber,
                    "❌ *Formato erróneo.* Por favor, indicá la fecha de tu viaje "
                            + "(por ejemplo: 12/08/2026):");
            return;
        }

        LocalDate travelDate = fechaParseada.get();
        LocalDate hoy = com.lunaris.ansenuza.shared.ArgentinaTime.today();

        // Mantenemos tu validación de negocio intacta
        if (travelDate.isBefore(hoy)) {
            messaging.sendText(phoneNumber,
                    "❌ La fecha no puede ser anterior a hoy. Reingresá:");
            return;
        }

        try {
            sameDayBookingPolicy.validate(travelDate, session.getScheduleBlock());
        } catch (SameDayBookingClosedException exception) {
            messaging.sendText(phoneNumber, exception.getMessage());
            return;
        }

        // ⏱️ REGLA DE ORO LOGÍSTICA: Control de corte para el día siguiente (Deadline 19:00 Hs)
        if (travelDate.equals(hoy.plusDays(1)) && operationControlService.isPastCutoffTime()) {
            messaging.sendText(phoneNumber,
                    "⏱️ *Logística Cerrada para Mañana.*\n\nTe recordamos que las reservas para viajar al día siguiente cierran estrictamente a las *19:00 Hs* para poder asignar unidades y garantizar el descanso reglamentario de nuestros choferes. 🚐💤\n\nPor favor, ingresá una fecha alternativa a partir de pasados mañana:");
            return;
        }
        
        session.setTravelDate(travelDate);

        if (Boolean.TRUE.equals(session.getRoundTrip())) {
            session.setCurrentStep("ASK_RETURN_DATE_TYPE");
            conversationSessionRepository.saveAndFlush(session);

            messaging.sendButtons(phoneNumber, "Fecha de Regreso",
                    "📅 *¿Cuándo programamos el regreso desde Córdoba?*\n\nSi todavía no sabés el día exacto, podés dejar la fecha abierta y coordinarla más adelante con Martín.",
                    List.of(new Button("return_same_day", "Vuelvo en el día"),
                            new Button("return_choose_date", "Otra fecha")));
        } else {
            session.setCurrentStep("ASK_DNI_REQUIRED");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números):");
        }
    }
}
