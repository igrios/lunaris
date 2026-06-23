package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Procesa el comprobante de pago (imagen) que envía el pasajero por WhatsApp:
 * lo enlaza con la primera reserva cronológica en estado PENDING_PAYMENT y la
 * promueve a PAYMENT_RECEIVED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentReceiptUseCase {

    private final PassengerRepository passengerRepository;
    private final ReservationRepository reservationRepository;
    private final ReceiptStoragePort receiptStoragePort;
    private final MessagingPort messaging;

    public void execute(String phoneNumber, String mediaId) {
        Optional<Passenger> passengerOpt = passengerRepository.findByPhone(phoneNumber);
        if (passengerOpt.isPresent()) {
            List<Reservation> activeReservations =
                    reservationRepository.findByPassengerOrderByTravelDateAsc(passengerOpt.get());

            // 🎯 FILTRO INTELIGENTE: Busca la primera reserva cronológica esperando pago estricto
            Optional<Reservation> pendingReservation = activeReservations.stream()
                    .filter(r -> "PENDING_PAYMENT".equals(r.getStatus()))
                    .findFirst();

            if (pendingReservation.isPresent()) {
                Reservation reservation = pendingReservation.get();
                String localWebUrl = receiptStoragePort.downloadAndSaveReceipt(mediaId);
                if (localWebUrl != null) {
                    reservation.setPaymentReceiptUrl(localWebUrl);
                    // Cambiamos el estado de forma canónica para renderizar celeste en agenda
                    reservation.setStatus("PAYMENT_RECEIVED");
                    reservationRepository.saveAndFlush(reservation);
                    log.info("[Bot Webhook] Comprobante enlazado con éxito para código: {}",
                            reservation.getReservationCode());
                } else {
                    log.warn("[Bot Webhook] El almacenamiento local devolvió NULL al descargar el mediaId: {}",
                            mediaId);
                }
            } else {
                log.warn("[Bot Webhook] No se encontró ninguna reserva en PENDING_PAYMENT para el teléfono: {}",
                        phoneNumber);
            }
        } else {
            log.warn("[Bot Webhook] No existe ningún pasajero registrado con el teléfono: {}",
                    phoneNumber);
        }
        messaging.sendText(phoneNumber,
                "✅ *Comprobante recibido.*\n\nNuestro equipo verificará la transferencia y confirmará tu viaje a la brevedad.");
    }
}
