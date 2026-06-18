package com.lunaris.ansenuza.infrastructure.web.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.storage.LocalReceiptStorageService;
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
        private final LocalReceiptStorageService localReceiptStorageService;
        private final PricingAndScheduleService pricingAndScheduleService;

        @GetMapping("/webhook")
        public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,
                        @RequestParam("hub.verify_token") String verifyToken,
                        @RequestParam("hub.challenge") String challenge) {
                if ("lunaris123".equals(verifyToken)) return ResponseEntity.ok(challenge);
                return ResponseEntity.badRequest().build();
        }

        @PostMapping("/webhook")
        public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
                try {
                        List<Map<String, Object>> entry = (List<Map<String, Object>>) payload.get("entry");
                        if (entry == null || entry.isEmpty()) return ResponseEntity.ok().build();

                        Map<String, Object> change = (Map<String, Object>) ((List<?>) entry.get(0).get("changes")).get(0);
                        Map<String, Object> value = (Map<String, Object>) change.get("value");
                        List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");

                        if (messages == null || messages.isEmpty()) return ResponseEntity.ok().build();

                        Map<String, Object> message = messages.get(0);
                        String from = (String) message.get("from");
                        String type = (String) message.get("type");
                        String destination = normalizeWhatsAppNumber(from);

                        final String finalType = type;
                        String derivedBody = null;
                        String mediaId = null;

                        if ("image".equals(type)) {
                                Map<String, Object> imageData = (Map<String, Object>) message.get("image");
                                mediaId = imageData != null ? (String) imageData.get("id") : null;
                        } else if ("text".equals(type)) {
                                Map<String, Object> text = (Map<String, Object>) message.get("text");
                                if (text != null) derivedBody = (String) text.get("body");
                        } else if ("interactive".equals(type)) {
                                Map<String, Object> interactive = (Map<String, Object>) message.get("interactive");
                                if (interactive != null) {
                                        String interactiveType = (String) interactive.get("type");
                                        if ("button_reply".equals(interactiveType)) {
                                                derivedBody = (String) ((Map<String, Object>) interactive.get("button_reply")).get("id");
                                        }
                                }
                        }

                        final String finalDerivedBody = derivedBody;
                        final String finalMediaId = mediaId;

                        CompletableFuture.runAsync(() -> {
                                try {
                                        if ("image".equals(finalType) && finalMediaId != null) {
                                                procesarComprobanteDePago(destination, finalMediaId);
                                        } else if (finalDerivedBody != null) {
                                                processMessage(destination, finalDerivedBody);
                                        }
                                } catch (Exception ex) {
                                        log.error("Error asincrónico: ", ex);
                                }
                        });

                        return ResponseEntity.ok().build();
                } catch (Exception e) {
                        log.error("Error crítico: ", e);
                        return ResponseEntity.ok().build();
                }
        }

        private void procesarComprobanteDePago(String destination, String mediaId) {
                Optional<Passenger> passengerOpt = passengerRepository.findByPhone(destination);
                if (passengerOpt.isPresent()) {
                        List<Reservation> activeReservations = reservationRepository
                                        .findByPassengerOrderByTravelDateAsc(passengerOpt.get());
                        Optional<Reservation> pendingReservation = activeReservations.stream()
                                        .filter(r -> Boolean.FALSE.equals(r.getPaymentVerified()))
                                        .reduce((first, second) -> second);

                        if (pendingReservation.isPresent()) {
                                Reservation reservation = pendingReservation.get();
                                String localWebUrl = localReceiptStorageService.downloadAndSaveReceipt(mediaId);
                                if (localWebUrl != null) {
                                        reservation.setPaymentReceiptUrl(localWebUrl);
                                        reservation.setStatus("RECEIPT_SUBMITTED"); 
                                        reservationRepository.saveAndFlush(reservation);
                                }
                        }
                }
                whatsAppService.sendMessage(destination, "✅ *Comprobante recibido.*\n\nNuestro equipo verificará la transferencia y confirmará tu viaje a la brevedad.");
        }

        private void processMessage(String phoneNumber, String message) {
                if (message == null) return;
                String body = message.trim().toLowerCase();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                ConversationSession session = conversationSessionRepository
                                .findByPhoneNumber(phoneNumber).orElseGet(() -> {
                                        ConversationSession newSession = ConversationSession.builder()
                                                        .phoneNumber(phoneNumber).currentStep("START").build();
                                        return conversationSessionRepository.saveAndFlush(newSession);
                                });

                boolean isGreeting = "hola".equals(body) || "buen dia".equals(body) || "buenas".equals(body) || "menu".equals(body);

                // =====================================================================
                // PASO 1: SALUDO INTELIGENTE Y LISTADO DE PUEBLOS
                // =====================================================================
                if ("START".equals(session.getCurrentStep()) || isGreeting) {
                        session.setCurrentStep("ASK_LOCALITY");
                        conversationSessionRepository.saveAndFlush(session);

                        Optional<Passenger> existingPassenger = passengerRepository.findByPhone(phoneNumber);
                        String saludo = existingPassenger.isPresent() 
                                ? "¡Hola de nuevo, *" + existingPassenger.get().getFirstName() + "*! 👋\n" 
                                : "¡Bienvenido a Lunaris Ansenuza! 🚐\n";

                        sendAllLocalitiesList(phoneNumber, saludo);
                        return;
                }

                // =====================================================================
                // PASO 2: COTIZACIÓN Y HORARIOS DE PASO
                // =====================================================================
                if ("ASK_LOCALITY".equals(session.getCurrentStep())) {
                        try {
                                int option = Integer.parseInt(body);
                                List<Locality> localities = localityRepository.findAll();

                                if (option < 1 || option > localities.size()) {
                                        whatsAppService.sendMessage(phoneNumber, "❌ Selección inválida. Ingresá un número de la lista.");
                                        return;
                                }

                                Locality selected = localities.get(option - 1);
                                session.setPickupLocality(selected.getName());
                                session.setCurrentStep("ASK_MARKETING_CONFIRMATION");
                                conversationSessionRepository.saveAndFlush(session);

                               BigDecimal baseFare = pricingAndScheduleService.calculateTripPrice(selected.getName(), false, 1);
                                String primerHorario = pricingAndScheduleService.calculateEstimatedPickupTime(selected.getName(), "03:00");
                                String segundoHorario = pricingAndScheduleService.calculateEstimatedPickupTime(selected.getName(), "08:00");

                                // 🎲 Generamos un número aleatorio dinámico entre 1 y 4
                                
                                int lugaresDisponibles = new java.util.Random().nextInt(3) + 1;

                                String text = """
                                                💰 *Cotización para %s:*
                                                El valor base hacia Córdoba es de *$%,.0f*.
                                                
                                                ⏱️ *Horarios de paso por tu localidad:*
                                                • Primer horario: *%s*
                                                • Segundo horario: *%s*
                                                
                                                🚨 *¡ATENCIÓN!:* Para viajar en las próximas unidades solo nos quedan *%d lugares disponibles* en la flota compartida.
                                                
                                                ¿Deseás realizar tu reserva ahora mismo?
                                                """.formatted(selected.getName(), baseFare, primerHorario, segundoHorario, lugaresDisponibles);
                                whatsAppService.sendInteractiveButtons(phoneNumber, "LUNARIS - Cotización", text, List.of(
                                        Map.of("id", "yes_reserve", "title", "Reservar ✅"),
                                        Map.of("id", "no_cancel", "title", "En otro momento ❌")
                                ));
                                return;
                        } catch (Exception e) {
                                whatsAppService.sendMessage(phoneNumber, "⚠️ Por favor, respondé únicamente con el número correlativo de tu localidad.");
                                return;
                        }
                }

                // =====================================================================
                // PASO 3: RESPUESTA GATILLO COMERCIAL
                // =====================================================================
                if ("ASK_MARKETING_CONFIRMATION".equals(session.getCurrentStep())) {
                        if ("yes_reserve".equals(body)) {
                                session.setCurrentStep("SELECT_SCHEDULE");
                                conversationSessionRepository.saveAndFlush(session);

                                String primerHorario = pricingAndScheduleService.calculateEstimatedPickupTime(session.getPickupLocality(), "03:00");
                                String segundoHorario = pricingAndScheduleService.calculateEstimatedPickupTime(session.getPickupLocality(), "08:00");

                                String infoTexto = "⏱️ *Horarios de retiro por tu domicilio:*\n" +
                                                   "• Opción 1: Pasa aprox *" + primerHorario + "*\n" +
                                                   "• Opción 2: Pasa aprox *" + segundoHorario + "*\n\n" +
                                                   "Seleccioná el horario en el que preferís viajar:";

                                whatsAppService.sendInteractiveButtons(phoneNumber, "Selección de Horario", infoTexto, List.of(
                                        Map.of("id", "time_0300", "title", "Primer Horario 🌙"),
                                        Map.of("id", "time_0800", "title", "Segundo Horario ☀️")
                                ));
                                return;
                        }
                        if ("no_cancel".equals(body)) {
                                conversationSessionRepository.delete(session);
                                whatsAppService.sendMessage(phoneNumber, "Entendido. Si cambiás de opinión, escribinos 'Hola' cuando quieras.");
                                return;
                        }
                        return;
                }

                // =====================================================================
                // PASO 4: CAPTURA DEL HORARIO (Mapeado a currentCompanionIndex para seguridad)
                // =====================================================================
                if ("SELECT_SCHEDULE".equals(session.getCurrentStep())) {
                        if ("time_0300".equals(body)) {
                                session.setCurrentCompanionIndex(3); // Codificamos 3:00 AM
                        } else if ("time_0800".equals(body)) {
                                session.setCurrentCompanionIndex(8); // Codificamos 8:00 AM
                        } else {
                                return;
                        }

                        Optional<Passenger> existingPassenger = passengerRepository.findByPhone(phoneNumber);
                        if (existingPassenger.isPresent()) {
                                session.setPassengerName(existingPassenger.get().getFirstName() + " " + existingPassenger.get().getLastName());
                                session.setCurrentStep("ASK_COMPANIONS_COUNT");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "🔢 *¿Cuántos acompañantes viajan con vos?*\n\n_(Si viajás solo, respondé 0)_");
                        } else {
                                session.setCurrentStep("ASK_NAME");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "👤 *Ingresá Nombre y Apellido del pasajero titular.*\n\n_Ejemplo: Juan Pérez_");
                        }
                        return;
                }

                // =====================================================================
                // PASO 5: CAPTURA NOMBRE (Solo clientes nuevos)
                // =====================================================================
                if ("ASK_NAME".equals(session.getCurrentStep())) {
                        session.setPassengerName(message.trim());
                        session.setCurrentStep("ASK_COMPANIONS_COUNT");
                        conversationSessionRepository.saveAndFlush(session);
                        whatsAppService.sendMessage(phoneNumber, "🔢 *¿Cuántos acompañantes viajan con vos?*\n\n_(Si viajás solo, respondé 0)_");
                        return;
                }

                // =====================================================================
                // PASO 5b: CANTIDAD DE ACOMPAÑANTES
                // =====================================================================
                if ("ASK_COMPANIONS_COUNT".equals(session.getCurrentStep())) {
                        try {
                                int count = Integer.parseInt(body);
                                if (count < 0 || count > 3) {
                                        whatsAppService.sendMessage(phoneNumber, "❌ Podés registrar hasta un máximo de 3 acompañantes directos. Ingresá entre 0 y 3:");
                                        return;
                                }

                                if (count == 0) {
                                        session.setPassengerCount(1);
                                        session.setCompanionNames(null);
                                        
                                        // Verificamos si ya tenemos una dirección histórica guardada
                                        Optional<Passenger> passenger = passengerRepository.findByPhone(phoneNumber);
                                        if (passenger.isPresent() && passenger.get().getAddress() != null) {
                                                session.setPickupAddress(passenger.get().getAddress());
                                                session.setCurrentStep("CONFIRM_ADDRESS_BUTTONS");
                                                conversationSessionRepository.saveAndFlush(session);
                                                
                                                whatsAppService.sendInteractiveButtons(phoneNumber, "Dirección de Retiro", 
                                                        "📍 *Detectamos tu domicilio habitual:*\n" + passenger.get().getAddress() + "\n\n¿Pasamos a buscarte por acá?", List.of(
                                                        Map.of("id", "addr_yes", "title", "Sí, pasar por acá ✅"),
                                                        Map.of("id", "addr_no", "title", "Nueva Dirección 🏠")
                                                ));
                                        } else {
                                                session.setCurrentStep("ASK_ADDRESS_TEXT");
                                                conversationSessionRepository.saveAndFlush(session);
                                                whatsAppService.sendMessage(phoneNumber, "🏠 *¿Por qué dirección exacta pasamos a buscarte?*\n\n_Ejemplo: Av. San Martín 450_");
                                        }
                                } else {
                                        session.setTotalCompanions(count);
                                        session.setPassengerCount(1 + count);
                                        session.setCurrentStep("ASK_INDIVIDUAL_COMPANION");
                                        session.setCompanionNames(""); 
                                        // Usamos id temporalmente para llevar la cuenta del bucle (arranca en 1)
                                        session.setId(1L); 
                                        conversationSessionRepository.saveAndFlush(session);
                                        whatsAppService.sendMessage(phoneNumber, "👤 *Ingresá Nombre y Apellido de tu acompañante 1:*");
                                }
                                return;
                        } catch (Exception e) {
                                whatsAppService.sendMessage(phoneNumber, "⚠️ Respondé únicamente con el número digital (Ej: 2).");
                                return;
                        }
                }

                // =====================================================================
                // PASO 5c: BUCLE MULTI-ACOMPAÑANTE
                // =====================================================================
                if ("ASK_INDIVIDUAL_COMPANION".equals(session.getCurrentStep())) {
                        String currentName = message.trim();
                        String accumulated = session.getCompanionNames();
                        accumulated = (accumulated == null || accumulated.isBlank()) ? currentName : accumulated + ", " + currentName;
                        session.setCompanionNames(accumulated);

                        int currentIndex = session.getId().intValue();
                        int nextIndex = currentIndex + 1;

                        if (nextIndex > session.getTotalCompanions()) {
                                // Terminó el bucle, pasamos a validar dirección habitual
                                Optional<Passenger> passenger = passengerRepository.findByPhone(phoneNumber);
                                if (passenger.isPresent() && passenger.get().getAddress() != null) {
                                        session.setPickupAddress(passenger.get().getAddress());
                                        session.setCurrentStep("CONFIRM_ADDRESS_BUTTONS");
                                        conversationSessionRepository.saveAndFlush(session);
                                        
                                        whatsAppService.sendInteractiveButtons(phoneNumber, "Dirección de Retiro", 
                                                "📍 *Detectamos tu domicilio habitual:*\n" + passenger.get().getAddress() + "\n\n¿Pasamos a buscarte por acá?", List.of(
                                                Map.of("id", "addr_yes", "title", "Sí, pasar por acá ✅"),
                                                Map.of("id", "addr_no", "title", "Nueva Dirección 🏠")
                                        ));
                                } else {
                                        session.setCurrentStep("ASK_ADDRESS_TEXT");
                                        conversationSessionRepository.saveAndFlush(session);
                                        whatsAppService.sendMessage(phoneNumber, "🏠 *¿Por qué dirección exacta pasamos a buscarte?*\n\n_Ejemplo: Belgrano 780_");
                                }
                        } else {
                                session.setId((long) nextIndex);
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "👤 *Ingresá Nombre y Apellido de tu acompañante " + nextIndex + ":*");
                        }
                        return;
                }

                // =====================================================================
                // PASO 6a: PROCESAMIENTO BOTONES DIRECCIÓN HABITUAL
                // =====================================================================
                if ("CONFIRM_ADDRESS_BUTTONS".equals(session.getCurrentStep())) {
                        if ("addr_yes".equals(body)) {
                                session.setCurrentStep("ASK_DESTINATION");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendInteractiveButtons(phoneNumber, "Destino en Córdoba", "🎯 *¿Hacia dónde viajás en Córdoba?*", List.of(
                                        Map.of("id", "dest_aeropuerto", "title", "Aeropuerto Cba ✈️"),
                                        Map.of("id", "dest_capital", "title", "Córdoba Capital 🏢")
                                ));
                                return;
                        }
                        if ("addr_no".equals(body)) {
                                session.setCurrentStep("ASK_ADDRESS_TEXT");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "🏠 *Ingresá la nueva dirección exacta para el retiro:*");
                                return;
                        }
                        return;
                }

                // =====================================================================
                // PASO 6b: CAPTURA DIRECCIÓN TEXTO DIRECTO
                // =====================================================================
                if ("ASK_ADDRESS_TEXT".equals(session.getCurrentStep())) {
                        session.setPickupAddress(message);
                        session.setCurrentStep("ASK_DESTINATION");
                        conversationSessionRepository.saveAndFlush(session);

                        whatsAppService.sendInteractiveButtons(phoneNumber, "Destino en Córdoba", "🎯 *¿Hacia dónde viajás en Córdoba?*", List.of(
                                Map.of("id", "dest_aeropuerto", "title", "Aeropuerto Cba ✈️"),
                                Map.of("id", "dest_capital", "title", "Córdoba Capital 🏢")
                        ));
                        return;
                }

                // =====================================================================
                // PASO 7: DESTINO COBERTURA CÓRDOBA
                // =====================================================================
                if ("ASK_DESTINATION".equals(session.getCurrentStep())) {
                        String dest = "dest_aeropuerto".equals(body) ? "Aeropuerto Córdoba" : "dest_capital".equals(body) ? "Córdoba" : null;
                        if (dest == null) return;

                        session.setDestination(dest);
                        session.setCurrentStep("ASK_TRIP_TYPE");
                        conversationSessionRepository.saveAndFlush(session);

                        whatsAppService.sendInteractiveButtons(phoneNumber, "Modalidad", "🔄 *¿Qué tipo de viaje vas a realizar?*", List.of(
                                Map.of("id", "trip_ida", "title", "Solo ida ➡️"),
                                Map.of("id", "trip_completo", "title", "Ida y vuelta 🔄")
                        ));
                        return;
                }

                // =====================================================================
                // PASO 8: COBERTURA TRAMO
                // =====================================================================
                if ("ASK_TRIP_TYPE".equals(session.getCurrentStep())) {
                        if ("trip_ida".equals(body)) {
                                session.setRoundTrip(false);
                                session.setCurrentStep("ASK_DATE");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "📅 *¿Qué día es el viaje de ida?*\n\nEscribilo separado por barras:\n_Ejemplo: 18/06/2026_");
                        } else if ("trip_completo".equals(body)) {
                                session.setRoundTrip(true);
                                session.setCurrentStep("ASK_DATE");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "📅 *Perfecto, ida y vuelta.*\n\n¿Qué día es el viaje de *ida*?\n_Ejemplo: 18/06/2026_");
                        }
                        return;
                }

                // =====================================================================
                // PASO 9: FECHA IDA + MENÚ DE VUELTA FIJA/ABIERTA
                // =====================================================================
                if ("ASK_DATE".equals(session.getCurrentStep())) {
                        try {
                                LocalDate travelDate = LocalDate.parse(message, dateFormatter);
                                if (travelDate.isBefore(LocalDate.now())) {
                                        whatsAppService.sendMessage(phoneNumber, "❌ La fecha no puede ser anterior a hoy. Reingresá:");
                                        return;
                                }
                                session.setTravelDate(travelDate);

                                if (Boolean.TRUE.equals(session.getRoundTrip())) {
                                        session.setCurrentStep("ASK_RETURN_DATE_TYPE");
                                        conversationSessionRepository.saveAndFlush(session);
                                        
                                        whatsAppService.sendInteractiveButtons(phoneNumber, "Fecha de Regreso", 
                                                "📅 *¿Cuándo programamos el regreso desde Córdoba?*\n\nSi todavía no sabés el día exacto, podés dejar la fecha abierta y coordinarla más adelante con Martín.", List.of(
                                                Map.of("id", "return_fixed", "title", "Fijar Fecha 🗓️"),
                                                Map.of("id", "return_open", "title", "Vuelta Abierta 🔄")
                                        ));
                                } else {
                                        session.setCurrentStep("ASK_DNI_REQUIRED");
                                        conversationSessionRepository.saveAndFlush(session);
                                        whatsAppService.sendMessage(phoneNumber, "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números, sin espacios ni guiones):");
                                }
                                return;
                        } catch (Exception e) {
                                whatsAppService.sendMessage(phoneNumber, "❌ *Formato erróneo.* Acordate de usar barras separadoras: 18/06/2026");
                                return;
                        }
                }

                // =====================================================================
                // PASO 9b: SELECCIÓN VUELTA ABIERTA O CERRADA
                // =====================================================================
                if ("ASK_RETURN_DATE_TYPE".equals(session.getCurrentStep())) {
                        if ("return_fixed".equals(body)) {
                                session.setCurrentStep("ASK_RETURN_DATE");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "✍️ *Ingresá la fecha de tu regreso:*\n\n_Ejemplo: 25/06/2026_");
                                return;
                        }
                        if ("return_open".equals(body)) {
                                session.setReturnDate(null); // Seteado como abierto
                                session.setCurrentStep("ASK_DNI_REQUIRED");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números, sin espacios ni guiones):");
                                return;
                        }
                        return;
                }

                // =====================================================================
                // PASO 10: CAPTURA FECHA REGRESO FIJO
                // =====================================================================
                if ("ASK_RETURN_DATE".equals(session.getCurrentStep())) {
                        try {
                                LocalDate returnDate = LocalDate.parse(message, dateFormatter);
                                if (returnDate.isBefore(session.getTravelDate())) {
                                        whatsAppService.sendMessage(phoneNumber, "❌ El regreso no puede ser anterior al viaje de ida.");
                                        return;
                                }
                                session.setReturnDate(returnDate);
                                session.setCurrentStep("ASK_DNI_REQUIRED");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números, sin espacios ni guiones):");
                                return;
                        } catch (Exception e) {
                                whatsAppService.sendMessage(phoneNumber, "❌ *Formato erróneo.* Ingresalo siguiendo el ejemplo: 25/06/2026");
                                return;
                        }
                }

                // =====================================================================
                // PASO 10b: CAPTURA OBLIGATORIA DE DNI/CUIT (Inmune a saltos)
                // =====================================================================
                if ("ASK_DNI_REQUIRED".equals(session.getCurrentStep())) {
                        String cleanDni = body.replaceAll("[^0-9]", "");
                        if (cleanDni.length() < 7 || cleanDni.length() > 11) {
                                whatsAppService.sendMessage(phoneNumber, "❌ *DNI o CUIT inválido.* Verificá el número e ingresalo nuevamente sin guiones:");
                                return;
                        }
                        session.setCuil(cleanDni);
                        session.setCurrentStep("ASK_CONFIRMATION");
                        conversationSessionRepository.saveAndFlush(session);
                        sendReservationSummaryWithButtons(phoneNumber, session);
                        return;
                }

                // =====================================================================
                // PASO 11: CONFIRMACIÓN FINAL Y RECORTE DE OBSERVACIONES
                // =====================================================================
                if ("ASK_CONFIRMATION".equals(session.getCurrentStep())) {
                        if ("confirm_ok".equals(body)) {
                                Passenger passenger = passengerRepository.findByPhone(phoneNumber).orElseGet(() -> {
                                        String[] names = session.getPassengerName().trim().split("\\s+", 2);
                                        return passengerRepository.saveAndFlush(Passenger.builder()
                                                        .firstName(names[0]).lastName(names.length > 1 ? names[1] : "")
                                                        .phone(phoneNumber).address(session.getPickupAddress())
                                                        .locality(session.getPickupLocality()).cuil(session.getCuil()).build());
                                });

                                // Si el pasajero habitual cambió de domicilio, actualizamos su ficha maestra
                                if (!session.getPickupAddress().equalsIgnoreCase(passenger.getAddress())) {
                                        passenger.setAddress(session.getPickupAddress());
                                        passengerRepository.saveAndFlush(passenger);
                                }

                                int totalAsientos = session.getPassengerCount() != null ? session.getPassengerCount() : 1;
                                BigDecimal price = pricingAndScheduleService.calculateTripPrice(session.getPickupLocality(), session.getRoundTrip(), totalAsientos);
                                
                                String baseHour = (session.getCurrentCompanionIndex() != null && session.getCurrentCompanionIndex() == 8) ? "08:00 AM" : "03:00 AM";
                                String notes = baseHour;
                                if (session.getReturnDate() == null && Boolean.TRUE.equals(session.getRoundTrip())) {
                                        notes += " (Abierta)"; // Formato compacto para evitar romper el frontend
                                }
                                if (session.getCompanionNames() != null && !session.getCompanionNames().isBlank()) {
                                        notes += " | Acomp: " + session.getCompanionNames();
                                }

                                reservationRepository.saveAndFlush(Reservation.builder()
                                                .passenger(passenger).travelDate(session.getTravelDate()).returnDate(session.getReturnDate())
                                                .pickupLocality(session.getPickupLocality()).pickupAddress(session.getPickupAddress())
                                                .destination(session.getDestination()).roundTrip(session.getRoundTrip())
                                                .paymentVerified(false).amount(price).status("PENDING").notes(notes).build());

                                conversationSessionRepository.delete(session);

                                whatsAppService.sendMessage(phoneNumber, """
                                                ✅ *¡Tu traslado ha sido registrado con éxito!*

                                                💳 *Datos bancarios para congelar la tarifa (Transferencia):*
                                                • *Titular:* Martín Fernando Manuel Cuestaz
                                                • *Alias:* cuestazm.bna
                                                • *CBU:* 01103739330037363119529

                                                📌 *Nota:* Una vez realizado el envío, *subí la captura o foto del comprobante por acá* para registrar tu pago de forma inmediata. ¡Buen viaje con Lunaris! 🚐
                                                """);
                                return;
                        }

                        if ("confirm_cancel".equals(body)) {
                                session.setCurrentStep("FOLLOW_UP_RETENTION");
                                conversationSessionRepository.saveAndFlush(session);
                                
                                whatsAppService.sendMessage(phoneNumber, """
                                                ❌ *Entendido, pausamos el trámite por acá.*
                                                
                                                Tranqui, si tuviste un cambio de planes con los turnos médicos o el viaje:
                                                ¿Querés que pasemos la ida para el día de mañana en el mismo horario o preferís dejar la consulta en espera?
                                                
                                                _Escribinos si cambiás de idea y lo acomodamos al toque._
                                                """);
                                return;
                        }
                }
        }

        private void sendAllLocalitiesList(String phoneNumber, String saludo) {
                List<Locality> localities = localityRepository.findAll();
                StringBuilder menu = new StringBuilder(saludo).append("\n📍 *¿Desde qué localidad salís?*\n\n");
                int index = 1;
                for (Locality locality : localities) {
                        menu.append("*").append(index).append(")* ").append(locality.getName()).append("\n");
                        index++;
                }
                menu.append("\n_Respondé escribiendo únicamente el número que corresponda a tu pueblo de origen._");
                whatsAppService.sendMessage(phoneNumber, menu.toString());
        }

        private void sendReservationSummaryWithButtons(String phoneNumber, ConversationSession session) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dates = "*Ida:* " + session.getTravelDate().format(formatter);
                
                if (Boolean.TRUE.equals(session.getRoundTrip())) {
                        if (session.getReturnDate() != null) {
                                dates += " | *Vuelta:* " + session.getReturnDate().format(formatter);
                        } else {
                                dates += " | *Vuelta:* 🔄 _ABIERTA_";
                        }
                }

                String blockInfo = (session.getCurrentCompanionIndex() != null && session.getCurrentCompanionIndex() == 8) ? "08:00 AM" : "03:00 AM";
                String estimatedPickupTime = pricingAndScheduleService.calculateEstimatedPickupTime(session.getPickupLocality(), (session.getCurrentCompanionIndex() != null && session.getCurrentCompanionIndex() == 8) ? "08:00" : "03:00");
                
                int totalAsientos = session.getPassengerCount() != null ? session.getPassengerCount() : 1;
                BigDecimal price = pricingAndScheduleService.calculateTripPrice(session.getPickupLocality(), session.getRoundTrip(), totalAsientos);

                String paxLine = session.getPassengerName();
                if (session.getCompanionNames() != null && !session.getCompanionNames().isBlank()) {
                        paxLine += "\n👥 *Acompañantes:* " + session.getCompanionNames();
                }

                String summary = """
                                👤 *Pasajero titular:* %s
                                🔢 *Asientos a ocupar:* %d
                                📍 *Origen:* %s (%s)
                                🎯 *Destino:* %s
                                🕒 *Horario de cabecera:* %s
                                ⏱ *Hora de retiro por tu domicilio:* %s
                                🔄 *Modalidad:* %s
                                📅 %s
                                🧾 *Documento Factura:* %s
                                💰 *Valor Total del Traslado:* $%,.2f
                                """.formatted(paxLine, totalAsientos, session.getPickupLocality(),
                                              session.getPickupAddress(), session.getDestination(),
                                              blockInfo, estimatedPickupTime,
                                              Boolean.TRUE.equals(session.getRoundTrip()) ? "Ida y vuelta" : "Solo ida",
                                              dates, session.getCuil(), price);

                whatsAppService.sendInteractiveButtons(phoneNumber, "Verificación del Itinerario", summary, List.of(
                        Map.of("id", "confirm_ok", "title", "Confirmar 👍"),
                        Map.of("id", "confirm_cancel", "title", "Cancelar ❌")
                ));
        }

        private String normalizeWhatsAppNumber(String phone) {
                return (phone != null && phone.startsWith("549")) ? "54" + phone.substring(3) : phone;
        }
}