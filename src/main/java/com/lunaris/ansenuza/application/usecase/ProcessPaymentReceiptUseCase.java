package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
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
    private final LiveChatPort liveChat;

    @Transactional
    public void execute(String phoneNumber, String mediaId) {
        // 1. Descargamos y persistimos el comprobante una única vez. Devuelve la URL
        //    pública (Cloudinary secure_url) con la que se renderiza la imagen.
        String receiptUrl = receiptStoragePort.downloadAndSaveReceipt(mediaId);

        if (receiptUrl != null) {
            // 2. Reflejamos la imagen en la sala de chat en vivo del operador (persistencia +
            //    broadcast por WebSocket). El frontend detecta la URL y la renderiza como <img>.
            liveChat.recordIncomingMessage(phoneNumber, receiptUrl);
        } else {
            log.warn("[Bot Webhook] El almacenamiento devolvió NULL al descargar el mediaId: {}",
                    mediaId);
        }

        // 3. Enlazamos el comprobante con la primera reserva cronológica esperando pago.
        Optional<Passenger> passengerOpt = passengerRepository.findByPhone(phoneNumber);
        if (passengerOpt.isPresent()) {
            List<Reservation> activeReservations =
                    reservationRepository
                            .findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(
                                    passengerOpt.get());

            // 🎯 FILTRO INTELIGENTE: Busca la primera reserva cronológica esperando pago estricto
            Optional<Reservation> pendingReservation = activeReservations.stream()
                    .filter(r -> "PENDING_PAYMENT".equals(r.getStatus()))
                    .findFirst();

            if (pendingReservation.isPresent() && receiptUrl != null) {
                Reservation reservation = pendingReservation.get();
                reservation.setPaymentReceiptUrl(receiptUrl);
                // El comprobante queda pendiente de revisión humana; recibirlo no verifica el pago.
                reservation.setPaymentVerified(false);
                reservation.setStatus("PAYMENT_RECEIVED");
                reservationRepository.saveAndFlush(reservation);
                log.info("[Bot Webhook] Comprobante enlazado con éxito para código: {}",
                        reservation.getReservationCode());
            } else if (pendingReservation.isEmpty()) {
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

    @Transactional
    public Reservation confirmOrCreateWebBooking(
            String phoneNumber,
            MultipartFile receiptFile,
            BookingVerificationData bookingData) {
        String normalizedPhone = com.lunaris.ansenuza.shared.PhoneUtils.normalizeArgentinePhone(phoneNumber);
        Passenger passenger = passengerRepository.findByPhone(normalizedPhone)
                .orElseThrow(() -> new com.lunaris.ansenuza.domain.exception.DomainValidationException(
                        "No existe un pasajero para confirmar la reserva."));
        Optional<Reservation> pendingReservation = reservationRepository
                .findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(passenger)
                .stream()
                .filter(candidate -> "PENDING_PAYMENT".equals(candidate.getStatus())
                        || "PAYMENT_RECEIVED".equals(candidate.getStatus())
                        || "PENDING_VERIFICATION".equals(candidate.getStatus()))
                .findFirst();

        if (pendingReservation.isEmpty()) {
            validateBookingData(bookingData);
        }

        String receiptUrl = uploadReceipt(receiptFile, normalizedPhone);
        Reservation reservation = pendingReservation.orElseGet(() -> newWebReservation(passenger, bookingData));
        if (receiptUrl != null) {
            reservation.setPaymentReceiptUrl(receiptUrl);
        }
        if (pendingReservation.isPresent()) {
            reservation.setPaymentVerified(true);
            reservation.setStatus("CONFIRMED");
        }
        reservationRepository.saveAndFlush(reservation);
        return reservation;
    }

    private String uploadReceipt(MultipartFile receiptFile, String phoneNumber) {
        if (receiptFile == null || receiptFile.isEmpty()) {
            return null;
        }
        String receiptUrl = receiptStoragePort.uploadFile(receiptFile);
        if (receiptUrl == null || receiptUrl.isBlank()) {
            log.warn("No se pudo almacenar el comprobante subido para el teléfono {}.", phoneNumber);
            return null;
        }
        return receiptUrl;
    }

    private Reservation newWebReservation(Passenger passenger, BookingVerificationData data) {
        return Reservation.builder()
                .id(UUID.randomUUID())
                .passenger(passenger)
                .travelDate(data.travelDate())
                .departureSchedule(data.scheduleBlock().trim())
                .pickupLocality(data.pickupLocality().trim())
                .destination(data.destination().trim())
                .passengerCount(data.passengerCount())
                .tripType(data.tripType())
                .roundTrip(data.tripType() != com.lunaris.ansenuza.domain.model.TripType.ONE_WAY)
                .amount(data.totalAmount())
                .paymentVerified(false)
                .status("PENDING_VERIFICATION")
                .source(ReservationSource.WEB)
                .build();
    }

    private void validateBookingData(BookingVerificationData data) {
        if (data == null || data.travelDate() == null
                || isBlank(data.scheduleBlock()) || isBlank(data.pickupLocality())
                || isBlank(data.destination()) || data.passengerCount() == null
                || data.passengerCount() < 1 || data.tripType() == null
                || data.totalAmount() == null || data.totalAmount().signum() < 0) {
            throw new com.lunaris.ansenuza.domain.exception.DomainValidationException(
                    "Los datos completos del viaje son obligatorios para crear la reserva.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
