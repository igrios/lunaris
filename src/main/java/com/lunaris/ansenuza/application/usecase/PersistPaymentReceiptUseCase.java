package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Persiste el comprobante en una transacción corta, sin llamadas de red externas. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersistPaymentReceiptUseCase {

    private final PassengerRepository passengerRepository;
    private final ReservationRepository reservationRepository;

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
        group.forEach(reservation -> {
            reservation.setPaymentReceiptUrl(receiptUrl);
            reservation.setPaymentVerified(false);
            reservation.setStatus("PAYMENT_RECEIVED");
        });
        reservationRepository.saveAllAndFlush(group);
        log.info("[Bot Webhook] Comprobante enlazado con éxito para código: {}",
                selected.getReservationCode());
    }

    private String paymentGroupCode(String reservationCode) {
        if (reservationCode == null || !(reservationCode.endsWith("-IDA")
                || reservationCode.endsWith("-VUELTA"))) return null;
        return reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
    }
}
