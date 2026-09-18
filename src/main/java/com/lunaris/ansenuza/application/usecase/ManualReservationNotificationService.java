package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.PassengerContactTemplate;
import com.lunaris.ansenuza.domain.model.ManualReservationCreated;
import com.lunaris.ansenuza.domain.model.PassengerMessageReceived;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.WhatsAppConversationWindowService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
public class ManualReservationNotificationService {
    public static final String PROMO = "💡 Tip Lunaris: ¡La próxima vez podés pedir tu viaje directamente por acá en 1 minuto! Nuestro Bot automático está disponible las 24 hs para cotizar, reservar y confirmarte al instante sin esperas. ¡Probalo en tu próximo viaje!";
    private final ReservationRepository reservations;
    private final WhatsAppConversationWindowService window;
    private final MessagingPort messaging;
    private final com.lunaris.ansenuza.domain.repository.InvoiceRepository invoices;
    private final TransactionTemplate transaction;
    private final TransactionTemplate withoutTransaction;

    public ManualReservationNotificationService(ReservationRepository reservations,
            WhatsAppConversationWindowService window, MessagingPort messaging,
            PlatformTransactionManager manager,
            com.lunaris.ansenuza.domain.repository.InvoiceRepository invoices) {
        this.reservations = reservations;
        this.window = window;
        this.messaging = messaging;
        this.invoices = invoices;
        this.transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        withoutTransaction = new TransactionTemplate(manager);
        withoutTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
    }

    @TransactionalEventListener
    public void created(ManualReservationCreated event) {
        deliver(event.reservationId(), false);
    }

    @EventListener
    public void incoming(PassengerMessageReceived event) {
        String phone;
        try {
            phone = PhoneUtils.normalizeArgentinePhone(event.phone());
        } catch (com.lunaris.ansenuza.domain.exception.DomainValidationException invalidPhone) {
            return;
        }
        try {
            for (Reservation reservation : reservations.findByPassengerPhoneAndManualNotificationWaitingReplyTrue(phone)) {
                deliver(reservation.getId(), true);
            }
        } catch (RuntimeException exception) {
            log.error("No se pudo recuperar el detalle pendiente del pasajero", exception);
        }
    }

    @Scheduled(fixedDelayString = "${lunaris.manual-notification.retry-ms:60000}")
    public void retryPending() {
        var now = com.lunaris.ansenuza.shared.ArgentinaTime.now();
        reservations.findRetryableManualNotifications(now.minusMinutes(5), now.minusHours(24),
                org.springframework.data.domain.PageRequest.of(0, 50))
                .forEach(reservation -> deliver(reservation.getId(), reservation.isManualNotificationWaitingReply()));
    }

    /** Asociar la factura nunca reemplaza la evidencia de pago del pasajero. */
    public boolean invoiceReady(UUID id, String url) {
        transaction.executeWithoutResult(status -> {
            Reservation reservation = reservations.findById(id).orElseThrow();
            String groupCode = com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.groupCode(reservation);
            var group = groupCode.startsWith("UUID:") ? List.of(reservation) : reservations.findReservationGroup(groupCode);
            if (group.isEmpty()) group = List.of(reservation);
            for (Reservation leg : group) leg.setInvoiceUrl(url);
            reservation.setInvoiceUrl(url);
            reservation.setManualNotificationPending(true);
            reservation.setManualNotificationWaitingReply(false);
            reservation.setManualNotificationAttemptAt(null);
        });
        deliver(id, false);
        return Boolean.TRUE.equals(transaction.execute(status -> reservations.findById(id)
                .map(r -> !r.isManualNotificationPending()).orElse(false)));
    }

    public void deliver(UUID id, boolean replying) {
        try {
            Reservation reservation = transaction.execute(status -> {
                var locked = reservations.findAllByIdForUpdate(List.of(id));
                if (locked.isEmpty()) return null;
                Reservation r = locked.getFirst();
                if (!r.isManualNotificationPending()) return null;
                if (r.isManualNotificationWaitingReply() && !replying) return null;
                var now = com.lunaris.ansenuza.shared.ArgentinaTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
                if (r.getManualNotificationAttemptAt() != null
                        && r.getManualNotificationAttemptAt().isAfter(now.minusMinutes(5))) return null;
                if (java.util.Set.of("CANCELLED", "CANCELED", "EXPIRED", "REJECTED").contains(value(r.getStatus()))) {
                    r.setManualNotificationPending(false);
                    r.setManualNotificationWaitingReply(false);
                    return null;
                }
                r.setManualNotificationAttemptAt(now);
                if (replying) r.setManualNotificationWaitingReply(false);
                return r;
            });
            if (reservation == null) return;
            withoutTransaction.executeWithoutResult(status -> dispatch(reservation, replying));
        } catch (RuntimeException exception) {
            log.error("La notificación de la reserva {} continúa pendiente", id, exception);
        }
    }

