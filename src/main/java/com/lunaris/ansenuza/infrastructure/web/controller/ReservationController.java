package com.lunaris.ansenuza.infrastructure.web.controller;

import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.ReservationCreateDTO;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/bot/monitor")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final PassengerRepository passengerRepository;

    @PostMapping("/cargar-reserva")
    public String saveReservation(@ModelAttribute("reservation") ReservationCreateDTO dto) {
        // 1. Buscamos o creamos el Pasajero usando su teléfono de forma unificada
        String telefonoClean = dto.getPhone().trim();

        Passenger passenger = passengerRepository.findByPhone(telefonoClean)
                .orElseGet(() -> {
                    Passenger newPassenger = new Passenger();
                    newPassenger.setPhone(telefonoClean);
                    newPassenger.setCurrentBalance(BigDecimal.ZERO);
                    return newPassenger;
                });

        // Asignamos Nombre y Apellido de forma separada
        passenger.setFirstName(dto.getFirstName().trim());
        passenger.setLastName(dto.getLastName().trim());
        
        if (dto.getCuil() != null && !dto.getCuil().isBlank()) {
            passenger.setCuil(dto.getCuil());
        }
        passengerRepository.saveAndFlush(passenger);

        // 2. Mapeamos los datos del DTO a la entidad de dominio de Reserva
        Reservation reservation = new Reservation();
        reservation.setPassenger(passenger);
        reservation.setPickupLocality(dto.getPickupLocality());
        reservation.setPickupAddress(dto.getPickupAddress());
        reservation.setDestination(dto.getDestination());
        reservation.setTravelDate(dto.getTravelDate());
        reservation.setRoundTrip(dto.getRoundTrip() != null ? dto.getRoundTrip() : false);
        reservation.setReturnDate(dto.getReturnDate());
        
        // Asignamos el horario de salida (departure_schedule)
        reservation.setDepartureSchedule(dto.getDepartureSchedule());
        
        reservation.setPassengerCount(dto.getPassengerCount() != null ? dto.getPassengerCount() : 1);
        
        // ✅ SOLUCIÓN AL ERROR DE COMPILACIÓN:
        // Soportamos de manera segura tanto si el DTO tiene List<String> como si tiene String
        Object companionObj = dto.getCompanionNames();
        if (companionObj instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) companionObj;
            if (!list.isEmpty()) {
                java.util.StringJoiner joiner = new java.util.StringJoiner(", ");
                for (Object item : list) {
                    if (item != null) joiner.add(item.toString());
                }
                reservation.setCompanionNames(joiner.toString());
            } else {
                reservation.setCompanionNames(null);
            }
        } else if (companionObj instanceof String) {
            reservation.setCompanionNames((String) companionObj);
        } else {
            reservation.setCompanionNames(null);
        }
        
        reservation.setNotes(dto.getNotes());

        // Verificación de Pago y Estado Inicial
        reservation.setPaymentVerified(dto.getPaymentVerified() != null ? dto.getPaymentVerified() : false);
        reservation.setStatus(Boolean.TRUE.equals(reservation.getPaymentVerified()) ? "CONFIRMED" : "PENDING_PAYMENT");
        
        // Seteamos el costo inicial estimativo
        reservation.setAmount(BigDecimal.ZERO); 

        // 3. Procesamos la reserva a través de tu lógica transaccional de negocio
        reservationService.saveReservationFlow(reservation);

        // Redirigimos de vuelta al monitor del bot
        return "redirect:/admin/bot/monitor?success=true";
    }
}