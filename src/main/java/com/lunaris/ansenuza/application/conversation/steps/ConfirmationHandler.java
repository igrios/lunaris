package com.lunaris.ansenuza.application.conversation.steps;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.conversation.WaitingListCapacityGuard;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** ASK_CONFIRMATION: confirma o cancela la reserva; al confirmar persiste pasajero + reserva(s). */
@Component
@RequiredArgsConstructor
public class ConfirmationHandler implements ConversationStepHandler {

    private static final String CBU_BANNER_IMAGE_URL =
            "https://res.cloudinary.com/dgrwrcb5p/image/upload/CBU_MARTIN_nxpvk8.jpg";

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerRepository passengerRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final PromotionService promotionService;
    private final ReservationService reservationService;
    private final MessagingPort messaging;
    private final WaitingListCapacityGuard capacityGuard;

    @Override
    public String step() {
        return "ASK_CONFIRMATION";
    }

    @Override
    @Transactional
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("confirm_ok".equals(body)) {
            if (capacityGuard.offerWaitingListWhenFull(session)) {
                return;
            }

            int totalAsientos = session.getPassengerCount() != null ? session.getPassengerCount() : 1;

            Passenger passenger = passengerRepository.findByPhone(phoneNumber).orElseGet(() -> {
                String[] names = session.getPassengerName().trim().split("\\s+", 2);
                return passengerRepository.saveAndFlush(Passenger.builder()
                        .firstName(names[0])
                        .lastName(names.length > 1 ? names[1] : "")
                        .phone(phoneNumber)
                        .address(session.getPickupAddress())
                        .locality(session.getPickupLocality())
                        .cuil(session.getCuil())
                        .build());
            });

            if (!session.getPickupAddress().equalsIgnoreCase(passenger.getAddress())
                    || !session.getPickupLocality().equalsIgnoreCase(passenger.getLocality())) {
                passenger.setAddress(session.getPickupAddress());
                passenger.setLocality(session.getPickupLocality());
                passengerRepository.saveAndFlush(passenger);
            }

            BigDecimal price = pricingAndScheduleService.calculateTripPrice(
                    session.getPickupLocality(), session.getRoundTrip(), totalAsientos);

            BigDecimal discountAmount = BigDecimal.ZERO;
            boolean freePromotion = false;
            Promotion appliedPromotion = null;
            if (session.getPromotionCode() != null) {
                Promotion promotion;
                try {
                    promotion = promotionService.requireAvailable(session.getPromotionCode(), phoneNumber);
                } catch (IllegalArgumentException exception) {
                    session.setCurrentStep("ASK_PROMOTION_CODE");
                    conversationSessionRepository.saveAndFlush(session);
                    messaging.sendText(phoneNumber,
                            "❌ " + exception.getMessage() + ". Ingresá otro código o escribí *SIN PROMO*.");
                    return;
                }
                discountAmount = promotionService.calculateDiscount(price, promotion.getDiscountPercentage());
                price = price.subtract(discountAmount).max(BigDecimal.ZERO);
                freePromotion = promotion.getDiscountPercentage() == 100;
                appliedPromotion = promotion;
            }

            // 🕒 REPARACIÓN FASE 3: Tomamos el bloque dinámico real elegido por el cliente
            String baseHour = session.getScheduleBlock() != null ? session.getScheduleBlock() : "03:00 AM";
            
            String notes = baseHour;
            if (session.getReturnDate() == null && Boolean.TRUE.equals(session.getRoundTrip())) {
                notes += " (Abierta)";
            }

            Reservation nuevaReserva = Reservation.builder()
                    .passenger(passenger)
                    .travelDate(session.getTravelDate())
                    .returnDate(session.getReturnDate())
                    .pickupLocality(session.getPickupLocality())
                    .pickupAddress(session.getPickupAddress())
                    .destination(session.getDestination())
                    .roundTrip(session.getRoundTrip())
                    .paymentVerified(freePromotion)
                    .requiresInvoice(!freePromotion)
                    .amount(price)
                    .discountAmount(discountAmount)
                    .promotionCode(session.getPromotionCode())
                    .promotionId(appliedPromotion != null ? appliedPromotion.getId() : null)
                    .promotionDiscountPercentage(
                            appliedPromotion != null ? appliedPromotion.getDiscountPercentage() : null)
                    .notes(notes)
                    .departureSchedule(baseHour)
                    .status(freePromotion ? "CONFIRMED" : "PENDING_PAYMENT")
                    .source(ReservationSource.WHATSAPP)
                    .passengerCount(totalAsientos)
                    .companionNames(session.getCompanionNames())
                    .build();

            List<Reservation> savedReservations = reservationService.saveReservationFlow(nuevaReserva);
            boolean paymentConfirmed = savedReservations.stream()
                    .allMatch(reservation -> Boolean.TRUE.equals(reservation.getPaymentVerified()));
            if (session.getPromotionCode() != null && paymentConfirmed) {
                promotionService.consume(session.getPromotionCode(), phoneNumber);
            }
            conversationSessionRepository.delete(session);

            if (Boolean.TRUE.equals(session.getRoundTrip())) {
                messaging.sendText(phoneNumber, "📌 Información importante sobre tu regreso: "
                        + "Las salidas de regreso desde Córdoba se realizan de 14:00 a 15:00 hs "
                        + "o de 17:30 a 18:00 hs. Podés responder a este mensaje indicándonos "
                        + "tu preferencia de horario.");
            }

            if (freePromotion) {
                messaging.sendText(phoneNumber, """
                        ✅ *¡Reserva confirmada con promoción 100% bonificada!*

                        🎟️ Tu código fue aplicado y el pasaje quedó emitido. No necesitás realizar ningún pago ni se emitirá factura fiscal por un importe de $0.
                        ¡Buen viaje con Lunaris! 🚐
                        """);
                return;
            }

            messaging.sendImage(phoneNumber, CBU_BANNER_IMAGE_URL, "Datos para la transferencia");
            messaging.sendText(phoneNumber, """
                    ✅ *¡Tu traslado ha sido registrado con éxito!*

                    💳 *Datos bancarios para congelar la tarifa (Transferencia):*
                    • *Titular:* Martín Fernando Manuel Cuestaz
                    • *Alias:* cuestazm.bna
                    • *CBU:* 01103739330037363119529

                    📌 *Nota:* Una vez realizado el envío, *subí la captura o foto del comprobante por acá* para registrar tu pago de forma inmediata. ¡Buen viaje con Lunaris! 🚐
                    """);
            return;
        }

        if ("confirm_cancel".equals(body)) {
            session.setCurrentStep("FOLLOW_UP_RETENTION");
            conversationSessionRepository.saveAndFlush(session);

            messaging.sendText(phoneNumber, """
                    ❌ *Entendido, pausamos el trámite por acá.*

                    Tranqui, si tuviste un cambio de planes con los turnos médicos o el viaje:
                    ¿Querés que pasemos la ida para el día de mañana en el mismo horario o preferís dejar la consulta en espera?

                    _Escribinos si cambiás de idea y lo acomodamos al toque._
                    """);
            return;
        }
    }
}
