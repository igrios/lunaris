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
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CancelReservationHandler implements ConversationStepHandler {

    private static final String DISPATCHED_MESSAGE =
            "⚠️ Tu viaje ya fue asignado al chofer y la ruta está en curso. "
                    + "Para cancelar o modificar tu reserva, por favor comunicate con un operador.";

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

        // Si viene del menú principal, mandamos a mostrar las opciones directamente
        if ("5".equals(input) || input.contains("CANCELAR")) {
            mostrarBotoneraOCodigo(session, phoneNumber);
            return;
        }

        Optional<Reservation> optRes = reservationRepository.findByReservationCode(input);

        if (optRes.isPresent()) {
            Reservation res = optRes.get();

            if (!res.getPassenger().getPhone().equals(phoneNumber)) {
                messaging.sendText(phoneNumber, "⚠️ El viaje seleccionado no corresponde a tu número por seguridad.");
                return;
            }

            if (res.getTravelStatus() == Reservation.TravelStatus.COMPLETED
                    || res.getTravelStatus() == Reservation.TravelStatus.REALIZED
                    || "COMPLETED".equalsIgnoreCase(res.getStatus())) {
                messaging.sendText(phoneNumber,
                        "⚠️ Ya te encontrás a bordo o tu viaje ya finalizó. No es posible cancelar este servicio.");
                return;
            }
            if (res.getDriver() != null
                    || res.getTravelStatus() == Reservation.TravelStatus.ROUTE_SENT) {
                messaging.sendText(phoneNumber, DISPATCHED_MESSAGE);
                return;
            }

            boolean reembolsoHabilitado = reservationService.isRefundEligible(res);
            boolean esViajeCompleto = res.getRoundTrip() != null && res.getRoundTrip();

            try {
                reservationService.cancelReservation(res.getId(), "BOT_WHATSAPP");
            } catch (ReservationAlreadyCompletedException exception) {
                messaging.sendText(phoneNumber,
                        "⚠️ Ya te encontrás a bordo o tu viaje ya finalizó. No es posible cancelar este servicio.");
                return;
            } catch (IllegalStateException exception) {
                messaging.sendText(phoneNumber,
                        "⚠️ Tu viaje ya se encuentra en proceso o la ruta fue asignada al chofer, "
                                + "por lo que no es posible realizar la cancelación por este medio. "
                                + "Comunícate con un operador.");
                return;
            } catch (DomainValidationException exception) {
                String detail = exception.getMessage();
                messaging.sendText(phoneNumber,
                        detail != null && detail.startsWith("⚠️") ? detail : "⚠️ " + detail);
                return;
            }

            if (!reembolsoHabilitado) {
                messaging.sendText(phoneNumber,
                        "Tu reserva fue cancelada. Dado que el pago aún no había sido verificado por administración, no se acreditó saldo a favor.");
            } else if (esViajeCompleto) {
                    messaging.sendText(phoneNumber, "✅ Tu viaje completo (Ida y Vuelta) ha sido dado de baja.\n\nUn operador revisará el saldo correspondiente para tus próximos viajes.\n\nEscribí *Menú* para regresar.");
            } else {
                messaging.sendText(phoneNumber, "✅ La reserva *" + input + "* ha sido cancelada con éxito.\n\nEscribí *Menú* para regresar.");
            }

            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
        } else {
            messaging.sendText(phoneNumber, "⚠️ Código inválido.");
            mostrarBotoneraOCodigo(session, phoneNumber);
        }
    }

    private void mostrarBotoneraOCodigo(ConversationSession session, String phoneNumber) {
        List<Reservation> todasLasReservas = reservationRepository.findByPassengerPhone(phoneNumber);
        List<Reservation> reservasActivas = todasLasReservas.stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !"COMPLETED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> r.getTravelStatus() != Reservation.TravelStatus.COMPLETED)
                .toList();

        if (reservasActivas.isEmpty()) {
            messaging.sendText(phoneNumber, "⚠️ No registrás ningún viaje activo o próximo para poder cancelar.\n\nEscribí *Menú* para volver.");
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
            return;
        }

        List<Button> botonesAEnviar = new ArrayList<>();
        int limite = Math.min(reservasActivas.size(), 3);

        for (int i = 0; i < limite; i++) {
            Reservation r = reservasActivas.get(i);
            String codigo = r.getReservationCode().trim();
            
            // 🛡️ SOLUCIÓN AL ERROR 400 DE META: El label del botón jamás supera los 20 caracteres
            String label = codigo.length() > 20 ? codigo.substring(0, 20) : codigo;
            botonesAEnviar.add(new Button(codigo, label));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 *Gestión de Cancelaciones* 🚐\n\n");
        sb.append("Seleccioná cuál de tus próximos viajes deseas dar de baja automáticamente.\n");
        sb.append("Por favor, *escribí el código* del viaje a cancelar:\n\n");
        for (int i = 0; i < limite; i++) {
            Reservation r = reservasActivas.get(i);
            sb.append(String.format("🔹 *%s* (%s ➡️ %s)\n", r.getReservationCode(), r.getPickupLocality(), r.getDestination()));
        }
        sb.append("\nEscribí *Menú* para regresar.");

        try {
            messaging.sendButtons(
                    phoneNumber,
                    "Gestión de Cancelaciones 🚐",
                    "Seleccioná de la pantalla cuál de tus próximos viajes deseas dar de baja:",
                    botonesAEnviar
            );
        } catch (Exception e) {
            messaging.sendText(phoneNumber, sb.toString());
        }
    }
}
