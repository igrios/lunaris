package com.lunaris.ansenuza.application.conversation.steps;

import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

/** WAITING_CANCEL_CODE: valida el código de reserva ingresado y la cancela (baja lógica). */
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
        String codigoIngresado = message.body().trim().toUpperCase();
        Optional<Reservation> optRes = reservationRepository.findByReservationCode(codigoIngresado);

        if (optRes.isPresent()) {
            Reservation res = optRes.get();
            if (!res.getPassenger().getPhone().equals(phoneNumber)) {
                messaging.sendText(phoneNumber,
                        "⚠️ El código ingresado no corresponde a tu número por cuestiones de seguridad.");
                return;
            }
            reservationService.cancelReservation(res.getId(), "BOT_WHATSAPP");
            messaging.sendText(phoneNumber,
                    "✅ ¡Listo! La reserva con código *" + codigoIngresado
                            + "* ha sido cancelada con éxito.\n\nEscribí *Menú* para volver a empezar.");
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
        } else {
            messaging.sendText(phoneNumber,
                    "⚠️ No encontré reservas con el código *" + codigoIngresado
                            + "*. Verificalo o escribí *Menú* para salir.");
        }
    }
}
