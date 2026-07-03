package com.lunaris.ansenuza.application.conversation.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.Button; // Coincide con tu record Button
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

/**
 * Handler interactivo de Cancelación de Viajes para Lunaris.
 * Usa los botones nativos del MessagingPort, cancela itinerarios completos y maneja pagos pendientes.
 */
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

        // 1. Si el usuario escribe "MENÚ", volvemos al inicio sin alterar nada
        if ("MENÚ".equals(input)) {
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber, "Volviendo al menú principal...");
            return;
        }

        // 2. Buscamos si el texto ingresado (o el ID del botón presionado) es un código de reserva válido
        Optional<Reservation> optRes = reservationRepository.findByReservationCode(input);

        if (optRes.isPresent()) {
            Reservation res = optRes.get();

            // Validación de seguridad por número de teléfono
            if (!res.getPassenger().getPhone().equals(phoneNumber)) {
                messaging.sendText(phoneNumber,
                        "⚠️ El viaje seleccionado no corresponde a tu número por cuestiones de seguridad.");
                return;
            }

            // 🛑 REGLA DE NEGOCIO 1: Si el estado es PENDING_PAYMENT, se cancela sin saldos a favor
            boolean esPagoPendiente = "PENDING_PAYMENT".equalsIgnoreCase(res.getStatus());

            // 🛑 REGLA DE NEGOCIO 2: Si el viaje incluye ida y vuelta (roundTrip), se liquida completo
            boolean esViajeCompleto = res.getRoundTrip() != null && res.getRoundTrip();

            // Ejecutamos la baja de la reserva
            reservationService.cancelReservation(res.getId(), "BOT_WHATSAPP");
            
            if (esViajeCompleto) {
                if (esPagoPendiente) {
                    messaging.sendText(phoneNumber,
                            "✅ Tu viaje completo (Ida y Vuelta) ha sido cancelado con éxito.\n\n" +
                            "Al encontrarse la reserva con *pago pendiente*, la operación se cerró sin cargos adicionales.\n\n" +
                            "Escribí *Menú* para regresar.");
                } else {
                    messaging.sendText(phoneNumber,
                            "✅ Tu viaje completo (Ida y Vuelta) ha sido dado de baja.\n\n" +
                            "Un operador revisará el saldo correspondiente para tus próximos viajes.\n\n" +
                            "Escribí *Menú* para regresar.");
                }
            } else {
                if (esPagoPendiente) {
                    messaging.sendText(phoneNumber,
                            "✅ La reserva *" + input + "* ha sido cancelada.\n\n" +
                            "Al figurar como *pago pendiente*, la operación se cerró sin cargos adicionales.\n\n" +
                            "Escribí *Menú* para regresar.");
                } else {
                    messaging.sendText(phoneNumber,
                            "✅ La reserva *" + input + "* ha sido cancelada con éxito.\n\n" +
                            "Escribí *Menú* para regresar.");
                }
            }

            // Reiniciamos el flujo conversacional para la próxima interacción
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);

        } else {
            // 3. Traemos todas las reservas asociadas al teléfono del pasajero
            List<Reservation> todasLasReservas = reservationRepository.findByPassengerPhone(phoneNumber);
            
            // Filtramos en memoria para excluir únicamente las que ya están canceladas
            List<Reservation> reservasActivas = todasLasReservas.stream()
                    .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                    .toList();

            if (reservasActivas.isEmpty()) {
                messaging.sendText(phoneNumber, "⚠️ No registrás ningún viaje activo o próximo para poder cancelar.\n\nEscribí *Menú* para volver.");
                session.setCurrentStep("START");
                conversationSessionRepository.saveAndFlush(session);
            } else {
                // Mapeamos las reservas a objetos Button nativos (Meta soporta un máximo de 3 botones)
                List<Button> botonesAEnviar = new ArrayList<>();
                int limite = Math.min(reservasActivas.size(), 3);
                
                for (int i = 0; i < limite; i++) {
                    Reservation r = reservasActivas.get(i);
                    // Label ultra corto ("Cancelar XXXXX") para cumplir rigurosamente el límite de Meta
                    String label = "Cancelar " + r.getReservationCode(); 
                    
                    botonesAEnviar.add(new Button(r.getReservationCode(), label));
                }

                // Disparamos la botonera nativa de WhatsApp Cloud API
                messaging.sendButtons(
                    phoneNumber,
                    "Gestión de Cancelaciones 🚐",
                    "Seleccioná de la pantalla cuál de tus próximos viajes deseas dar de baja de forma automática:",
                    botonesAEnviar
                );
            }
        }
    }
}