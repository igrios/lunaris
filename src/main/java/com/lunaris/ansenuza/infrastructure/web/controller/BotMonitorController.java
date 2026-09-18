package com.lunaris.ansenuza.infrastructure.web.controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
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
    private final PassengerRepository passengerRepository;
    private final ChatMessageRepository messageRepository; 
    private final LocalityRepository localityRepository; 
    private final WhatsAppService whatsAppService;
    private final PricingAndScheduleService tarifaService;
    private final ReceiptStoragePort cloudinaryService;
    private final OperationControlService operationControlService;
    private final ReservationService reservationService;

    private final com.lunaris.ansenuza.application.usecase.BotMonitorService botMonitorService;
    private final com.lunaris.ansenuza.application.usecase.CreateManualReservationUseCase createManualReservation;

    @GetMapping("/monitor")
    public String getMonitor(Model model, Principal principal) {
        model.addAttribute("sesiones", botMonitorService.rows());
        model.addAttribute("jornadaActiva", operationControlService.isHumanActionEnabled());
        return "admin/bot-monitor";
    }

    @GetMapping("/monitor/rows")
    public String monitorRows(Model model) {
        model.addAttribute("sesiones", botMonitorService.rows());
        return "admin/bot-monitor :: monitorRows";
    }

    @PostMapping("/monitor/pause")
    @ResponseBody
    public ResponseEntity<Void> pause(@RequestParam long id, @RequestParam boolean paused) {
        botMonitorService.setPaused(id, paused);
        return ResponseEntity.noContent().build();
    }

    // 🖥️ Abre el formulario tradicional de nueva reserva
    @GetMapping("/monitor/nueva-reserva")
    public String mostrarFormularioManual(Model model) {
        // Mantenemos soporte Thymeleaf tradicional por si acaso
        List<String> localidades = localityRepository.findAllWithActiveFare().stream()
                .map(locality -> locality.getName())
                .collect(Collectors.toList());
        
        model.addAttribute("localidades", localidades);
        return "reservation-form"; 
    }

    // 📡 NUEVO ENDPOINT API: Retorna todas las localidades en JSON para que las consuman vía JS en caliente
    @GetMapping("/monitor/localidades")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerLocalidades() {
      try {
            // 🚀 Usamos el método que filtra solo pueblos con tarifas y los ordena de la A a la Z
            List<String> localidades = localityRepository.findAllWithActiveFare().stream()
                    .map(locality -> locality.getName())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(localidades);
        } catch (Exception e) {
            log.error("[API Localidades] Error al consultar de la BD: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        session.setManuallyPaused(!currentState);

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
            @RequestParam("passengerCount") int asientos,
            @RequestParam(value = "roundTrip", defaultValue = "true") boolean roundTrip) {

        Map<String, Object> respuesta = new HashMap<>();
        try {
            java.math.BigDecimal montoTramo =
                    tarifaService.calculateReservationAmount(origen, destino, roundTrip, asientos);

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
            @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate, 
            @RequestParam("departureSchedule") String departureSchedule,
            @RequestParam(value = "roundTrip", defaultValue = "false") boolean roundTrip,
            @RequestParam(value = "requiresInvoice", defaultValue = "false") boolean requiresInvoice,
            @RequestParam(value = "chatReceiptUrl", required = false, defaultValue = "null") String chatReceiptUrl,
            @org.springframework.web.bind.annotation.ModelAttribute
            com.lunaris.ansenuza.infrastructure.web.dto.reservation.ManualReservationOptions options,
            RedirectAttributes redirectAttributes) {

        try {
            Passenger passenger = Passenger.builder().firstName(firstName).lastName(lastName)
                    .phone(phone).cuil(cuil).build();

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
            ida.setAmount(options.getAmount());
            ida.setExtraAmount(options.getExtraAmount());
            ida.setCompanionNames(options.getCompanionNames());
            ida.setRouteDirection(options.getRouteDirection());
            ida.setDiscountAmount(options.getDiscountAmount());
            ida.setPaymentReceiptUrl(urlComprobantePermanente); 
            ida.setStatus("PENDING_VERIFICATION");
            ida.setPaymentVerified(false);
            ida.setRoundTrip(roundTrip);
            ida.setTripType(tripType(roundTrip, returnDate));
            ida.setReturnDate(returnDate);
            ida.setDepartureSchedule(departureSchedule);
            ida.setRequiresInvoice(requiresInvoice);
            ida.setNotes(options.getNotes() == null || options.getNotes().isBlank() ? notasAuditoria : options.getNotes());

            createManualReservation.execute(ida, options.getReturnDepartureSchedule());

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
            @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate, 
            @RequestParam("departureSchedule") String departureSchedule,
            @RequestParam(value = "returnDepartureSchedule", required = false) String returnDepartureSchedule,
            @RequestParam(value = "roundTrip", defaultValue = "false") boolean roundTrip,
            @RequestParam(value = "requiresInvoice", defaultValue = "false") boolean requiresInvoice,
            @RequestParam(value = "notes", required = false) String notes,
            @org.springframework.web.bind.annotation.ModelAttribute
            com.lunaris.ansenuza.infrastructure.web.dto.reservation.ManualReservationOptions options,
            RedirectAttributes redirectAttributes) {

        try {
            Passenger passenger = Passenger.builder().firstName(firstName).lastName(lastName)
                    .phone(phone).cuil(cuil).build();

            Reservation ida = new Reservation();
            ida.setPassenger(passenger);
            ida.setTravelDate(travelDate);
            ida.setPickupLocality(pickupLocality);
            ida.setPickupAddress(pickupAddress);
            ida.setDestination(destination);
            ida.setPassengerCount(passengerCount);
            ida.setAmount(options.getAmount());
            ida.setExtraAmount(options.getExtraAmount());
            ida.setCompanionNames(options.getCompanionNames());
            ida.setRouteDirection(options.getRouteDirection());
            ida.setDiscountAmount(options.getDiscountAmount());
            ida.setStatus("CONFIRMED"); 
            ida.setPaymentVerified(false);
            ida.setRoundTrip(roundTrip);
            ida.setTripType(tripType(roundTrip, returnDate));
            ida.setReturnDate(returnDate);
            ida.setDepartureSchedule(departureSchedule);
            ida.setRequiresInvoice(requiresInvoice);
            ida.setNotes(notes != null ? notes : "Cargado manualmente desde la administración web.");

            createManualReservation.execute(ida, returnDepartureSchedule);

            redirectAttributes.addFlashAttribute("successMessage", "¡Reserva manual creada correctamente!");

        } catch (Exception e) {
            log.error("[Carga Web] Error al procesar reserva manual: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar: " + e.getMessage());
        }

        return "redirect:/agenda?success=true";
    }

    private TripType tripType(boolean roundTrip, LocalDate returnDate) {
        if (!roundTrip) return TripType.ONE_WAY;
        return returnDate == null ? TripType.OPEN_RETURN : TripType.ROUND_TRIP;
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