    private void dispatch(Reservation reservation, boolean replying) {
        String phone = reservation.getPassenger().getPhone();
        boolean active = replying || window.isActive(phone);
        List<String> parameters = parameters(reservation);
        if (!active) {
            messaging.sendTemplate(phone, PassengerContactTemplate.NAME,
                    PassengerContactTemplate.parameters(reservation.getPassenger().getFirstName()),
                    sent -> complete(reservation, sent, true));
            return;
        }
        messaging.sendText(phone, confirmation(parameters) + extendedDetails(reservation), sent -> {
            if (!sent || reservation.getInvoiceUrl() == null) {
                complete(reservation, sent, false);
            } else {
                messaging.sendDocumentUrl(phone, reservation.getInvoiceUrl(), "Factura.pdf",
                        "Factura de tu reserva " + reservation.getReservationCode(),
                        documentSent -> complete(reservation, documentSent, false));
            }
        });
    }

    private void complete(Reservation attempt, boolean sent, boolean hsm) {
        transaction.executeWithoutResult(status -> {
            var locked = reservations.findAllByIdForUpdate(List.of(attempt.getId()));
            if (locked.isEmpty()) return;
            Reservation reservation = locked.getFirst();
            if (!java.util.Objects.equals(reservation.getManualNotificationAttemptAt(), attempt.getManualNotificationAttemptAt())) return;
            reservation.setManualNotificationAttemptAt(null);
            reservation.setManualNotificationPending(!sent || hsm);
            reservation.setManualNotificationWaitingReply(sent && hsm);
            if (sent && !hsm && reservation.getInvoiceUrl() != null) {
                String code = com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.groupCode(reservation);
                var group = code.startsWith("UUID:") ? List.of(reservation) : reservations.findReservationGroup(code);
                var ids = group.isEmpty() ? List.of(reservation.getId()) : group.stream().map(Reservation::getId).toList();
                invoices.findFirstByReservationIdIn(ids).ifPresent(invoice -> {
                    invoice.setSentViaWhatsapp(true);
                    invoice.setSentAt(com.lunaris.ansenuza.shared.ArgentinaTime.now());
                });
            }
        });
    }

    public static List<String> parameters(Reservation reservation) {
        String document = reservation.getInvoiceUrl();
        if (document == null || document.isBlank()) document = reservation.getPaymentReceiptUrl();
        if (document == null || document.isBlank()) document = Boolean.TRUE.equals(reservation.getRequiresInvoice()) ? "Factura pendiente de emisión y envío por administración" : "No solicitado";
        return List.of(reservation.getPassenger().getFirstName(), reservation.getPickupLocality(),
                reservation.getDestination(), String.valueOf(reservation.getTravelDate()) + " " + value(reservation.getDepartureSchedule()),
                reservation.getReservationCode(), document).stream()
                .map(parameter -> parameter.replaceAll("\\s+", " ").trim()).toList();
    }

    public static String confirmation(List<String> p) {
        return "¡Hola %s! Tu reserva fue registrada con éxito 🚌✨\n\n📍 Trayecto: %s -> %s\n📅 Fecha y hora: %s\n🎟️ Código de reserva: %s\n📄 Factura/Comprobante: %s\n\n%s"
                .formatted(p.get(0), p.get(1), p.get(2), p.get(3), p.get(4), p.get(5), PROMO);
    }

    private static String extendedDetails(Reservation r) {
        return "\n\nDomicilio: " + value(r.getPickupAddress()) + "\nPasajeros: " + r.getPassengerCount()
                + "\nAcompañantes: " + value(r.getCompanionNames()) + "\nTipo de viaje: " + tripLabel(r);
    }

    private static String tripLabel(Reservation reservation) {
        if (reservation.getTripType() == null) return "A confirmar";
        return switch (reservation.getTripType()) {
            case ONE_WAY -> "Solo ida";
            case ROUND_TRIP -> "Ida y vuelta";
            case OPEN_RETURN -> "Ida y vuelta con regreso abierto";
        };
    }

    private static String value(String value) { return value == null || value.isBlank() ? "A confirmar" : value; }
}
