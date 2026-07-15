package com.lunaris.ansenuza.infrastructure.web.controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository; // ✅ Importación de Localidades de la Base de Datos
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
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

    // Inyecciones homologadas para el procesamiento con el Bot y Carga Manual
    private final ReservationRepository reservationRepository;
    private final PassengerRepository passengerRepository;
    private final ChatMessageRepository messageRepository; 
    private final LocalityRepository localityRepository; // ✅ Inyectamos el repositorio para consultar los pueblos de la BD
    private final WhatsAppService whatsAppService;
    private final PricingAndScheduleService tarifaService;
    private final ReceiptStoragePort cloudinaryService;
    private final OperationControlService operationControlService;

    // 🖥️ Muestra la lista de conversaciones en el monitor filtrada por operador logueado
    @GetMapping("/monitor")
    public String getMonitor(Model model, Principal principal) {
        List<ConversationSession> sesiones = sessionRepository.findAll();
        
        String username = (principal != null) ? principal.getName() : "anonimo";
        
        // ⚖️ FILTRO DE TORRE DE CONTROL: Ignacio ve todo, Martín solo lo suyo
        if (!"ignacio".equalsIgnoreCase(username)) {
            log.info("[Monitor] Filtrando chats en tiempo real para el operador: {}", username);
            sesiones = sesiones.stream()
                    .filter(s -> s != null && username.equalsIgnoreCase(s.getAssignedOperator()))
                    .collect(Collectors.toList());
        } else {
            log.info("[Monitor] Administrador 'ignacio' accediendo a la vista global de la Torre de Control.");
        }
        
        model.addAttribute("sesiones", sesiones);
        
        // 🕒 Pasamos el estado del interruptor a la vista HTML de Thymeleaf
        model.addAttribute("jornadaActiva", operationControlService.isHumanActionEnabled());
        
        return "admin/bot-monitor";
    }

    // 🖥️ NUEVO ENDPOINT: Abre el formulario tradicional de nueva reserva alimentado con la base de datos
    @GetMapping("/monitor/nueva-reserva")
    public String mostrarFormularioManual(Model model) {
        // Traemos todos los pueblos registrados en la BD ordenados alfabéticamente para los dropdowns
        List<String> localidades = localityRepository.findAll().stream()
                .map(locality -> locality.getName())
                .sorted()
                .collect(Collectors.toList());
        
        model.addAttribute("localidades", localidades);
        return "reservation-form"; // Abre src/main/resources/templates/reservation-form.html
    }

    // 🌙 Acción POST para encender o apagar la jornada de atención humana
    @PostMapping("/toggle-jornada")
    public String toggleJornada(@RequestParam("enabled") boolean enabled) {
        operationControlService.setHumanActionEnabled(enabled);
        log.info("[Jornada Laboral] Modificada por Administrador. ¿Atención humana activa?: {}", enabled);
        return "redirect:/admin/bot/monitor";
    }

    // 🛑 Acción para pausar o activar el bot de forma dinámica por chat individual
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

    // 💰 COTIZACIÓN EN VIVO ASÍNCRONA PARA EL FORMULARIO
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

    // 🔍 ENDPOINT EXTENDIDO: Busca pasajero por teléfono y retorna todos sus datos y saldo actual
    @GetMapping("/monitor/pasajero/saldo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerSaldoPasajero(@RequestParam("phone") String phone) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            String telefonoClean = phone.trim();
            java.util.Optional<Passenger> passengerOpt = passengerRepository.findByPhone(telefonoClean);
            
            if (passengerOpt.isPresent()) {
                Passenger p = passengerOpt.get();
                respuesta.put("existe", true);
                respuesta.put("firstName", p.getFirstName() != null ? p.getFirstName() : "");
                respuesta.put("lastName", p.getLastName() != null ? p.getLastName() : "");
                respuesta.put("cuil", p.getCuil() != null ? p.getCuil() : "");
                respuesta.put("saldo", p.getCurrentBalance() != null ? p.getCurrentBalance() : java.math.BigDecimal.ZERO);
            } else {
                respuesta.put("existe", false);
                respuesta.put("saldo", java.math.BigDecimal.ZERO);
            }
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("[Buscador Pasajero] Error al consultar para el teléfono {}: ", phone, e);
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    // 🚀 CARGA MANUAL ASISTIDA (Desde la pantalla dividida del chat en vivo)
  // 🚀 CARGA MANUAL ASISTIDA (Desde la pantalla dividida del chat en vivo)
    @PostMapping("/monitor/cargar-reserva")
    public String cargarReservaManualOperador(
            @RequestParam("phone") String phone,
            @RequestParam("firstName") String firstName, 
            @RequestParam("lastName") String lastName,   
            @RequestParam(value = "cuil", required = false) String cuil,
            @RequestParam("pickupLocality") String pickupLocality,
            @RequestParam("destination") String destination,
            @RequestParam("pickupAddress") String pickupAddress,
            @RequestParam("passengerCount") int passengerCount,
            @RequestParam("travelDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate, // ✅ Recibe fecha de vuelta precisa si es programada
            @RequestParam("departureSchedule") String departureSchedule,
            @RequestParam(value = "roundTrip", defaultValue = "false") boolean roundTrip,
            @RequestParam(value = "requiresInvoice", defaultValue = "false") boolean requiresInvoice,
            @RequestParam(value = "chatReceiptUrl", required = false, defaultValue = "null") String chatReceiptUrl,
            RedirectAttributes redirectAttributes) {

        try {
            Passenger passenger = passengerRepository.findByPhone(phone).orElseGet(() -> {
                Passenger newP = new Passenger();
                newP.setPhone(phone);
                newP.setCurrentBalance(java.math.BigDecimal.ZERO);
                return newP;
            });
            
            passenger.setFirstName(firstName.trim());
            passenger.setLastName(lastName.trim() + " (Manual)"); 
            if (cuil != null && !cuil.isBlank()) {
                passenger.setCuil(cuil.trim());
            }
            passengerRepository.save(passenger);

            java.math.BigDecimal montoComboCompleto =
                    tarifaService.calculateReservationAmount(pickupLocality, destination, true, passengerCount);

            java.math.BigDecimal montoIda;
            java.math.BigDecimal montoVuelta;

            if (roundTrip) {
                java.math.BigDecimal mitadCombo = montoComboCompleto.divide(java.math.BigDecimal.valueOf(2), java.math.RoundingMode.HALF_UP);
                montoIda = mitadCombo;
                montoVuelta = mitadCombo;
            } else {
                java.math.BigDecimal mitadCombo = montoComboCompleto.divide(java.math.BigDecimal.valueOf(2), java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal recargoFijo = java.math.BigDecimal.valueOf(8000);
                montoIda = mitadCombo.add(recargoFijo);
                montoVuelta = java.math.BigDecimal.ZERO;
            }

            String urlComprobanteCruda = messageRepository.findByPhoneNumberOrderByTimestampAsc(phone).stream()
                    .filter(m -> m != null && !m.isFromOperator()) 
                    .map(m -> m.getMessageText())
                    .filter(text -> text != null && !text.isBlank())
                    .filter(text -> !text.contains("ahí mando") && !text.contains("ahi mando") && !text.contains("gracias"))
                    .filter(text -> text.startsWith("http://") 
                                 || text.startsWith("https://") 
                                 || text.contains("res.cloudinary.com")
                                 || text.matches(".*[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.*"))
                    .reduce((first, second) -> second) 
                    .orElse("null");

            if (chatReceiptUrl != null && !chatReceiptUrl.isBlank() && chatReceiptUrl.startsWith("http")) {
                urlComprobanteCruda = chatReceiptUrl;
            }

            String urlComprobantePermanente = persistirComprobanteEnCloudinary(urlComprobanteCruda, phone);

            String shortId = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            String codigoBase = pickupLocality.substring(0, 3).toUpperCase() + "-" 
                    + destination.substring(0, 3).toUpperCase() + "-" + shortId;

            String notasAuditoria = urlComprobantePermanente != null
                    ? "Cargado por Operador desde Monitor. Comprobante enlazado y persistido en Cloudinary."
                    : "Cargado por Operador desde Monitor. ⚠️ Comprobante no pudo persistirse.";

            Reservation ida = new Reservation();
            ida.setPassenger(passenger);
            ida.setTravelDate(travelDate);
            ida.setPickupLocality(pickupLocality);
            ida.setPickupAddress(pickupAddress);
            ida.setDestination(destination);
            ida.setPassengerCount(passengerCount);
            ida.setAmount(montoIda); 
            ida.setPaymentReceiptUrl(urlComprobantePermanente); 
            ida.setStatus("PENDING_VERIFICATION");
            ida.setPaymentVerified(false);
            ida.setRoundTrip(roundTrip);
            ida.setDepartureSchedule(departureSchedule);
            ida.setRequiresInvoice(requiresInvoice);
            ida.setReservationCode(codigoBase + "-IDA");
            ida.setNotes(notasAuditoria);

            reservationRepository.saveAndFlush(ida);

            if (roundTrip) {
                Reservation vuelta = new Reservation();
                vuelta.setPassenger(passenger);
                // ✅ Si viene returnDate lo usamos (programada), de lo contrario usa el centinela 2099 (abierta)
                vuelta.setTravelDate(returnDate != null ? returnDate : LocalDate.of(2099, 12, 31));
                vuelta.setPickupLocality(destination);
                vuelta.setDestination(pickupLocality);
                vuelta.setPickupAddress(returnDate != null ? "A coordinar domicilio de vuelta" : "A coordinar por WhatsApp (Vuelta Abierta)");
                vuelta.setPassengerCount(passengerCount);
                vuelta.setAmount(montoVuelta); 
                vuelta.setPaymentReceiptUrl(urlComprobantePermanente); 
                vuelta.setStatus("PENDING_VERIFICATION");
                vuelta.setPaymentVerified(false);
                vuelta.setRoundTrip(true);
                vuelta.setDepartureSchedule(departureSchedule);
                vuelta.setRequiresInvoice(requiresInvoice);
                vuelta.setReturnDate(returnDate != null ? returnDate : LocalDate.of(2099, 12, 31));
                vuelta.setReservationCode(codigoBase + "-VUELTA");
                vuelta.setNotes(notasAuditoria);

                reservationRepository.saveAndFlush(vuelta);
            }

            String textoConfirmacion = "¡Ok, gracias por el comprobante! Verificamos y te aviso. 📝\n\n"
                    + "*Detalles de tu viaje registrado:*\n"
                    + "*Pasajero:* " + firstName + " " + lastName + "\n"
                    + "*Código de Reserva:* " + codigoBase + "\n" 
                    + "*Viaje:* " + pickupLocality + " ➡️ " + destination + "\n" 
                    + "*Fecha:* " + travelDate.toString() + "\n" 
                    + "*Asientos:* " + passengerCount + "\n\n"
                    + "En cuanto validemos la transferencia, el sistema te enviará la confirmación definitiva.";

            whatsAppService.sendMessage(phone, textoConfirmacion);
            redirectAttributes.addFlashAttribute("successMessage", "¡Reserva registrada con éxito!");

        } catch (Exception e) {
            log.error("[Carga Manual] Falló el flujo asistido: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar: " + e.getMessage());
        }

        return "redirect:/admin/chat/" + phone;
    }

    // 🖥️ CARGA MANUAL WEB TRADICIONAL (Desde el formulario web de administración)
    @PostMapping("/monitor/cargar-reserva-web")
    public String cargarReservaWebTradicional(
            @RequestParam("phone") String phone,
            @RequestParam("firstName") String firstName, 
            @RequestParam("lastName") String lastName,   
            @RequestParam(value = "cuil", required = false) String cuil,
            @RequestParam("pickupLocality") String pickupLocality,
            @RequestParam("destination") String destination,
            @RequestParam("pickupAddress") String pickupAddress,
            @RequestParam("passengerCount") int passengerCount,
            @RequestParam("travelDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate, // ✅ Recibe la fecha de regreso desde la web
            @RequestParam("departureSchedule") String departureSchedule,
            @RequestParam(value = "roundTrip", defaultValue = "false") boolean roundTrip,
            @RequestParam(value = "requiresInvoice", defaultValue = "false") boolean requiresInvoice,
            @RequestParam(value = "notes", required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            Passenger passenger = passengerRepository.findByPhone(phone).orElseGet(() -> {
                Passenger newP = new Passenger();
                newP.setPhone(phone);
                newP.setCurrentBalance(java.math.BigDecimal.ZERO);
                return newP;
            });
            
            passenger.setFirstName(firstName.trim());
            passenger.setLastName(lastName.trim()); 
            if (cuil != null && !cuil.isBlank()) {
                passenger.setCuil(cuil.trim());
            }
            passengerRepository.save(passenger);

            java.math.BigDecimal montoComboCompleto =
                    tarifaService.calculateReservationAmount(pickupLocality, destination, true, passengerCount);

            java.math.BigDecimal montoIda;
            java.math.BigDecimal montoVuelta;

            if (roundTrip) {
                java.math.BigDecimal mitadCombo = montoComboCompleto.divide(java.math.BigDecimal.valueOf(2), java.math.RoundingMode.HALF_UP);
                montoIda = mitadCombo;
                montoVuelta = mitadCombo;
            } else {
                java.math.BigDecimal mitadCombo = montoComboCompleto.divide(java.math.BigDecimal.valueOf(2), java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal recargoFijo = java.math.BigDecimal.valueOf(8000);
                montoIda = mitadCombo.add(recargoFijo);
                montoVuelta = java.math.BigDecimal.ZERO;
            }

            String shortId = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            String codigoBase = pickupLocality.substring(0, 3).toUpperCase() + "-" 
                    + destination.substring(0, 3).toUpperCase() + "-" + shortId;

            Reservation ida = new Reservation();
            ida.setPassenger(passenger);
            ida.setTravelDate(travelDate);
            ida.setPickupLocality(pickupLocality);
            ida.setPickupAddress(pickupAddress);
            ida.setDestination(destination);
            ida.setPassengerCount(passengerCount);
            ida.setAmount(montoIda); 
            ida.setStatus("CONFIRMED"); // Confirmado directo por oficina
            ida.setPaymentVerified(true);
            ida.setRoundTrip(roundTrip);
            ida.setDepartureSchedule(departureSchedule);
            ida.setRequiresInvoice(requiresInvoice);
            ida.setReservationCode(codigoBase + "-IDA");
            ida.setNotes(notes != null ? notes : "Cargado manualmente desde la administración web.");

            reservationRepository.saveAndFlush(ida);

            if (roundTrip) {
                Reservation vuelta = new Reservation();
                vuelta.setPassenger(passenger);
                // Si viene una fecha de regreso del formulario la aplicamos, sino default al centinela (2099)
                vuelta.setTravelDate(returnDate != null ? returnDate : LocalDate.of(2099, 12, 31));
                vuelta.setPickupLocality(destination);
                vuelta.setDestination(pickupLocality);
                vuelta.setPickupAddress(returnDate != null ? "A coordinar domicilio de vuelta" : "A coordinar por WhatsApp (Vuelta Abierta)");
                vuelta.setPassengerCount(passengerCount);
                vuelta.setAmount(montoVuelta); 
                vuelta.setStatus("CONFIRMED");
                vuelta.setPaymentVerified(true);
                vuelta.setRoundTrip(true);
                vuelta.setDepartureSchedule(departureSchedule);
                vuelta.setRequiresInvoice(requiresInvoice);
                vuelta.setReturnDate(returnDate != null ? returnDate : LocalDate.of(2099, 12, 31));
                vuelta.setReservationCode(codigoBase + "-VUELTA");
                vuelta.setNotes(notes != null ? notes : "Cargado manualmente desde la administración web.");

                reservationRepository.saveAndFlush(vuelta);
            }

            redirectAttributes.addFlashAttribute("successMessage", "¡Reserva manual creada correctamente!");

        } catch (Exception e) {
            log.error("[Carga Web] Error al procesar reserva manual: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar: " + e.getMessage());
        }

        return "redirect:/agenda?success=true";
    }

    private String persistirComprobanteEnCloudinary(String urlOrigen, String phone) {
        if (urlOrigen == null || urlOrigen.isBlank() || "null".equalsIgnoreCase(urlOrigen)) {
            return null;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(urlOrigen))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            java.net.http.HttpResponse<byte[]> response =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200 || response.body() == null || response.body().length == 0) {
                log.warn("[Comprobante Cloudinary] Descarga falló con status {} para el teléfono {}", response.statusCode(), phone);
                return null;
            }

            String nombreArchivo = "comprobante-" + phone + "-" + System.currentTimeMillis();

            final byte[] contenido = response.body();
            org.springframework.web.multipart.MultipartFile multipartFile = new org.springframework.web.multipart.MultipartFile() {
                @Override
                public String getName() { return nombreArchivo; }
                @Override
                public String getOriginalFilename() { return nombreArchivo + ".jpg"; }
                @Override
                public String getContentType() { return "image/jpeg"; }
                @Override
                public boolean isEmpty() { return contenido.length == 0; }
                @Override
                public long getSize() { return contenido.length; }
                @Override
                public byte[] getBytes() throws java.io.IOException { return contenido; }
                @Override
                public java.io.InputStream getInputStream() throws java.io.IOException { 
                    return new java.io.ByteArrayInputStream(contenido); 
                }
                @Override
                public void transferTo(java.io.File dest) throws java.io.IOException, IllegalStateException {
                    java.nio.file.Files.write(dest.toPath(), contenido);
                }
            };

            return cloudinaryService.uploadFile(multipartFile);

        } catch (Exception e) {
            log.error("[Comprobante Cloudinary] Error crítico al procesar la subida para el teléfono {}: ", phone, e);
            return null;
        }
    }
}