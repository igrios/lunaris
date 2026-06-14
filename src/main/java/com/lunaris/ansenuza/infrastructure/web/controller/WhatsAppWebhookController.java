package com.lunaris.ansenuza.infrastructure.web.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/whatsapp")
@AllArgsConstructor
@Slf4j
public class WhatsAppWebhookController {

        private final WhatsAppService whatsAppService;

        private final ConversationSessionRepository conversationSessionRepository;
        private final LocalityRepository localityRepository;

private final PassengerRepository passengerRepository;

private final ReservationRepository reservationRepository;


        @GetMapping("/webhook")
        public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,
                        @RequestParam("hub.verify_token") String verifyToken,
                        @RequestParam("hub.challenge") String challenge) {

                if ("lunaris123".equals(verifyToken)) {
                        return ResponseEntity.ok(challenge);
                }

                return ResponseEntity.badRequest().build();
        }

        @PostMapping("/webhook")
        public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {

                try {

                        List<Map<String, Object>> entry =
                                        (List<Map<String, Object>>) payload.get("entry");

                        Map<String, Object> change = (Map<String, Object>) ((List<?>) entry.get(0)
                                        .get("changes")).get(0);

                        Map<String, Object> value = (Map<String, Object>) change.get("value");

                        List<Map<String, Object>> messages =
                                        (List<Map<String, Object>>) value.get("messages");

                        if (messages == null || messages.isEmpty()) {
                                return ResponseEntity.ok().build();
                        }

                        Map<String, Object> message = messages.get(0);

                        String from = (String) message.get("from");

                        Map<String, Object> text = (Map<String, Object>) message.get("text");

                        if (text == null) {
                                return ResponseEntity.ok().build();
                        }

                        String body = (String) text.get("body");

                        System.out.println("=================================");
                        System.out.println("FROM: " + from);
                        System.out.println("MESSAGE: " + body);
                        System.out.println("=================================");

                        String destination = normalizeWhatsAppNumber(from);

                        System.out.println("DESTINATION: " + destination);

                        processMessage(destination, body);

                } catch (Exception e) {

                        e.printStackTrace();
                }

                return ResponseEntity.ok().build();
        }

        private void processMessage(String phoneNumber, String message) {

                if (message == null) {
                        return;
                }

                ConversationSession session = conversationSessionRepository
                                .findByPhoneNumber(phoneNumber).orElseGet(() -> {

                                        ConversationSession newSession = ConversationSession
                                                        .builder().phoneNumber(phoneNumber)
                                                        .currentStep("MAIN_MENU").build();

                                        return conversationSessionRepository.save(newSession);
                                });

                String body = message.trim().toLowerCase();

                // MENU PRINCIPAL

                if ("hola".equals(body)) {

                        session.setCurrentStep("MAIN_MENU");

                        conversationSessionRepository.save(session);

                        whatsAppService.sendMessage(phoneNumber, """
                                        🚐 Bienvenido a Lunaris Ansenuza

                                        1️⃣ Reservar viaje

                                        2️⃣ Consultar reserva

                                        3️⃣ Hablar con Martín

                                        Responda con el número.
                                        """);

                        return;
                }

                // RESERVAR

                if ("1".equals(body) && "MAIN_MENU".equals(session.getCurrentStep())) {

                        session.setCurrentStep("ASK_LOCALITY");

                        conversationSessionRepository.save(session);

                        whatsAppService.sendMessage(phoneNumber, buildLocalitiesMenu());

                        return;
                }

                // LOCALIDAD

                if ("ASK_LOCALITY".equals(session.getCurrentStep())) {

                        try {

                                int option = Integer.parseInt(body);

                                List<Locality> localities = localityRepository.findAll();

                                if (option < 1 || option > localities.size()) {

                                        whatsAppService.sendMessage(phoneNumber,
                                                        "Opción inválida.");

                                        return;
                                }

                                Locality selected = localities.get(option - 1);

                                session.setPickupLocality(selected.getName());

                                session.setCurrentStep("ASK_NAME");

                                conversationSessionRepository.save(session);

                                whatsAppService.sendMessage(phoneNumber, """
                                                👤 Indique nombre y apellido del pasajero.

                                                Ejemplo:
                                                Juan Pérez
                                                """);

                                return;

                        } catch (Exception e) {

                                whatsAppService.sendMessage(phoneNumber,
                                                "Debe responder con un número.");

                                return;
                        }
                }

                // NOMBRE

                if ("ASK_NAME".equals(session.getCurrentStep())) {

                        session.setPassengerName(message);

                        session.setCurrentStep("ASK_ADDRESS");

                        conversationSessionRepository.save(session);

                        whatsAppService.sendMessage(phoneNumber, """
                                        📍 Indique la dirección de retiro.

                                        Ejemplo:
                                        Belgrano 123
                                        """);

                        return;
                }

                // DIRECCION

                if ("ASK_ADDRESS".equals(session.getCurrentStep())) {

                        session.setPickupAddress(message);

                        session.setCurrentStep("ASK_DESTINATION");

                        conversationSessionRepository.save(session);

                        whatsAppService.sendMessage(phoneNumber, """
                                        🎯 Seleccione el destino:

                                        1) Aeropuerto Córdoba
                                        2) Córdoba Capital

                                        Responda con el número.
                                        """);

                        return;
                }

                // DESTINO

                if ("ASK_DESTINATION".equals(session.getCurrentStep())) {

                        String destination = null;

                        if ("1".equals(body)) {

                                destination = "Aeropuerto Córdoba";

                        } else if ("2".equals(body)) {

                                destination = "Córdoba";

                        } else {

                                whatsAppService.sendMessage(phoneNumber, """
                                                Opción inválida.

                                                1) Aeropuerto Córdoba
                                                2) Córdoba Capital
                                                """);

                                return;
                        }

                        session.setDestination(destination);

                        session.setCurrentStep("ASK_TRIP_TYPE");

                        conversationSessionRepository.save(session);

                        whatsAppService.sendMessage(phoneNumber, """
                                        🔄 Tipo de viaje:

                                        1) Solo ida
                                        2) Ida y vuelta

                                        Responda con el número.
                                        """);

                        return;
                }

                // TIPO DE VIAJE

                if ("ASK_TRIP_TYPE".equals(session.getCurrentStep())) {

                        if ("1".equals(body)) {

                                session.setRoundTrip(false);

                        } else if ("2".equals(body)) {

                                session.setRoundTrip(true);

                        } else {

                                whatsAppService.sendMessage(phoneNumber, """
                                                Opción inválida.

                                                1) Solo ida
                                                2) Ida y vuelta
                                                """);

                                return;
                        }

                        session.setCurrentStep("ASK_DATE");

                        conversationSessionRepository.save(session);

                        whatsAppService.sendMessage(phoneNumber, """
                                        📅 ¿Qué fecha desea viajar?

                                        Formato:
                                        18/06/2026
                                        """);

                        return;
                }
                if ("ASK_DATE".equals(session.getCurrentStep())) {

                        try {

                                LocalDate travelDate = LocalDate.parse(message,
                                                DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                                if (travelDate.isBefore(LocalDate.now())) {

                                        whatsAppService.sendMessage(phoneNumber, """
                                                        ❌ La fecha no puede ser anterior a hoy.

                                                        Ingrese una fecha válida.
                                                        """);

                                        return;
                                }

                                session.setTravelDate(travelDate);

                                session.setCurrentStep("ASK_INVOICE");

                                conversationSessionRepository.save(session);

                                whatsAppService.sendMessage(phoneNumber, """
                                                🧾 ¿Necesita factura?

                                                1) Sí
                                                2) No
                                                """);

                                return;

                        } catch (Exception e) {

                                whatsAppService.sendMessage(phoneNumber, """
                                                ❌ Fecha inválida.

                                                Utilice formato:

                                                18/06/2026
                                                """);

                                return;
                        }
                }
                if ("ASK_INVOICE".equals(session.getCurrentStep())) {

                        if ("1".equals(body)) {

                                session.setRequiresInvoice(true);

                                session.setCurrentStep("ASK_CUIL");

                                conversationSessionRepository.save(session);

                                whatsAppService.sendMessage(phoneNumber, """
                                                🧾 Ingrese su CUIL/CUIT.

                                                Ejemplo:
                                                20-12345678-9
                                                """);

                                return;
                        }

                        if ("2".equals(body)) {

                                session.setRequiresInvoice(false);

                                session.setCurrentStep("ASK_CONFIRMATION");

                                conversationSessionRepository.save(session);

                                sendReservationSummary(phoneNumber, session);

                                return;
                        }

                        whatsAppService.sendMessage(phoneNumber, """
                                        Opción inválida.

                                        1) Sí
                                        2) No
                                        """);

                        return;
                }

                        if ("ASK_CUIL".equals(session.getCurrentStep())) {
        
                                session.setCuil(body);
        
                                session.setCurrentStep("ASK_CONFIRMATION");
        
                                conversationSessionRepository.save(session);
        
                                sendReservationSummary(phoneNumber, session);
        
                                return;
                        }

if ("ASK_CONFIRMATION".equals(session.getCurrentStep())) {

    if ("1".equals(body)) {

        Passenger passenger =
                passengerRepository
                        .findByPhone(phoneNumber)
                        .orElseGet(() -> {

                            String[] names =
                                    session.getPassengerName()
                                            .trim()
                                            .split("\\s+", 2);

                            String firstName =
                                    names[0];

                            String lastName =
                                    names.length > 1
                                            ? names[1]
                                            : "";

                            Passenger newPassenger =
                                    Passenger.builder()
                                            .firstName(firstName)
                                            .lastName(lastName)
                                            .phone(phoneNumber)
                                            .cuil(session.getCuil())
                                            .address(session.getPickupAddress())
                                            .locality(session.getPickupLocality())
                                            .build();

                            return passengerRepository
                                    .save(newPassenger);
                        });

        Reservation reservation =
                Reservation.builder()
                        .passenger(passenger)
                        .travelDate(session.getTravelDate())
                        .pickupLocality(session.getPickupLocality())
                        .pickupAddress(session.getPickupAddress())
                        .destination(session.getDestination())
                        .roundTrip(session.getRoundTrip())
                        .paymentVerified(false)
                        .amount(BigDecimal.ZERO)
                        .build();

        reservationRepository.save(reservation);

        conversationSessionRepository.delete(session);

        whatsAppService.sendMessage(
                phoneNumber,
                """
                ✅ Reserva registrada correctamente

                Su solicitud fue recibida y quedó registrada en nuestro sistema.

                💳 Alias para transferencia:
                LUNARIS.ANSENUZA

                Una vez realizado el pago, envíe el comprobante por este mismo medio para verificar la operación.

                📞 Nos comunicaremos con usted a la brevedad para coordinar los detalles del viaje y confirmar definitivamente la reserva.

                Gracias por elegir Lunaris Ansenuza 🚐
                """
        );

        return;
    }

    if ("2".equals(body)) {

        conversationSessionRepository.delete(session);

        whatsAppService.sendMessage(
                phoneNumber,
                """
                ❌ Reserva cancelada.

                Si desea realizar una nueva reserva envíe:

                Hola
                """
        );

        return;
    }

    whatsAppService.sendMessage(
            phoneNumber,
            """
            Opción inválida.

            1) Confirmar
            2) Cancelar
            """
    );

    return;
}





                System.out.println("ESTADO ACTUAL: " + session.getCurrentStep());
        }

        private String normalizeWhatsAppNumber(String phone) {

                if (phone != null && phone.startsWith("549")) {

                        return "54" + phone.substring(3);
                }

                return phone;
        }

        private String buildLocalitiesMenu() {

                StringBuilder menu = new StringBuilder("📍 ¿Desde dónde viaja?\n\n");

                int index = 1;

                for (Locality locality : localityRepository.findAll()) {

                        menu.append(index).append(") ").append(locality.getName()).append("\n");

                        index++;
                }

                menu.append("\nResponda con el número.");

                return menu.toString();
        }


        private void sendReservationSummary(String phoneNumber, ConversationSession session) {

                whatsAppService.sendMessage(phoneNumber, """
                                📋 Resumen de la reserva

                                Pasajero: %s
                                Localidad: %s
                                Dirección: %s
                                Destino: %s
                                Viaje: %s
                                Fecha: %s
                                Factura: %s

                                1) Confirmar
                                2) Cancelar
                                """.formatted(session.getPassengerName(),
                                session.getPickupLocality(), session.getPickupAddress(),
                                session.getDestination(),
                                Boolean.TRUE.equals(session.getRoundTrip()) ? "Ida y vuelta"
                                                : "Solo ida",
                                session.getTravelDate(),
                                Boolean.TRUE.equals(session.getRequiresInvoice()) ? "Sí" : "No"));


        }

}


