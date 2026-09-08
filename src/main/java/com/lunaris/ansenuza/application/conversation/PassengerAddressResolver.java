package com.lunaris.ansenuza.application.conversation;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;

/**
 * Decide el siguiente paso de la conversación según conozcamos o no el domicilio habitual
 * del pasajero: si ya tenemos dirección en la misma localidad, ofrece confirmarla; si no,
 * pide la dirección por texto. Reutilizado al terminar la carga de acompañantes.
 */
@Component
@RequiredArgsConstructor
public class PassengerAddressResolver {

    private final PassengerRepository passengerRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    public void resolve(String phoneNumber, ConversationSession session) {
        Optional<Passenger> passengerOpt = passengerRepository.findByPhone(phoneNumber);
        if (passengerOpt.isPresent() && passengerOpt.get().getAddress() != null
                && passengerOpt.get().getLocality() != null) {
            Passenger p = passengerOpt.get();
            if (session.getPickupLocality().equalsIgnoreCase(p.getLocality())) {
                session.setPickupAddress(p.getAddress());
                session.setCurrentStep("CONFIRM_ADDRESS_BUTTONS");
                conversationSessionRepository.saveAndFlush(session);

                messaging.sendButtons(phoneNumber, "Dirección de Retiro",
                        "📍 *Detectamos tu domicilio habitual en " + p.getLocality() + ":*\n"
                                + p.getAddress() + "\n\n¿Pasamos a buscarte por acá?",
                        List.of(new Button("addr_yes", "Sí, pasar por acá ✅"),
                                new Button("addr_no", "Nueva Dirección 🏠")));
                return;
            }
        }
        session.setCurrentStep("ASK_ADDRESS_TEXT");
        conversationSessionRepository.saveAndFlush(session);
        messaging.requestLocation(phoneNumber,
                "🏠 Escribí la calle y número donde pasamos a buscarte en "
                        + session.getPickupLocality()
                        + " (ej.: Av. San Martín 450), o tocá el botón para compartir tu ubicación.");
    }
}
