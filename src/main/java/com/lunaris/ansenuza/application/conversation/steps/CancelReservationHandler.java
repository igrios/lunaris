package com.lunaris.ansenuza.application.conversation.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CancelReservationHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "WAITING_CANCEL_CODE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String input = message.body().trim().toUpperCase();

        System.out.println("[BOT-CANCEL] Entrando al handler. Input recibido: " + input);

        if ("MENÚ".equals(input) || "MENU".equals(input)) {
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber, "Volviendo al menú principal...");
            return;
        }

        // 💡 CORRECCIÓN CRUCIAL: Si el input es "5" o "CANCELAR", significa que recién viene del menú principal.
        // No tenemos que buscar una reserva llamada "5", tenemos que mostrar los botones directo.
        if ("5".equals(input) || "CANCELAR".equals(input)) {
            System.out.println("[BOT-CANCEL] Detectado redireccionamiento inicial. Mostrando opciones al pasajero.");
            mostrarBotoneraOCodigo(session, phoneNumber);
            return;
        }

        // Si no es el comando inicial, asumimos que tocó un botón o tipeó un código manual
        Optional<Reservation> optRes = reservationRepository.findByReservationCode(input);

        if (optRes.isPresent()) {
            Reservation res = optRes.get();
            System.out.println("[BOT-CANCEL] Reserva encontrada para cancelar: " + res.getReservationCode());

            if (!res.getPassenger().getPhone().equals(phoneNumber)) {
                messaging.sendText(phoneNumber, "⚠️ El viaje seleccionado no corresponde a tu número por seguridad.");
                return;
            }

            boolean esPagoPendiente = "PENDING_PAYMENT".equalsIgnoreCase(res.getStatus());
            boolean esViajeCompleto = res.getRoundTrip() != null && res.getRoundTrip();

            reservationService.cancelReservation(res.getId(), "BOT_WHATSAPP");
            
            if (esViajeCompleto) {
                if (esPagoPendiente) {
                    messaging.sendText(phoneNumber, "✅ Tu viaje completo (Ida y Vuelta) ha sido cancelado con éxito.\n\nAl encontrarse la reserva con *pago pendiente*, la operación se cerró sin cargos adicionales.\n\nEscribí *Menú* para regresar.");
                } else {
                    messaging.sendText(phoneNumber, "✅ Tu viaje completo (Ida y Vuelta) ha sido dado de baja.\n\nUn operador revisará el saldo correspondiente para tus próximos viajes.\n\nEscribí *Menú* para regresar.");
                }
            } else {
                if (esPagoPendiente) {
                    messaging.sendText(phoneNumber, "✅ La reserva *" + input + "* ha sido cancelada.\n\nAl figurar como *pago pendiente*, la operación se cerró sin cargos adicionales.\n\nEscribí *Menú* para regresar.");
                } else {
                    messaging.sendText(phoneNumber, "✅ La reserva *" + input + "* ha sido cancelada con éxito.\n\nEscribí *Menú* para regresar.");
                }
            }

            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
        } else {
            System.out.println("[BOT-CANCEL] El input no coincide con ninguna reserva. Re-mostrando opciones.");
            messaging.sendText(phoneNumber, "⚠️ Código inválido.");
            mostrarBotoneraOCodigo(session, phoneNumber);
        }
    }

    private void mostrarBotoneraOCodigo(ConversationSession session, String phoneNumber) {
        List<Reservation> todasLasReservas = reservationRepository.findByPassengerPhone(phoneNumber);
        List<Reservation> reservasActivas = todasLasReservas.stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .toList();

        System.out.println("[BOT-CANCEL] Cantidad de reservas activas encontradas en BD: " + reservasActivas.size());

        if (reservasActivas.isEmpty()) {
            System.out.println("[BOT-CANCEL] Sin reservas activas. Enviando texto alternativo.");
            messaging.sendText(phoneNumber, "⚠️ No registrás ningún viaje activo o próximo para poder cancelar.\n\nEscribí *Menú* para volver.");
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
        } else {
            List<Button> botonesAEnviar = new ArrayList<>();
            int limite = Math.min(reservasActivas.size(), 3);
            
            for (int i = 0; i < limite; i++) {
                Reservation r = reservasActivas.get(i);
                String label = "Cancelar " + r.getReservationCode(); 
                botonesAEnviar.add(new Button(r.getReservationCode(), label));
            }

            System.out.println("[BOT-CANCEL] Enviando " + botonesAEnviar.size() + " botones interactivos a WhatsApp.");
            messaging.sendButtons(
                phoneNumber,
                "Gestión de Cancelaciones 🚐",
                "Seleccioná de la pantalla cuál de tus próximos viajes deseas dar de baja automáticamente:",
                botonesAEnviar
            );
        }
    }
}