package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Persiste el comprobante en una transacción corta, sin llamadas de red externas. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersistPaymentReceiptUseCase {

    private final PassengerRepository passengerRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository reservationEventRepository;

    @Transactional
    public void execute(String phoneNumber, String receiptUrl) {
        Optional<Passenger> passenger = passengerRepository.findByPhone(phoneNumber);
        if (passenger.isEmpty()) {
            log.warn("[Bot Webhook] No existe ningún pasajero registrado con el teléfono: {}",
                    phoneNumber);
            return;
        }

        Optional<Reservation> pending = reservationRepository
                .findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(passenger.get())
                .stream()
                .filter(reservation -> "PENDING_PAYMENT".equals(reservation.getStatus()))
                .findFirst();
        if (pending.isEmpty()) {
            log.warn("[Bot Webhook] No se encontró ninguna reserva en PENDING_PAYMENT para el teléfono: {}",
                    phoneNumber);
            return;
        }

        Reservation selected = pending.get();
        String groupCode = selected.getBookingGroupCode() != null
                && !selected.getBookingGroupCode().isBlank()
                        ? selected.getBookingGroupCode()
                        : paymentGroupCode(selected.getReservationCode());
        List<Reservation> group = groupCode == null
                ? List.of(selected)
                : reservationRepository.findReservationGroupForUpdate(groupCode);
        if (group.isEmpty()) group = List.of(selected);
        String receiptGroup = groupCode == null ? selected.getReservationCode() : groupCode;
        if (reservationRepository.existsActiveReceiptInAnotherGroup(receiptUrl, receiptGroup)) {
            throw new DomainValidationException(
                    "El comprobante ya está vinculado a otra reserva activa.");
        }
        group.forEach(reservation -> {
            reservation.setPaymentReceiptUrl(receiptUrl);
            reservation.setPaymentVerified(false);
            reservation.setStatus("PAYMENT_RECEIVED");
            reservation.setPaymentExpiresAt(null);
        });
        reservationRepository.saveAllAndFlush(group);
        group.forEach(reservation -> reservationEventRepository.save(ReservationEvent.builder()
                .reservationId(reservation.getId())
                .eventType("PAYMENT_RECEIPT_LINKED")
                .description("Comprobante recibido; pago pendiente de verificación.")
                .triggeredBy("PASSENGER_WHATSAPP")
                .build()));
        log.info("[Bot Webhook] Comprobante enlazado con éxito para código: {}",
                selected.getReservationCode());
    }

    @Transactional
    public void executeByReservationCode(String reservationCode, String receiptUrl,
            String triggeredBy) {
        Reservation selected = reservationRepository.findByReservationCodeForUpdate(reservationCode)
                .orElseThrow(() -> new DomainValidationException("La reserva indicada no existe."));
        String groupCode = selected.getBookingGroupCode() != null
                && !selected.getBookingGroupCode().isBlank()
                        ? selected.getBookingGroupCode() : paymentGroupCode(reservationCode);
        List<Reservation> group = groupCode == null ? List.of(selected)
                : reservationRepository.findByBookingGroupCodeForUpdate(groupCode);
        if (group.isEmpty()) group = List.of(selected);
        String receiptGroup = groupCode == null ? reservationCode : groupCode;
        if (reservationRepository.existsActiveReceiptInAnotherGroup(receiptUrl, receiptGroup)) {
            throw new DomainValidationException(
                    "El comprobante ya está vinculado a otra reserva activa.");
        }
        for (Reservation reservation : group) {
            reservation.setPaymentReceiptUrl(receiptUrl);
            reservation.setPaymentVerified(false);
            reservation.setStatus("PAYMENT_RECEIVED");
            reservation.setPaymentExpiresAt(null);
            reservationRepository.save(reservation);
            reservationEventRepository.save(ReservationEvent.builder()
                    .reservationId(reservation.getId()).eventType("PAYMENT_RECEIPT_LINKED")
                    .description("Comprobante recibido; pago pendiente de verificación.")
                    .triggeredBy(triggeredBy).build());
        }
    }

    private String paymentGroupCode(String reservationCode) {
        if (reservationCode == null || !(reservationCode.endsWith("-IDA")
                || reservationCode.endsWith("-VUELTA"))) return null;
        return reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
    }
}
