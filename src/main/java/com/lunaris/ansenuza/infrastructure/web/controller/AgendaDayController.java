package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AgendaDayController {

    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;

    // Escucha en /agenda/day?date=2026-06-17
    @GetMapping("/agenda/day")
    public String dayAgenda(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<Reservation> reservations = reservationRepository.findByTravelDate(date);

        model.addAttribute("date", date);
        model.addAttribute("reservations", reservations);

        return "agenda-day"; // Levanta src/main/resources/templates/agenda-day.html
    }

    @PostMapping("/agenda/verify-payment/{id}")
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

    @GetMapping("/agenda/comprobante/{id}")
    public ResponseEntity<byte[]> getReceiptImage(@PathVariable UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        String receiptUrl = reservation.getPaymentReceiptUrl();
        if (receiptUrl == null || !receiptUrl.contains("v20.0/")) {
            return ResponseEntity.notFound().build();
        }

        String whatsappToken = "TU_TOKEN_DE_ACCESS_DE_WHATSAPP_AQUÍ";

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> mediaResponse = restTemplate.exchange(
                    receiptUrl, HttpMethod.GET, entity, JsonNode.class);
            
            String actualDownloadUrl = mediaResponse.getBody().get("url").asText();

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
}