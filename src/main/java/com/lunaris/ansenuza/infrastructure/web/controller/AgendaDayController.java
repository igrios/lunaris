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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AgendaDayController {

    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    @GetMapping("/agenda/day")
    public String dayAgenda(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<Reservation> reservations = reservationRepository.findByTravelDate(date);

        List<Reservation> activeReservations = reservations.stream()
                .filter(r -> r != null)
                .filter(r -> r.getStatus() == null || !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> r.getPassengerCount() == null || r.getPassengerCount() > 0)
                .toList();

        model.addAttribute("date", date);
        model.addAttribute("reservations", activeReservations);

        return "agenda-day";
    }

    @PostMapping("/agenda/verify-payment/{id}")
    @ResponseBody
    public ResponseEntity<Void> verifyPayment(@PathVariable UUID id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    confirmPaymentUseCase.execute(id);

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

    // 🛡️ CONTROLADOR REPARADO PARA SOPORTAR CARGAS MANUALES Y REDIRECCIONES DIRECTAS
    @GetMapping("/agenda/comprobante/{id}")
    public ResponseEntity<byte[]> getReceiptImage(@PathVariable UUID id) {
        // Buscamos la reserva de forma segura sin clavar excepciones orElseThrow
        Reservation reservation = reservationRepository.findById(id).orElse(null);

        if (reservation == null) {
            // Si no se encuentra la reserva por UUID, evitamos el crash y devolvemos un 404 limpio
            return ResponseEntity.notFound().build();
        }

        String receiptUrl = reservation.getPaymentReceiptUrl();
        if (receiptUrl == null || receiptUrl.isBlank() || "null".equalsIgnoreCase(receiptUrl)) {
            return ResponseEntity.notFound().build();
        }

        // 💡 Si el campo ya contiene una URL HTTP directa de Cloudinary, render o Supabase
        // Redirigimos el navegador de una para que el operador vea la imagen y no consuma recursos de tu backend
        if (receiptUrl.startsWith("http://") || receiptUrl.startsWith("https://")) {
            if (!receiptUrl.contains("graph.facebook.com")) {
                return ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, receiptUrl)
                        .build();
            }
        }

        // 🛠️ Fallback: Si es un ID de recurso de la API de Meta, procede con la descarga binaria tradicional
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
            org.slf4j.LoggerFactory.getLogger(getClass())
                .error("Error al procesar la descarga del adjunto de Meta/WhatsApp", e);
            return ResponseEntity.status(500).build();
        }
    }
}
