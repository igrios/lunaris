package com.lunaris.ansenuza.application.conversation.steps;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;

/** ASK_CONFIRMATION: confirma o cancela la reserva; al confirmar persiste pasajero + reserva(s). */
@Component
@RequiredArgsConstructor
public class ConfirmationHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerRepository passengerRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final ReservationService reservationService;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_CONFIRMATION";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("confirm_ok".equals(body)) {
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

            int totalAsientos = session.getPassengerCount() != null ? session.getPassengerCount() : 1;

            BigDecimal price = pricingAndScheduleService.calculateTripPrice(
                    session.getPickupLocality(), session.getRoundTrip(), totalAsientos);

            String baseHour = (session.getCurrentCompanionIndex() != null
                    && session.getCurrentCompanionIndex() == 8) ? "08:00 AM" : "03:00 AM";
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
                    .paymentVerified(false)
                    .amount(price)
                    .notes(notes)
                    .status("PENDING_PAYMENT")
                    .passengerCount(totalAsientos)
                    .companionNames(session.getCompanionNames())
                    .build();

            reservationService.saveReservationFlow(nuevaReserva);
            conversationSessionRepository.delete(session);

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
