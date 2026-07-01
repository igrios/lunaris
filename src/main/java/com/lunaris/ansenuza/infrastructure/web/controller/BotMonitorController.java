package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/bot")
@AllArgsConstructor
@Slf4j
public class BotMonitorController {

    private final ConversationSessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Inyecciones para el procesamiento homologado con el Bot
    private final ReservationRepository reservationRepository;
    private final PassengerRepository passengerRepository;
    private final ChatMessageRepository messageRepository; // Inyectado para buscar el comprobante
    private final WhatsAppService whatsAppService;
    private final PricingAndScheduleService tarifaService;
    private final ReceiptStoragePort cloudinaryService;

    // 🖥️ Muestra la lista de conversaciones
    @GetMapping("/monitor")
    public String getMonitor(Model model) {
        List<ConversationSession> sesiones = sessionRepository.findAll();
        model.addAttribute("sesiones", sesiones);
        return "admin/bot-monitor";
    }

    // 🛑 Acción para mutear/pausar o despausar el bot
    @PostMapping("/toggle-bot")
    public String toggleBot(@RequestParam("id") Long sessionId) {
        ConversationSession session = sessionRepository.findById(sessionId).orElseThrow(
                () -> new IllegalArgumentException("Sesión no encontrada con ID: " + sessionId));

        boolean currentState = session.isBotPaused();
        session.setBotPaused(!currentState);

        sessionRepository.saveAndFlush(session);

        messagingTemplate.convertAndSend("/topic/system-alerts",
                Map.of("action", "TOGGLE_BOT", "sessionId", sessionId, "isPaused", !currentState));

        return "redirect:/admin/bot/monitor";
    }

