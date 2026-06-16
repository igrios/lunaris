package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.agenda.AgendaDayView;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.domain.model.Driver;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AgendaViewController {

    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;
    private final DriverRepository driverRepository;

    // Levantamos el token seguro desde tu application.properties
    @Value("${whatsapp.api.token:EAAOpuc7IAZCYBRr2RWtWMKLtUU2sMYy0HEo2GxFiUPX2Uj70TOMysoptwJ6HQ7DJjT0eaQcarX8UC824cYb2rXwbdPaTZBT3sB5DLVyRiBD1Ihc2wznb1DukhjGZAFR5kG72ZCWi2YbBKMGVTXSz1cUuPBcfDYE61Eq9XgBK5wAZBQ6ZAue5g9iwstZAsyP9jMhwE89dzsP0TYzOPmZCgnt8n8W49rrt8m6Yo0fmLVjw0l5ZAf7gHeoY9UbUCMOtOYR6ggJD7yZC9cuNfbar7RHLASzAZDZD}")
    private String whatsappToken;

    // 📅 1. Vista resumen de los próximos 5 días (localhost:8080/agenda)
    @GetMapping("/agenda")
    public String agenda(Model model) {
        LocalDate today = LocalDate.now();

        List<AgendaDayView> agenda = java.util.stream.IntStream.range(0, 5)
                .mapToObj(today::plusDays)
                .map(date -> {
                    List<Reservation> reservations = reservationRepository.findByTravelDate(date);
                    int totalPassengers = reservations.size();
                    int pendingPayments = (int) reservations.stream()
                            .filter(r -> !Boolean.TRUE.equals(r.getPaymentVerified()))
                            .count();
                    int estimatedVehicles = totalPassengers == 0 ? 0 : (int) Math.ceil(totalPassengers / 4.0);

                    return new AgendaDayView(date, totalPassengers, pendingPayments, estimatedVehicles);
                })
                .toList();

        model.addAttribute("agenda", agenda);
        return "agenda"; 
    }

    // 🚐 2. Vista detalle del día CON LISTA DE CHOFERES (Ruta única corregida)
    @GetMapping("/agenda/view-detalle")
    public String dayAgenda(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

     List<Reservation> reservations = reservationRepository.findByTravelDate(date);

        // 🚖 2. Buscamos los choferes reales cargados en tu Postgres
        List<Driver> choferes = driverRepository.findByActiveTrue();

        model.addAttribute("date", date);
        model.addAttribute("reservations", reservations);
        model.addAttribute("choferes", choferes); // Se los mandamos al HTML con el mismo nombre

        return "agenda-day";

        
    }

    // 💳 3. Endpoint de Verificación asíncrona del pago + Notificación WhatsApp
    @PostMapping("/api/agenda/confirmar-pago/{id}")
    @ResponseBody
    public ResponseEntity<Void> verifyPayment(@PathVariable UUID id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    reservation.setPaymentVerified(true);
                    reservation.setStatus("CONFIRMED");
                    reservationRepository.saveAndFlush(reservation);

                    try {
                        String clienteCelular = reservation.getPassenger().getPhone();
                        String nombrePasajero = reservation.getPassenger().getFirstName();
                        
                        String mensajeWhatsApp = """
                                ✅ *¡Pago Verificado con Éxito!*
                                
                                Hola %s, te confirmamos que recibimos correctamente tu transferencia. Tu reserva para el traslado hacia *%s* ya se encuentra asentada de forma definitiva.
                                
                                🚐 Próximamente nos comunicaremos para coordinar el horario exacto en el que el chofer pasará por tu domicilio. ¡Muchas gracias por viajar con Lunaris!
                                """.formatted(nombrePasajero, reservation.getDestination());

                        whatsAppService.sendMessage(clienteCelular, mensajeWhatsApp);
                        
                    } catch (Exception e) {
                        org.slf4j.LoggerFactory.getLogger(getClass())
                            .error("No se pudo enviar el WhatsApp de confirmación de pago", e);
                    }

                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 📄 4. Descarga segura del comprobante de pago directo desde los servidores de Meta
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
            headers.setBearerAuth(whatsappToken); // Usa el token inyectado de forma segura
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Consultamos la URL de descarga real a Meta
            ResponseEntity<JsonNode> mediaResponse = restTemplate.exchange(
                    receiptUrl, HttpMethod.GET, entity, JsonNode.class);
            
            String actualDownloadUrl = mediaResponse.getBody().get("url").asText();

            // Descargamos los bytes de la foto del comprobante
            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(
                    actualDownloadUrl, HttpMethod.GET, entity, byte[].class);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageResponse.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }


    // 🚖 Envío automático de Hoja de Ruta al Chofer por API de Meta
    @PostMapping("/api/agenda/enviar-hoja-ruta")
    public ResponseEntity<Void> enviarHojaRuta(
            @RequestParam("phone") String choferPhone,
            @RequestBody List<UUID> reservationIds) {
        
        if (reservationIds == null || reservationIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 1. Armamos el encabezado de la hoja de ruta
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("📋 *HOJA DE RUTA - TRASLADOS LUNARIS*\n");
        mensaje.append("--------------------------------------------------\n\n");

        int index = 1;
        for (UUID id : reservationIds) {
            Reservation res = reservationRepository.findById(id).orElse(null);
            if (res == null) continue;

            String nombre = res.getPassenger().getFirstName() + " " + res.getPassenger().getLastName();
            String origen = res.getPickupLocality();
            String destino = res.getDestination();
            String direccion = (res.getPickupAddress() != null && !res.getPickupAddress().isEmpty()) ? res.getPickupAddress() : "No especificada";
            String telefono = res.getPassenger().getPhone();
            String observaciones = (res.getNotes() != null && !res.getNotes().isEmpty()) ? res.getNotes() : "-";

            mensaje.append(String.format("🚐 *VIAJE #%d*\n", index));
            mensaje.append(String.format("👤 *Pasajero:* %s\n", nombre));
            mensaje.append(String.format("📍 *Origen:* %s\n", origen));
            mensaje.append(String.format("🏁 *Destino:* %s\n", destino));
            mensaje.append(String.format("🏠 *Dirección:* %s\n", direccion));
            mensaje.append(String.format("📞 *Tel:* %s\n", telefono));
            mensaje.append(String.format("📝 *Obs:* %s\n", observaciones));
            mensaje.append("--------------------------------------------------\n\n");
            index++;
        }

        mensaje.append("_¡Buen viaje! Por cualquier duda comunicarse con la base._");

        // 2. Disparamos el mensaje usando tu servicio oficial de WhatsApp
        try {
            whatsAppService.sendMessage(choferPhone, mensaje.toString());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                .error("Error al enviar la hoja de ruta al chofer por webhook/API", e);
            return ResponseEntity.status(500).build();
        }
    }

    // DTO para la estructura interna de los conductores
    public record Chofer(String nombre, String telefono) {}
}