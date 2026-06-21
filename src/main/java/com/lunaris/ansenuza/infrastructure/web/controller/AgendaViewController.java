package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.agenda.AgendaDayView;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AgendaViewController {

    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;
    private final DriverRepository driverRepository;

    @Value("${whatsapp.api.token:EAAOpuc7IAZCYBRr2RWtWMKLtUU2sMYy0HEo2GxFiUPX2Uj70TOMysoptwJ6HQ7DJjT0eaQcarX8UC824cYb2rXwbdPaTZBT3sB5DLVyRiBD1Ihc2wznb1DukhjGZAFR5kG72ZCWi2YbBKMGVTXSz1cUuPBcfDYE61Eq9XgBK5wAZBQ6ZAue5g9iwstZAsyP9jMhwE89dzsP0TYzOPmZCgnt8n8W49rrt8m6Yo0fmLVjw0l5ZAf7gHeoY9UbUCMOtOYR6ggJD7yZC9cuNfbar7RHLASzAZDZD}")
    private String whatsappToken;

    // 📅 1. Vista resumen de los próximos 5 días CORREGIDA CON EL CONTEO NATIVO DE ASIENTOS
    // 📅 1. Vista resumen de los próximos 7 días (Semana Completa) BLINDADA CONTRA VUELTAS ABIERTAS
    @GetMapping("/agenda")
    public String agenda(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate fechaCentinela = LocalDate.of(2099, 12, 31);

        // 🌟 Modificado: Pasamos de 5 a 7 en el IntStream.range para cubrir la semana completa
        List<AgendaDayView> agenda =
                java.util.stream.IntStream.range(0, 7).mapToObj(today::plusDays).map(date -> {
                    List<Reservation> reservations = reservationRepository.findByTravelDate(date);

                    // 🌟 FILTRO: Excluimos cualquier registro que por error tenga la fecha centinela
                    int totalPassengers = reservations.stream()
                            .filter(r -> r.getTravelDate() == null || !r.getTravelDate().equals(fechaCentinela))
                            .mapToInt(r -> r.getPassengerCount() != null ? r.getPassengerCount() : 1)
                            .sum();

                    int pendingPayments = (int) reservations.stream()
                            .filter(r -> r.getTravelDate() == null || !r.getTravelDate().equals(fechaCentinela))
                            .filter(r -> !Boolean.TRUE.equals(r.getPaymentVerified())).count();

                    int estimatedVehicles = totalPassengers == 0 ? 0 : (int) Math.ceil(totalPassengers / 4.0);

                    return new AgendaDayView(date, totalPassengers, pendingPayments, estimatedVehicles);
                }).toList();

        model.addAttribute("agenda", agenda);
        return "agenda";
    }

    
    // 🚐 2. Vista detalle del día
    @GetMapping("/agenda/view-detalle")
    public String dayAgenda(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<Reservation> reservations = reservationRepository.findByTravelDate(date);
        List<Driver> choferes = driverRepository.findByActiveTrue();

        model.addAttribute("date", date);
        model.addAttribute("reservations", reservations);
        model.addAttribute("choferes", choferes);

        return "agenda-day";
    }

    // 💳 3. Confirmación asíncrona de pago
    @PostMapping("/api/agenda/confirmar-pago/{id}")
    @ResponseBody
    public ResponseEntity<Void> verifyPayment(@PathVariable UUID id) {
        return reservationRepository.findById(id).map(reservation -> {
            reservation.setPaymentVerified(true);
            reservation.setStatus("CONFIRMED");
            reservationRepository.saveAndFlush(reservation);

            try {
                String clienteCelular = reservation.getPassenger().getPhone();
                String nombrePasajero = reservation.getPassenger().getFirstName();

                String mensajeWhatsApp =
                        """
                                ✅ *¡Pago Verificado con Éxito!*

                                Hola %s, te confirmamos que recibimos correctamente tu transferencia. Tu reserva para el traslado hacia *%s* ya se encuentra asentada de forma definitiva.

                                🚐 Próximamente nos comunicaremos para coordinar el horario exacto en el que el chofer pasará por tu domicilio. ¡Muchas gracias por viajar con Lunaris!
                                """
                                .formatted(nombrePasajero, reservation.getDestination());

                whatsAppService.sendMessage(clienteCelular, mensajeWhatsApp);

            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(getClass())
                        .error("No se pudo enviar el WhatsApp de confirmación de pago", e);
            }

            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // 📄 4. Descarga del comprobante
    @GetMapping("/api/agenda/comprobante-descarga/{id}")
    public ResponseEntity<byte[]> getReceiptImage(@PathVariable UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        String receiptUrl = reservation.getPaymentReceiptUrl();
        if (receiptUrl == null || !receiptUrl.contains("v20.0/")) {
            return ResponseEntity.notFound().build();
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> mediaResponse =
                    restTemplate.exchange(receiptUrl, HttpMethod.GET, entity, JsonNode.class);

            String actualDownloadUrl = mediaResponse.getBody().get("url").asText();

            ResponseEntity<byte[]> imageResponse =
                    restTemplate.exchange(actualDownloadUrl, HttpMethod.GET, entity, byte[].class);

            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
                    .body(imageResponse.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // 🚖 5. Envío de Hoja de Ruta AL CHOFER CORREGIDO CON DESGLOSE DE LUGARES OCUPADOS
    @PostMapping("/api/agenda/enviar-hoja-ruta")
    public ResponseEntity<Void> enviarHojaRuta(@RequestParam("phone") String choferPhone,
            @RequestBody List<UUID> reservationIds) {

        if (reservationIds == null || reservationIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("📋 *HOJA DE RUTA - TRASLADOS LUNARIS*\n");
        mensaje.append("--------------------------------------------------\n\n");

        int index = 1;
        for (UUID id : reservationIds) {
            Reservation res = reservationRepository.findById(id).orElse(null);
            if (res == null)
                continue;

            String nombre = res.getPassenger().getFirstName() + " " + res.getPassenger().getLastName();
            String origen = res.getPickupLocality();
            String destino = res.getDestination();
            String direccion = (res.getPickupAddress() != null && !res.getPickupAddress().isEmpty())
                    ? res.getPickupAddress()
                    : "No especificada";
            String telefono = res.getPassenger().getPhone();
            String observaciones = (res.getNotes() != null && !res.getNotes().isEmpty()) ? res.getNotes() : "-";

            int asientos = res.getPassengerCount() != null ? res.getPassengerCount() : 1;
            String listaAcompanantes = (res.getCompanionNames() != null && !res.getCompanionNames().isEmpty())
                            ? res.getCompanionNames()
                            : "Ninguno";

            mensaje.append(String.format("🚐 *VIAJE #%d*\n", index));
            mensaje.append(String.format("👤 *Pasajero Titular:* %s\n", nombre));
            mensaje.append(String.format("🔢 *Asientos a Ocupar:* %d\n", asientos));
            mensaje.append(String.format("👥 *Acompañantes:* %s\n", listaAcompanantes));
            mensaje.append(String.format("📍 *Origen:* %s\n", origen));
            mensaje.append(String.format("🏁 *Destino:* %s\n", destino));
            mensaje.append(String.format("🏠 *Dirección:* %s\n", direccion));
            mensaje.append(String.format("📞 *Tel:* %s\n", telefono));
            mensaje.append(String.format("📝 *Obs:* %s\n", observaciones));
            mensaje.append("--------------------------------------------------\n\n");
            index++;
        }

        mensaje.append("_¡Buen viaje! Por cualquier duda comunicarse con la base._");

        try {
            whatsAppService.sendMessage(choferPhone, mensaje.toString());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .error("Error al enviar la hoja de ruta al chofer por webhook/API", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 💬 6. Habilita http://localhost:8080/chat-room apuntando adentro de admin/
    @GetMapping("/chat-room")
    public String showChatRoom(Model model) {
        model.addAttribute("historial", new java.util.ArrayList<>());
        
        // El salvavidas para que el HTML no explote exigiendo el th:object
        model.addAttribute("reservation", new com.lunaris.ansenuza.domain.model.Reservation());
        
        // Listas para que se llenen los desplegables de localidad
        model.addAttribute("origenes", List.of("Morteros", "Brinkmann", "San Guillermo", "Porteña", "Suardi")); 
        model.addAttribute("destinos", List.of("Córdoba", "Aeropuerto Córdoba"));

        return "admin/chat-room"; // 👈 Corregido: va a buscar a templates/admin/chat-room.html
    }

    // 🤖 7. Habilita http://localhost:8080/bot-monitor apuntando adentro de admin/
    @GetMapping("/bot-monitor")
    public String showBotMonitor(Model model) {
        model.addAttribute("logs", new java.util.ArrayList<>());
        return "admin/bot-monitor"; // 👈 Corregido: va a buscar a templates/admin/bot-monitor.html
    }

    // 📋 8. Habilita http://localhost:8080/hoja-ruta apuntando adentro de admin/
    @GetMapping("/hoja-ruta")
    public String showHojaRuta(Model model) {
        model.addAttribute("date", LocalDate.now());
        return "admin/hoja-ruta"; // 👈 Corregido: va a buscar a templates/admin/hoja-ruta.html
    }

    public record Chofer(String nombre, String telefono) {
    }
}