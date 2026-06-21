package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository reservationEventRepository;

    @Transactional
    public List<Reservation> saveReservationFlow(Reservation mainReservation) {
        List<Reservation> savedReservations = new ArrayList<>();

        // 1. Limpiamos espacios en blanco y formateamos los prefijos (ej: "Morteros" -> "COR")
        String originClean = mainReservation.getPickupLocality().trim();
        String destClean = mainReservation.getDestination().trim();

        String originPref = originClean.substring(0, 3).toUpperCase().replace("Ó", "O");
        String destPref = destClean.substring(0, 3).toUpperCase().replace("Ó", "O");

        // 2. Obtenemos la secuencia estimada inicial basada en la ruta y fecha de la ida
        long currentCount = reservationRepository.countSequenceByRouteAndDate(originClean,
                destClean, mainReservation.getTravelDate());
        long nextSequence = currentCount + 1;

        // 3. 🛡️ BUCLE DEFENSIVO ANTI-COLISIÓN (IDA)
        String codigoIda = String.format("%s-%s-%03d_I", originPref, destPref, nextSequence);
        while (reservationRepository.existsByReservationCode(codigoIda)) {
            nextSequence++;
            codigoIda = String.format("%s-%s-%03d_I", originPref, destPref, nextSequence);
        }

        mainReservation.setReservationCode(codigoIda);
        mainReservation
                .setPaymentVerified(Boolean.TRUE.equals(mainReservation.getPaymentVerified()));
        mainReservation
                .setStatus(Boolean.TRUE.equals(mainReservation.getPaymentVerified()) ? "CONFIRMED"
                        : "PENDING_PAYMENT");

        Reservation savedMain = reservationRepository.save(mainReservation);
        savedReservations.add(savedMain);

        // 🌟 CORRECCIÓN IDE: Variable local para evitar el warning Null type safety
        ReservationEvent eventIda = ReservationEvent.builder().reservationId(savedMain.getId())
                .eventType("RESERVATION_CREATED")
                .description("Reserva de IDA creada automáticamente con código " + codigoIda)
                .triggeredBy("API_SYSTEM").build();
        reservationEventRepository.save(eventIda);

        // 4. ¿Es Ida y Vuelta?
        if (Boolean.TRUE.equals(mainReservation.getRoundTrip())) {

            // 🛡️ BUCLE DEFENSIVO ANTI-COLISIÓN (VUELTA): Evita choques en el tramo inverso
            String codigoVuelta = String.format("%s-%s-%03d_V", destPref, originPref, nextSequence);
            while (reservationRepository.existsByReservationCode(codigoVuelta)) {
                nextSequence++;
                codigoVuelta = String.format("%s-%s-%03d_V", destPref, originPref, nextSequence);
            }

            Reservation returnReservation = new Reservation();
            returnReservation.setPassenger(mainReservation.getPassenger());
            returnReservation.setPickupLocality(mainReservation.getDestination()); // Inversión radial
            returnReservation.setDestination(mainReservation.getPickupLocality()); // Inversión radial

            // 🌟 CORRECCIÓN POSTGRES: Lógica adaptativa para Vuelta Abierta evitando Violación Not-Null
            if (mainReservation.getReturnDate() != null) {
                returnReservation.setTravelDate(mainReservation.getReturnDate());
                returnReservation
                        .setNotes("Vuelta vinculada automáticamente a la ida " + codigoIda);
            } else {
                // 📅 Fecha Centinela lejana: Saltea la restricción física de Postgres sin alterar la base
                returnReservation.setTravelDate(LocalDate.of(2099, 12, 31));
                returnReservation.setNotes(
                        "🛑 VUELTA ABIERTA - Pendiente confirmar fecha. Vinculada a la ida "
                                + codigoIda);
            }

            // Tasación simétrica automática
            returnReservation.setAmount(mainReservation.getAmount());
            returnReservation.setPassengerCount(mainReservation.getPassengerCount());
            returnReservation.setCompanionNames(mainReservation.getCompanionNames());
            returnReservation.setPaymentVerified(mainReservation.getPaymentVerified());
            returnReservation.setStatus(mainReservation.getStatus());
            returnReservation.setRoundTrip(true);
            returnReservation.setReservationCode(codigoVuelta);

            Reservation savedReturn = reservationRepository.save(returnReservation);
            savedReservations.add(savedReturn);

            // 🌟 EVENTO DE AUDITORÍA: Registro inmutable de la Vuelta
            String descEvento = (mainReservation.getReturnDate() != null)
                    ? "Reserva de VUELTA gemela creada automáticamente con código " + codigoVuelta
                    : "Reserva de VUELTA ABIERTA creada automáticamente con código " + codigoVuelta;

            // 🌟 CORRECCIÓN IDE: Variable local para evitar el warning Null type safety
            ReservationEvent eventVuelta = ReservationEvent.builder()
                    .reservationId(savedReturn.getId()).eventType("RESERVATION_CREATED")
                    .description(descEvento).triggeredBy("API_SYSTEM").build();
            reservationEventRepository.save(eventVuelta);
        }

        return savedReservations;
    }

    // 🗑️ BAJA LOGICA: Modifica el estado a CANCELLED (Mantiene historial operativo para el Bot/Panel)
    @Transactional
    public void cancelReservation(UUID id, String triggeredBy) {
        reservationRepository.findById(id).ifPresent(reservation -> {
            reservation.setStatus("CANCELLED");
            reservationRepository.saveAndFlush(reservation);

            ReservationEvent cancelEvent = ReservationEvent.builder()
                    .reservationId(reservation.getId())
                    .eventType("RESERVATION_CANCELLED")
                    .description("Reserva " + reservation.getReservationCode() + " dada de baja por el sistema.")
                    .triggeredBy(triggeredBy)
                    .build();
            reservationEventRepository.save(cancelEvent);
        });
    }

    // 🔄 MODIFICACIÓN GENERAL: Permite re-calendarizar o ajustar datos desde el Bot o el Formulario
    @Transactional
    public Reservation updateReservation(UUID id, Reservation updatedData, String triggeredBy) {
        return reservationRepository.findById(id).map(reservation -> {
            StringBuilder auditoriaDesc = new StringBuilder("Campos modificados: ");
            LocalDate fechaCentinela = LocalDate.of(2099, 12, 31);

            // Si se modifica o confirma la fecha de viaje (cierra la Vuelta Abierta)
            if (updatedData.getTravelDate() != null && !updatedData.getTravelDate().equals(reservation.getTravelDate())) {
                auditoriaDesc.append(String.format("[Fecha: %s -> %s] ", reservation.getTravelDate(), updatedData.getTravelDate()));
                reservation.setTravelDate(updatedData.getTravelDate());
                
                // Si deja de ser fecha centinela, saneamos la nota descriptiva
                if (!updatedData.getTravelDate().equals(fechaCentinela) && reservation.getNotes() != null) {
                    reservation.setNotes(reservation.getNotes().replace("🛑 VUELTA ABIERTA - Pendiente confirmar fecha.", "🔄 Vuelta agendada:"));
                }
            }

            // Cambios de logística básicos
            if (updatedData.getPickupAddress() != null) reservation.setPickupAddress(updatedData.getPickupAddress());
            if (updatedData.getPassengerCount() != null) reservation.setPassengerCount(updatedData.getPassengerCount());
            if (updatedData.getCompanionNames() != null) reservation.setCompanionNames(updatedData.getCompanionNames());
            if (updatedData.getAmount() != null) reservation.setAmount(updatedData.getAmount());
            
            // Gestión de estados de pago
            if (updatedData.getPaymentVerified() != null) {
                reservation.setPaymentVerified(updatedData.getPaymentVerified());
                if (Boolean.TRUE.equals(updatedData.getPaymentVerified())) {
                    reservation.setStatus("CONFIRMED");
                }
            }
            if (updatedData.getStatus() != null) reservation.setStatus(updatedData.getStatus());
            if (updatedData.getNotes() != null) reservation.setNotes(updatedData.getNotes());

            Reservation saved = reservationRepository.saveAndFlush(reservation);

            ReservationEvent updateEvent = ReservationEvent.builder()
                    .reservationId(saved.getId())
                    .eventType("RESERVATION_UPDATED")
                    .description(auditoriaDesc.toString())
                    .triggeredBy(triggeredBy)
                    .build();
            reservationEventRepository.save(updateEvent);

            return saved;
        }).orElseThrow(() -> new IllegalArgumentException("No se encontró la reserva con ID: " + id));
    }
}