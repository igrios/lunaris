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
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
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
    private final SameDayBookingPolicy sameDayBookingPolicy;
    private final ReservationService reservationService;
    private final PersistPaymentReceiptUseCase persistPaymentReceiptUseCase;
    private final PricingAndScheduleService pricingAndScheduleService;

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

        // 3. Recién ahora se abre una transacción corta para datos financieros.
        if (receiptUrl != null) persistPaymentReceiptUseCase.execute(phoneNumber, receiptUrl);

        messaging.sendText(phoneNumber,
                "✅ *Comprobante recibido.*\n\nNuestro equipo verificará la transferencia y confirmará tu viaje a la brevedad.");
    }

    /** Procesa un recurso ya disponible localmente, usado por el simulador de desarrollo. */
    public void executeStoredReceipt(String phoneNumber, String receiptUrl) {
        if (receiptUrl == null || receiptUrl.isBlank()) {
            throw new IllegalArgumentException("La URL del comprobante es obligatoria.");
        }
        liveChat.recordIncomingMessage(phoneNumber, receiptUrl);
        persistPaymentReceiptUseCase.execute(phoneNumber, receiptUrl);
        messaging.sendText(phoneNumber,
                "✅ *Comprobante recibido.*\n\nNuestro equipo verificará la transferencia y confirmará tu viaje a la brevedad.");
    }

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
            sameDayBookingPolicy.validate(
                    bookingData.travelDate(), bookingData.scheduleBlock());
        }

        // La llamada a Cloudinary/almacenamiento ocurre antes de abrir cualquier transacción.
        String receiptUrl = uploadReceipt(receiptFile, normalizedPhone);
        Reservation reservation = pendingReservation.orElseGet(() -> newWebReservation(passenger, bookingData));
        if (pendingReservation.isPresent()) {
            if (receiptUrl != null) {
                persistPaymentReceiptUseCase.executeByReservationCode(
                        reservation.getReservationCode(), receiptUrl, "PASSENGER_WEB");
                return reservationRepository.findById(reservation.getId()).orElse(reservation);
            }
            return reservation;
        }
        if (receiptUrl != null) reservation.setPaymentReceiptUrl(receiptUrl);
        return reservationService.saveReservationFlow(reservation).getFirst();
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
                // El total enviado por el navegador se ignora deliberadamente.
                .amount(pricingAndScheduleService.calculateReservationAmount(
                        data.pickupLocality(), data.destination(), data.tripType(), data.passengerCount()))
                .paymentVerified(false)
                .requiresInvoice(true)
                .status("PENDING_VERIFICATION")
                .source(ReservationSource.WEB)
                .build();
    }

    private void validateBookingData(BookingVerificationData data) {
        if (data == null || data.travelDate() == null
                || isBlank(data.scheduleBlock()) || isBlank(data.pickupLocality())
                || isBlank(data.destination()) || data.passengerCount() == null
                || data.passengerCount() < 1 || data.tripType() == null) {
            throw new com.lunaris.ansenuza.domain.exception.DomainValidationException(
                    "Los datos completos del viaje son obligatorios para crear la reserva.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