    // 💰 COTIZACIÓN ASÍNCRONA EN TIEMPO REAL
    @GetMapping("/monitor/cotizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cotizarFilaManual(
            @RequestParam("pickupLocality") String origen,
            @RequestParam("destination") String destino,
            @RequestParam("passengerCount") int asientos) {

        Map<String, Object> respuesta = new HashMap<>();
        try {
            java.math.BigDecimal montoTramo =
                    tarifaService.calculateReservationAmount(origen, destino, Boolean.TRUE, asientos);

            String prefijoCodigo = "---";
            if (origen.length() >= 3 && destino.length() >= 3) {
                prefijoCodigo = origen.substring(0, 3).toUpperCase() + "-"
                        + destino.substring(0, 3).toUpperCase();
            }

            respuesta.put("monto", montoTramo);
            respuesta.put("codigo", prefijoCodigo + "-XXXXX");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("monto", java.math.BigDecimal.ZERO);
            respuesta.put("codigo", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    // 🚀 CARGA MANUAL INTELIGENTE: Vincula datos tipeados y recupera la foto real del chat
    @PostMapping("/monitor/cargar-reserva")
    public String cargarReservaManualOperador(
            @RequestParam("phone") String phone,
            @RequestParam("firstName") String firstName, 
            @RequestParam("lastName") String lastName,   
            @RequestParam("pickupLocality") String pickupLocality,
            @RequestParam("destination") String destination,
            @RequestParam("pickupAddress") String pickupAddress,
            @RequestParam("passengerCount") int passengerCount,
            @RequestParam("travelDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(value = "isRoundTrip", defaultValue = "false") boolean isRoundTrip,
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Resolver o registrar Pasajero usando nombre real ingresado por Martín
            Passenger passenger = passengerRepository.findByPhone(phone).orElseGet(() -> {
                Passenger newP = new Passenger();
                newP.setPhone(phone);
                return newP;
            });
            
            passenger.setFirstName(firstName.trim());
            passenger.setLastName(lastName.trim() + " (Manual)"); // Distinción visual en la lista de pasajeros
            passengerRepository.save(passenger);

  
      // 2. 📸 RECUPERACIÓN AUTOMÁTICA DEL COMPROBANTE REAL DEL CHAT
            String urlComprobante = messageRepository.findByPhoneNumberOrderByTimestampAsc(phone).stream()
                    .filter(m -> m != null && !m.isFromOperator()) // 👈 Filtramos que el objeto 'm' no sea null primero
                    .map(m -> m.getMessageText()) // 👈 Cambiamos la referencia por lambda para evitar el warning del IDE
                    .filter(text -> text != null && (text.contains("http://") || text.contains("https://") || text.contains("res.cloudinary.com")))
                    .reduce((first, second) -> second)
                    .orElse("null");

            String shortId = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            String codigoBase = pickupLocality.substring(0, 3).toUpperCase() + "-" 
                    + destination.substring(0, 3).toUpperCase() + "-" + shortId;
                    
            java.math.BigDecimal montoTramo =
                    tarifaService.calculateReservationAmount(pickupLocality, destination, isRoundTrip, passengerCount);

            // 3. Persistir Ida como PENDING_VERIFICATION vinculando la foto del chat
            Reservation ida = new Reservation();
            ida.setPassenger(passenger);
            ida.setTravelDate(travelDate);
            ida.setPickupLocality(pickupLocality);
            ida.setPickupAddress(pickupAddress);
            ida.setDestination(destination);
            ida.setPassengerCount(passengerCount);
            ida.setAmount(montoTramo);
            ida.setPaymentReceiptUrl(urlComprobante); // Guardado seguro
            ida.setStatus("PENDING_VERIFICATION");
            ida.setPaymentVerified(false);
            ida.setRoundTrip(isRoundTrip);
            ida.setReservationCode(codigoBase + "-IDA");
            ida.setNotes("Cargado automáticamente por Operador desde Monitor. Comprobante enlazado.");

            reservationRepository.saveAndFlush(ida);

            // 4. Si es combo (Ida y Vuelta), abrir tramo de Vuelta Centinela 2099
            if (isRoundTrip) {
                Reservation vuelta = new Reservation();
                vuelta.setPassenger(passenger);
                vuelta.setTravelDate(LocalDate.of(2099, 12, 31));
                vuelta.setPickupLocality(destination);
                vuelta.setDestination(pickupLocality);
                vuelta.setPickupAddress("A coordinar por WhatsApp (Vuelta Abierta)");
                vuelta.setPassengerCount(passengerCount);
                vuelta.setAmount(montoTramo);
                vuelta.setPaymentReceiptUrl(urlComprobante); // Enlazado también en la vuelta
                vuelta.setStatus("PENDING_VERIFICATION");
                vuelta.setPaymentVerified(false);
                vuelta.setRoundTrip(true);
                vuelta.setReturnDate(LocalDate.of(2099, 12, 31));
                vuelta.setReservationCode(codigoBase + "-VUELTA");
                vuelta.setNotes("Vuelta abierta inicializada automáticamente por Operador.");

                reservationRepository.saveAndFlush(vuelta);
            }

            // 5. RESPUESTA AL PASAJERO: Mensaje cordial confirmando la recepción del comprobante
            String textoConfirmacion = "¡Ok, gracias por el comprobante! Verificamos y te aviso. 📝\n\n"
                    + "*Detalles de tu viaje registrado:*\n"
                    + "*Pasajero:* " + firstName + " " + lastName + "\n"
                    + "*Código de Reserva:* " + codigoBase + "\n" 
                    + "*Viaje:* " + pickupLocality + " ➡️ " + destination + "\n" 
                    + "*Fecha:* " + travelDate.toString() + "\n" 
                    + "*Asientos:* " + passengerCount + "\n\n"
                    + "En cuanto validemos la transferencia en el homebanking, el sistema te enviará la confirmación definitiva.";

            whatsAppService.sendMessage(phone, textoConfirmacion);

            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Reserva registrada con éxito! El comprobante del chat fue enlazado.");

        } catch (Exception e) {
            log.error("[Carga Manual] Falló el flujo asistido: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al procesar carga: " + e.getMessage());
        }

        // Retorna a la sala de chat activa con el cliente
        return "redirect:/admin/chat/" + phone;
    }
}