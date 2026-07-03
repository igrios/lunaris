package com.lunaris.ansenuza.application.conversation.steps;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.context.annotation.Lazy; // 👈 AGREGADO PARA EVITAR CONFLICTOS DE INYECCIÓN
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationPresenter;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;

/** MAIN_MENU: resuelve la opción elegida (1 a 5) del menú principal. */
@Component
public class MainMenuHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final ReservationRepository reservationRepository;
    private final ConversationPresenter presenter;
    private final MessagingPort messaging;
    private final OperationControlService operationControlService;
    private final CancelReservationHandler cancelReservationHandler; // 👈 INYECTAMOS TU NUEVO HANDLER

    public MainMenuHandler(
            ConversationSessionRepository conversationSessionRepository,
            ReservationRepository reservationRepository,
            ConversationPresenter presenter,
            MessagingPort messaging,
            OperationControlService operationControlService,
            @Lazy CancelReservationHandler cancelReservationHandler) { // 👈 @Lazy evita acoplamientos circulares
        this.conversationSessionRepository = conversationSessionRepository;
        this.reservationRepository = reservationRepository;
        this.presenter = presenter;
        this.messaging = messaging;
        this.operationControlService = operationControlService;
        this.cancelReservationHandler = cancelReservationHandler;
    }

    @Override
    public String step() {
        return "MAIN_MENU";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("1".equals(body) || body.contains("reservar")) {
            if (!operationControlService.isHumanActionEnabled()) {
                messaging.sendText(phoneNumber,
                        "🌙 *Horario de Atención Finalizado*\n\nNuestras vans están descansando. Podés gestionar tus reservas de Lunes a Viernes de 08:00 a 20:00 hs.\n\n¡Gracias por elegir Lunaris!");
                session.setCurrentStep("START");
                conversationSessionRepository.saveAndFlush(session);
                return;
            }
            messaging.sendText(phoneNumber, "✨ *¡Vamos a armar tu viaje!* 🚐\n\nPor favor, ingresá la *Localidad de Origen* de tu viaje (Ejemplo: `San Francisco`, `Córdoba`, `Miramar`):");
            session.setCurrentStep("WAITING_ORIGIN");
            conversationSessionRepository.saveAndFlush(session);
            return;
        } else if ("2".equals(body) || body.contains("estado")) {
            messaging.sendText(phoneNumber, "🔍 Mandanos el código de tu reserva para verificar el estado.");
            session.setCurrentStep("WAITING_STATUS_CODE");
            conversationSessionRepository.saveAndFlush(session);
            return;
        } else if ("3".equals(body) || body.contains("operador") || body.contains("humano")) {
            session.setBotPaused(true);
            session.setCurrentStep("TALKING_TO_HUMAN");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber, "🔔 *Pasando con un operador...*\n\nEl bot se ha pausado. Martín o Ignacio te van a atender de forma manual en breves minutos. ¡Muchas gracias por tu paciencia!");
            return;
        } else if ("4".equals(body) || body.contains("mis viajes") || body.contains("historial")) {
            List<Reservation> misReservas = reservationRepository.findByPassengerPhone(phoneNumber);
            if (misReservas.isEmpty()) {
                messaging.sendText(phoneNumber, "⚠️ No encontramos viajes asociados a tu número de teléfono.\n\nEscribí *Menú* para volver a ver las opciones.");
                session.setCurrentStep("START");
                conversationSessionRepository.saveAndFlush(session);
                return;
            }

            StringBuilder listado = new StringBuilder("📋 *Tus Próximos Viajes en Lunaris* 🚐\n\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (int i = 0; i < misReservas.size(); i++) {
                Reservation r = misReservas.get(i);
                String fechaStr = r.getTravelDate() != null ? r.getTravelDate().format(formatter) : "A confirmar";
                listado.append(String.format("🔹 *Viaje #%d*\n", i + 1));
                listado.append(String.format("🆔 Código: *%s*\n", r.getReservationCode()));
                listado.append(String.format("📅 Fecha: %s\n", fechaStr));
                listado.append(String.format("📍 Ruta: %s ➡️ %s\n", r.getPickupLocality(), r.getDestination()));
                listado.append(String.format("💵 Estado: %s\n\n", "CONFIRMED".equals(r.getStatus()) ? "✅ Confirmado" : "⏳ Pago Pendiente"));
            }

            listado.append("Escribí *Menú* para volver a la pantalla de opciones.");
            messaging.sendText(phoneNumber, listado.toString());
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
            return;
        } else if ("5".equals(body) || body.contains("cancelar")) {
            // 🚀 MODIFICACIÓN DIRECTA INTERACTIVA:
            // Seteamos el paso correspondiente en la sesión
            session.setCurrentStep("WAITING_CANCEL_CODE");
            conversationSessionRepository.saveAndFlush(session);
            
            // Forzamos la ejecución en tiempo real de tu nuevo handler de botones
            cancelReservationHandler.handle(session, message);
            return;
        } else {
            messaging.sendText(phoneNumber, "⚠️ Opción inválida. Por favor, seleccioná una opción válida (1 al 5) o escribí *Menú*.");
        }
    }
}