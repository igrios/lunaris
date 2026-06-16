package com.lunaris.ansenuza.infrastructure.web.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService; // Ajustalo a tu package de servicio si es necesario
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
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
        private final FareRepository fareRepository; // Agregado para el método de tarifas
        private final PassengerRepository passengerRepository;
        private final ReservationRepository reservationRepository;
        private final LocalReceiptStorageService localReceiptStorageService;
        private final PricingAndScheduleService pricingAndScheduleService;

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
                        List<Map<String, Object>> entry = (List<Map<String, Object>>) payload.get("entry");
                        Map<String, Object> change = (Map<String, Object>) ((List<?>) entry.get(0).get("changes")).get(0);
                        Map<String, Object> value = (Map<String, Object>) change.get("value");
                        List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");

                        if (messages == null || messages.isEmpty()) {
                                return ResponseEntity.ok().build();
                        }

                        Map<String, Object> message = messages.get(0);
                        String from = (String) message.get("from");
                        String type = (String) message.get("type");
                        String destination = normalizeWhatsAppNumber(from);

                        // Procesamiento asincrónico en hilo de fondo para no colgar a Meta
                        CompletableFuture.runAsync(() -> {
                                try {
                                        if ("image".equals(type)) {
                                                Map<String, Object> imageData = (Map<String, Object>) message.get("image");
                                                if (imageData != null) {
                                                        procesarComprobanteDePago(destination, (String) imageData.get("id"));
                                                }
                                                return;
                                        }

                                        String derivedBody = null;
                                        if ("text".equals(type)) {
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

                                        if (derivedBody != null) {
                                                processMessage(destination, derivedBody);
                                        }
                                } catch (Exception ex) {
                                        log.error("Error en el procesamiento asincrónico: ", ex);
                                }
                        });

                        return ResponseEntity.ok().build();

                } catch (Exception e) {
                        log.error("Error crítico recibiendo webhook: ", e);
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

                // 1. PASO: INICIO / MENÚ PRINCIPAL CON BOTÓN DE TARIFAS
                if ("START".equals(session.getCurrentStep()) || isGreeting || ("MAIN_MENU".equals(session.getCurrentStep()) && !("1".equals(body) || "2".equals(body) || "3".equals(body)))) {
                        Optional<Passenger> passenger = passengerRepository.findByPhone(phoneNumber);
                        String header = passenger.isPresent() ? "¡Hola de nuevo, " + passenger.get().getFirstName() + "! 👋" : "¡Bienvenido a Lunaris Ansenuza! 🚐";
                        String text = "Gestioná tus traslados premium puerta a puerta hacia Córdoba seleccionando una opción:";

                        session.setCurrentStep("MAIN_MENU");
                        conversationSessionRepository.saveAndFlush(session);

                        whatsAppService.sendInteractiveButtons(phoneNumber, header, text, List.of(
                                Map.of("id", "1", "title", "Reservar Viaje 🚗"),
                                Map.of("id", "2", "title", "Tarifas Oficiales 💰"),
                                Map.of("id", "3", "title", "Mis Reservas 📋")
                        ));
                        return;
                }

                // 2. PROCESAMIENTO MENÚ PRINCIPAL
                if ("MAIN_MENU".equals(session.getCurrentStep())) {
                        if ("1".equals(body)) {
                                Passenger passenger = passengerRepository.findByPhone(phoneNumber).orElse(null);
                                if (passenger != null) {
                                        session.setPickupLocality(passenger.getLocality());
                                        session.setPickupAddress(passenger.getAddress());
                                        session.setPassengerName(passenger.getFirstName() + " " + passenger.getLastName());
                                        session.setCurrentStep("CONFIRM_SAVED_ADDRESS");
                                        conversationSessionRepository.saveAndFlush(session);

                                        String desc = "📍 *Retiro registrado:*\n" + passenger.getLocality() + " - " + passenger.getAddress();
                                        whatsAppService.sendInteractiveButtons(phoneNumber, "Verificación de Datos", desc + "\n\n¿Pasamos a buscarte por este mismo domicilio?", List.of(
                                                Map.of("id", "1", "title", "Sí, usar datos ✅"),
                                                Map.of("id", "2", "title", "Cambiar origen 📍")
                                        ));
                                        return;
                                }
                                session.setCurrentStep("ASK_LOCALITY");
                                conversationSessionRepository.saveAndFlush(session);
                                sendAllLocalitiesList(phoneNumber);
                                return;
                        }
                        if ("2".equals(body)) {
                                session.setCurrentStep("SHOW_FARES");
                                conversationSessionRepository.saveAndFlush(session);
                                sendFaresInformation(phoneNumber);
                                return;
                        }
                        if ("3".equals(body)) {
                                processConsultation(phoneNumber);
                                return;
                        }
                }

                // MÁQUINA DE ESTADO: PASO DE PANTALLA DE TARIFAS
                if ("SHOW_FARES".equals(session.getCurrentStep())) {
                        if ("back_menu".equals(body)) {
                                session.setCurrentStep("START");
                                conversationSessionRepository.saveAndFlush(session);
                                processMessage(phoneNumber, "menu"); 
                                return;
                        }
                        if ("support_martin".equals(body)) {
                                conversationSessionRepository.delete(session);
                                whatsAppService.sendMessage(phoneNumber, "👤 Martín recibió tu solicitud de asistencia. Se pondrá en contacto a este número a la brevedad. ¡Muchas gracias!");
                                return;
                        }
                        return;
                }

                // 3. PASO: CONFIRMACIÓN DE RECOGIDA HISTÓRICA
                if ("CONFIRM_SAVED_ADDRESS".equals(session.getCurrentStep())) {
                        if ("1".equals(body)) {
                                session.setCurrentStep("ASK_COMPANIONS_COUNT");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "🔢 *¿Cuántos acompañantes viajan con vos?*\n\n_(Si viajás solo, respondé únicamente con el número 0. Máximo: 3 acompañantes)_");
                                return;
                        }
                        if ("2".equals(body)) {
                                session.setPickupLocality(null);
                                session.setPickupAddress(null);
                                session.setCurrentStep("ASK_LOCALITY");
                                conversationSessionRepository.saveAndFlush(session);
                                sendAllLocalitiesList(phoneNumber);
                                return;
                        }
                        return;
                }

                // 4. PASO: PROCESAR NÚMERO DE LOCALIDAD
                if ("ASK_LOCALITY".equals(session.getCurrentStep())) {
                        try {
                                int option = Integer.parseInt(body);
                                List<Locality> localities = localityRepository.findAll();

                                if (option < 1 || option > localities.size()) {
                                        whatsAppService.sendMessage(phoneNumber, "❌ Selección inválida. Por favor, ingresá un número que esté en la lista.");
                                        return;
                                }

                                Locality selected = localities.get(option - 1);
                                session.setPickupLocality(selected.getName());

                                if (session.getPassengerName() != null && !session.getPassengerName().isBlank()) {
                                        session.setCurrentStep("ASK_COMPANIONS_COUNT");
                                        conversationSessionRepository.saveAndFlush(session);
                                        whatsAppService.sendMessage(phoneNumber, "🔢 *¿Cuántos acompañantes viajan con vos?*\n\n_(Si viajás solo, respondé 0)_");
                                } else {
                                        session.setCurrentStep("ASK_NAME");
                                        conversationSessionRepository.saveAndFlush(session);
                                        whatsAppService.sendMessage(phoneNumber, "👤 *Ingresá Nombre y Apellido del pasajero titular.*\n\n_Ejemplo: Juan Pérez_");
                                }
                                return;
                        } catch (Exception e) {
                                whatsAppService.sendMessage(phoneNumber, "⚠️ Debés responder únicamente con el número correlativo de tu localidad.");
                                return;
                        }
                }

                // 5. PASO: NOMBRE TITULAR
                if ("ASK_NAME".equals(session.getCurrentStep())) {
                        session.setPassengerName(message.trim());
                        session.setCurrentStep("ASK_COMPANIONS_COUNT");
                        conversationSessionRepository.saveAndFlush(session);
                        whatsAppService.sendMessage(phoneNumber, "🔢 *¿Cuántos acompañantes viajan con vos?*\n\n_(Si viajás solo, respondé 0)_");
                        return;
                }

                // 6. PASO: CANTIDAD DE ACOMPAÑANTES
                if ("ASK_COMPANIONS_COUNT".equals(session.getCurrentStep())) {
                        try {
                                int count = Integer.parseInt(body);
                                if (count < 0 || count > 3) {
                                        whatsAppService.sendMessage(phoneNumber, "❌ *Cantidad no permitida.*\n\nPodés registrar hasta un máximo de 3 acompañantes directos.\n\nIngresá un número entre 0 y 3:");
                                        return;
                                }

                                if (count == 0) {
                                        session.setPassengerCount(1);
                                        session.setCompanionNames(null);
                                        session.setCurrentStep("ASK_ADDRESS");
                                        conversationSessionRepository.saveAndFlush(session);
                                        whatsAppService.sendMessage(phoneNumber, "🏠 *¿Por qué dirección exacta pasamos a buscarte?*\n\n_Ejemplo: Av. San Martín 450_");
                                } else {
                                        session.setTotalCompanions(count);
                                        session.setPassengerCount(1 + count);
                                        session.setCurrentStep("ASK_INDIVIDUAL_COMPANION");
                                        session.setCurrentCompanionIndex(1);
                                        session.setCompanionNames("");
                                        conversationSessionRepository.saveAndFlush(session);
                                        whatsAppService.sendMessage(phoneNumber, "👤 *Ingresá Nombre y Apellido de tu acompañante 1:*");
                                }
                                return;
                        } catch (Exception e) {
                                whatsAppService.sendMessage(phoneNumber, "⚠️ Respondé únicamente con el número digital (Ej: 2).");
                                return;
                        }
                }

                // 7. PASO MULTI-BUCLE: NOMBRES DE ACOMPAÑANTES
                if ("ASK_INDIVIDUAL_COMPANION".equals(session.getCurrentStep())) {
                        String currentName = message.trim();
                        String accumulated = session.getCompanionNames();
                        accumulated = (accumulated == null || accumulated.isBlank()) ? currentName : accumulated + ", " + currentName;
                        session.setCompanionNames(accumulated);

                        int nextIndex = session.getCurrentCompanionIndex() + 1;
                        if (nextIndex > session.getTotalCompanions()) {
                                session.setCurrentStep("ASK_ADDRESS");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "📍 *Acompañantes registrados con éxito.*\n\nAhora indicanos tu domicilio para el retiro:\n_Ejemplo: Belgrano 780_");
                        } else {
                                session.setCurrentCompanionIndex(nextIndex);
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "👤 *Ingresá Nombre y Apellido de tu acompañante " + nextIndex + ":*");
                        }
                        return;
                }

                // 8. PASO: DIRECCIÓN DE RETIRO
                if ("ASK_ADDRESS".equals(session.getCurrentStep())) {
                        session.setPickupAddress(message);
                        session.setCurrentStep("ASK_DESTINATION");
                        conversationSessionRepository.saveAndFlush(session);

                        whatsAppService.sendInteractiveButtons(phoneNumber, "Selección de Destino", "🎯 *¿Hacia dónde viajás en Córdoba?*", List.of(
                                Map.of("id", "1", "title", "Aeropuerto Cba ✈️"),
                                Map.of("id", "2", "title", "Córdoba Capital 🏢")
                        ));
                        return;
                }

                // 9. PASO: DESTINO FINAL
                if ("ASK_DESTINATION".equals(session.getCurrentStep())) {
                        String dest = "1".equals(body) ? "Aeropuerto Córdoba" : "2".equals(body) ? "Córdoba" : null;
                        if (dest == null) return;

                        session.setDestination(dest);
                        session.setCurrentStep("ASK_TRIP_TYPE");
                        conversationSessionRepository.saveAndFlush(session);

                        whatsAppService.sendInteractiveButtons(phoneNumber, "Configuración del Tramo", "🔄 *¿Qué modalidad de cobertura requerís?*", List.of(
                                Map.of("id", "1", "title", "Solo ida ➡️"),
                                Map.of("id", "2", "title", "Ida y vuelta 🔄")
                        ));
                        return;
                }

                // 10. PASO: MODALIDAD TRAMO
                if ("ASK_TRIP_TYPE".equals(session.getCurrentStep())) {
                        if ("1".equals(body)) session.setRoundTrip(false);
                        else if ("2".equals(body)) session.setRoundTrip(true);
                        else return;

                        session.setCurrentStep("ASK_DATE");
                        conversationSessionRepository.saveAndFlush(session);
                        whatsAppService.sendMessage(phoneNumber, "📅 *¿Qué día es el viaje de ida?*\n\nEscribilo utilizando el formato de barras:\n_Ejemplo: 18/06/2026_");
                        return;
                }

                // 11. PASO: FECHA DE IDA
                if ("ASK_DATE".equals(session.getCurrentStep())) {
                        try {
                                LocalDate travelDate = LocalDate.parse(message, dateFormatter);
                                if (travelDate.isBefore(LocalDate.now())) {
                                        whatsAppService.sendMessage(phoneNumber, "❌ La fecha no puede ser anterior al día de hoy. Por favor, reingresá:");
                                        return;
                                }
                                session.setTravelDate(travelDate);

                                if (Boolean.TRUE.equals(session.getRoundTrip())) {
                                        session.setCurrentStep("ASK_RETURN_DATE");
                                        conversationSessionRepository.saveAndFlush(session);
                                        whatsAppService.sendMessage(phoneNumber, "📅 *¿Qué fecha está programado el regreso?*\n\n_Ejemplo: 25/06/2026_");
                                } else {
                                        session.setCurrentStep("ASK_INVOICE");
                                        conversationSessionRepository.saveAndFlush(session);
                                        sendInvoiceButtons(phoneNumber);
                                }
                                return;
                        } catch (Exception e) {
                                whatsAppService.sendMessage(phoneNumber, "❌ *Formato erróneo.* Acordate de escribirlo separado por barras: 18/06/2026");
                                return;
                        }
                }

                // 12. PASO: FECHA DE REGRESO
                if ("ASK_RETURN_DATE".equals(session.getCurrentStep())) {
                        try {
                                LocalDate returnDate = LocalDate.parse(message, dateFormatter);
                                if (returnDate.isBefore(session.getTravelDate())) {
                                        whatsAppService.sendMessage(phoneNumber, "❌ El retorno no puede ser previo al día de la ida (" + session.getTravelDate().format(dateFormatter) + ").");
                                        return;
                                }
                                session.setReturnDate(returnDate);
                                session.setCurrentStep("ASK_INVOICE");
                                conversationSessionRepository.saveAndFlush(session);
                                sendInvoiceButtons(phoneNumber);
                                return;
                        } catch (Exception e) {
                                whatsAppService.sendMessage(phoneNumber, "❌ *Formato erróneo.* Volvé a ingresar siguiendo el ejemplo: 25/06/2026");
                                return;
                        }
                }

                // 13. PASO: FACTURACIÓN FISCAL
                if ("ASK_INVOICE".equals(session.getCurrentStep())) {
                        if ("1".equals(body)) {
                                session.setRequiresInvoice(true);
                                session.setCurrentStep("ASK_CUIL");
                                conversationSessionRepository.saveAndFlush(session);
                                whatsAppService.sendMessage(phoneNumber, "🧾 *Ingresá tu número de CUIL o CUIT sin guiones ni espacios.*\n\n_Ejemplo: 20123456789_");
                                return;
                        }
                        if ("2".equals(body)) {
                                session.setRequiresInvoice(false);
                                session.setCurrentStep("ASK_CONFIRMATION");
                                conversationSessionRepository.saveAndFlush(session);
                                sendReservationSummaryWithButtons(phoneNumber, session);
                                return;
                        }
                        return;
                }

                // 14. PASO: CAPTURA CUIL/CUIT
                if ("ASK_CUIL".equals(session.getCurrentStep())) {
                        session.setCuil(body);
                        session.setCurrentStep("ASK_CONFIRMATION");
                        conversationSessionRepository.saveAndFlush(session);
                        sendReservationSummaryWithButtons(phoneNumber, session);
                        return;
                }

                // 15. PASO FINAL: CIERRE Y PERSISTENCIA
                if ("ASK_CONFIRMATION".equals(session.getCurrentStep())) {
                        if ("1".equals(body)) {
                                Passenger passenger = passengerRepository.findByPhone(phoneNumber).orElseGet(() -> {
                                        String[] names = session.getPassengerName().trim().split("\\s+", 2);
                                        return passengerRepository.saveAndFlush(Passenger.builder()
                                                        .firstName(names[0]).lastName(names.length > 1 ? names[1] : "")
                                                        .phone(phoneNumber).cuil(session.getCuil())
                                                        .address(session.getPickupAddress()).locality(session.getPickupLocality()).build());
                                });

                                int seatsCount = session.getPassengerCount() != null ? session.getPassengerCount() : 1;
                                BigDecimal finalCalculatedAmount = pricingAndScheduleService.calculateTripPrice(
                                        session.getPickupLocality(), 
                                        Boolean.TRUE.equals(session.getRoundTrip()), 
                                        seatsCount
                                );

                                String notes = "Asientos: " + seatsCount;
                                if (session.getCompanionNames() != null) notes += " | Acompañantes: " + session.getCompanionNames();

                                reservationRepository.saveAndFlush(Reservation.builder()
                                                .passenger(passenger).travelDate(session.getTravelDate()).returnDate(session.getReturnDate())
                                                .pickupLocality(session.getPickupLocality()).pickupAddress(session.getPickupAddress())
                                                .destination(session.getDestination()).roundTrip(session.getRoundTrip())
                                                .paymentVerified(false).amount(finalCalculatedAmount).status("PENDING").notes(notes).build());

                                conversationSessionRepository.delete(session);

                                whatsAppService.sendMessage(phoneNumber, """
                                                ✅ *¡Tu viaje ha sido registrado con éxito!*

                                                💳 *Datos bancarios para congelar la tarifa (Transferencia):*
                                                • *Titular:* Martín Fernando Manuel Cuestaz
                                                • *Alias:* cuestazm.bna
                                                • *CBU:* 01103739330037363119529

                                                📌 *Nota:* Una vez realizado el envío, *subí la captura o foto del comprobante por acá* para registrar tu pago de forma inmediata. Nos comunicaremos con vos para confirmar detalles de horarios. ¡Buen viaje! 🚐
                                                """);
                                return;
                        }

                        if ("2".equals(body)) {
                                conversationSessionRepository.delete(session);
                                whatsAppService.sendMessage(phoneNumber, "❌ *Reserva cancelada.* Si deseás iniciar una nueva consulta operativa, escribí 'Hola' en cualquier momento.");
                                return;
                        }
                }
        }

        // =========================================================================
        // MÉTODOS AUXILIARES DE RENDERIZADO (TODOS DENTRO DE LA CLASE)
        // =========================================================================
        private void sendFaresInformation(String phoneNumber) {
                List<com.lunaris.ansenuza.domain.model.Fare> currentFares = fareRepository.findAll();
                
                StringBuilder sb = new StringBuilder();
                sb.append("📋 *TARIFARIO OFICIAL LUNARIS*\n");
                sb.append("📊 _Valores Base expresados en modalidad Ida y Vuelta:_\n\n");

                for (com.lunaris.ansenuza.domain.model.Fare fare : currentFares) {
                        sb.append("• *").append(fare.getLocalityName()).append(":* $")
                          .append(String.format("%,.2f", fare.getAmount())).append("\n");
                }

                sb.append("\n🔄 *¿Cómo funciona nuestra modalidad?*\n");
                sb.append("1️⃣ El precio de la lista de arriba cubre el trayecto completo (*Ida y Vuelta*).\n");
                sb.append("2️⃣ Si solicitás cobertura para *Solo Ida*, la tarifa se calcula dividiendo el precio base a la mitad (1/2) y sumando un recargo fijo de asiento vacío de *$8.000*.\n\n");
                sb.append("_Todos nuestros viajes son traslados premium puerta a puerta en autos particulares compartidos._");

                whatsAppService.sendInteractiveButtons(phoneNumber, "Información Tarifaria", sb.toString(), List.of(
                        Map.of("id", "back_menu", "title", "Volver al Menú ↩️"),
                        Map.of("id", "support_martin", "title", "Asistencia Humana 👤")
                ));
        }

        private void sendAllLocalitiesList(String phoneNumber) {
                List<Locality> localities = localityRepository.findAll();
                StringBuilder menu = new StringBuilder("📍 *¿Desde dónde viajás?*\n\n");
                int index = 1;
                for (Locality locality : localities) {
                        menu.append("*").append(index).append(")* ").append(locality.getName()).append("\n");
                        index++;
                }
                menu.append("\n_Por favor, respondé escribiendo únicamente el número que corresponda a tu pueblo de origen._");
                whatsAppService.sendMessage(phoneNumber, menu.toString());
        }

        private void sendInvoiceButtons(String phoneNumber) {
                whatsAppService.sendInteractiveButtons(phoneNumber, "Comprobante Fiscal", "🧾 *¿Vas a precisar factura legal oficial para este traslado?*", List.of(
                        Map.of("id", "1", "title", "Sí, necesito"),
                        Map.of("id", "2", "title", "No hace falta")
                ));
        }

        private void processConsultation(String phoneNumber) {
                Passenger passenger = passengerRepository.findByPhone(phoneNumber).orElse(null);
                if (passenger == null) {
                        whatsAppService.sendMessage(phoneNumber, "No registramos pasajes o cuentas vigentes ligadas a tu teléfono.");
                        return;
                }
                List<Reservation> reservations = reservationRepository.findByPassengerOrderByTravelDateAsc(passenger).stream()
                                .filter(r -> !r.getTravelDate().isBefore(LocalDate.now())).collect(Collectors.toList());

                if (reservations.isEmpty()) {
                        whatsAppService.sendMessage(phoneNumber, "No tenés traslados agendados para los próximos días.");
                        return;
                }

                StringBuilder sb = new StringBuilder("📋 *Tus reservas activas en Lunaris:*\n\n");
                int idx = 1;
                for (Reservation r : reservations) {
                        String payment = Boolean.TRUE.equals(r.getPaymentVerified()) ? "✅ Transferencia Verificada" : "⏳ Revisión de pago pendiente";
                        sb.append("*").append(idx++).append(")* ").append(r.getPickupLocality()).append(" ➡️ ").append(r.getDestination())
                          .append("\n🗓️ *Ida:* ").append(r.getTravelDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        if (r.getReturnDate() != null) sb.append(" | *Vuelta:* ").append(r.getReturnDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        sb.append("\n💰 *Valor:* $").append(r.getAmount())
                          .append("\n💳 *Estado:* ").append(payment).append("\n\n");
                }
                whatsAppService.sendMessage(phoneNumber, sb.toString());
        }

        private void sendReservationSummaryWithButtons(String phoneNumber, ConversationSession session) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dates = "*Ida:* " + session.getTravelDate().format(formatter);
                if (Boolean.TRUE.equals(session.getRoundTrip()) && session.getReturnDate() != null) {
                        dates += " | *Vuelta:* " + session.getReturnDate().format(formatter);
                }

                String pax = (session.getCompanionNames() == null || session.getCompanionNames().isBlank())
                                ? session.getPassengerName() + " _(Viaja Solo)_"
                                : session.getPassengerName() + "\n👥 *Acompañantes:* " + session.getCompanionNames();

                String estimatedPickupTime = pricingAndScheduleService.calculatePickupTime(session.getPickupLocality());
                int seatsCount = session.getPassengerCount() != null ? session.getPassengerCount() : 1;
                BigDecimal price = pricingAndScheduleService.calculateTripPrice(
                        session.getPickupLocality(), 
                        Boolean.TRUE.equals(session.getRoundTrip()), 
                        seatsCount
                );

                String summary = """
                                Pasajero(s): %s
                                Asientos Totales: %d
                                Origen: %s (%s)
                                Destino: %s
                                Cobertura: %s
                                %s
                                🕒 Hora de Retiro Estimada: *%s*
                                🧾 Requiere Factura: %s
                                💰 Valor Neto del Traslado: *$%,.2f*
                                """.formatted(pax, seatsCount, session.getPickupLocality(),
                                              session.getPickupAddress(), session.getDestination(),
                                              Boolean.TRUE.equals(session.getRoundTrip()) ? "Ida y vuelta" : "Solo ida",
                                              dates, estimatedPickupTime, Boolean.TRUE.equals(session.getRequiresInvoice()) ? "Sí" : "No", price);

                whatsAppService.sendInteractiveButtons(phoneNumber, "Verificación del Itinerario", summary, List.of(
                        Map.of("id", "1", "title", "Confirmar Todo 👍"),
                        Map.of("id", "2", "title", "Anular Trámite ❌")
                ));
        }

        private String normalizeWhatsAppNumber(String phone) {
                return (phone != null && phone.startsWith("549")) ? "54" + phone.substring(3) : phone;
        }
}