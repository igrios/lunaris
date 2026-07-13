package com.lunaris.ansenuza.application.conversation.steps;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MainMenuHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final ReservationRepository reservationRepository;
    private final ConversationPresenter presenter;
    private final MessagingPort messaging;
    private final OperationControlService operationControlService;
    private final CancelReservationHandler cancelReservationHandler; // Inyectamos el handler para invocarlo directo

    @Override
    public String step() {
        return "MAIN_MENU";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if ("1".equals(body)) {
            session.setCurrentStep("ASK_LOCALITY");
            conversationSessionRepository.saveAndFlush(session);
            presenter.sendAllLocalitiesList(phoneNumber, "📍 *Excelente elección.* ");
            return;
        } else if ("2".equals(body)) {
            session.setCurrentStep("ASK_LOCALITY");
            conversationSessionRepository.saveAndFlush(session);
            String ganchoMarketing = "💰 *¡Viajá al mejor precio con Lunaris Ansenusa!*\\nContamos con las tarifas más competitivas del sector, descuentos especiales por tramos de ida y vuelta coordinados, y unidades premium climatizadas con total puntualidad.\\n\\n";
            presenter.sendAllLocalitiesList(phoneNumber, ganchoMarketing);
            return;
        } else if ("3".equals(body)) {
            if (!operationControlService.isHumanActionEnabled()) {
                messaging.sendText(phoneNumber, "🌙 *Atención Telefónica Finalizada.*\\n\\nNuestro equipo humano se encuentra descansando en este momento para iniciar las rutas temprano. 🚐💨\\n\\nTe sugerimos usar las opciones *1* o *2* para registrar tu viaje de forma **100% automática** en menos de un minuto. ¡El bot te guiará solo!");
                return;
            }
            session.setBotPaused(true);
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber, "🔔 *Un operador fue notificado.* En instantes Martín se comunicará con vos de forma manual por este canal. ¡Muchas gracias por tu paciencia!");
            return;
        } else if ("4".equals(body) || body.contains("consultar")) {
            List<Reservation> viajesActivos = reservationRepository.findByPassengerPhone(phoneNumber).stream()
                    .filter(r -> !"CANCELLED".equals(r.getStatus()))
                    .toList();

            if (viajesActivos.isEmpty()) {
                messaging.sendText(phoneNumber, "No encontré ningún viaje activo o pendiente agendado con tu número de teléfono. 🤷‍♂️");
                session.setCurrentStep("START");
                conversationSessionRepository.saveAndFlush(session);
                return;
            }

            StringBuilder listado = new StringBuilder("📋 *TUS PRÓXIMOS VIAJES EN LUNARIS:*\\n\\n");
            LocalDate fechaCentinela = LocalDate.of(2099, 12, 31);

            for (int i = 0; i < viajesActivos.size(); i++) {
                Reservation r = viajesActivos.get(i);
                String fechaStr = r.getTravelDate().equals(fechaCentinela) ? "🛑 VUELTA ABIERTA (Pendiente confirmar)" : r.getTravelDate().format(dateFormatter);
                listado.append(String.format("🔹 *Viaje #%d*\\n", i + 1));
                listado.append(String.format("🆔 Código: *%s*\\n", r.getReservationCode()));
                listado.append(String.format("📅 Fecha: %s\\n", fechaStr));
                listado.append(String.format("📍 Ruta: %s ➡️ %s\\n", r.getPickupLocality(), r.getDestination()));
                listado.append(String.format("💵 Estado: %s\\n\\n", "CONFIRMED".equals(r.getStatus()) ? "✅ Confirmado" : "⏳ Pago Pendiente"));
            }
            listado.append("Escribí *Menú* para volver a la pantalla de opciones.");
            messaging.sendText(phoneNumber, listado.toString());
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
            return;
        } else if ("5".equals(body) || body.contains("cancelar")) {
            // 💡 FLUJO OPTIMIZADO: Cambiamos de paso y llamamos en caliente al handler de cancelaciones de inmediato
            session.setCurrentStep("WAITING_CANCEL_CODE");
            conversationSessionRepository.saveAndFlush(session);
            cancelReservationHandler.handle(session, message);
            return;
        } else {
            messaging.sendText(phoneNumber, "⚠️ Opción inválida. Por favor, seleccioná una opción del menú (1 al 5) o escribí *Menú*.");
        }
    }
}