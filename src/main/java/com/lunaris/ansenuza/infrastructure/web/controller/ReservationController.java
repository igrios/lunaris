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
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final PassengerRepository passengerRepository;

    @PostMapping("/new")
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

        // ✅ Corregido: Asignamos Nombre y Apellido por separado usando las propiedades reales de tu clase Passenger
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
        
        // ✅ Corregido: Asignamos el horario usando el campo real que agregamos en la entidad Reservation
        reservation.setDepartureSchedule(dto.getDepartureSchedule());
        
        reservation.setPassengerCount(dto.getPassengerCount() != null ? dto.getPassengerCount() : 1);
        
        // ✅ Corregido: Transformamos List<String> a un String con comas para que sea compatible con el modelo
        if (dto.getCompanionNames() != null && !dto.getCompanionNames().isEmpty()) {
            String companionsString = String.join(", ", dto.getCompanionNames());
            reservation.setCompanionNames(companionsString);
        } else {
            reservation.setCompanionNames(null);
        }
        
        reservation.setNotes(dto.getNotes());

        // Verificación de Pago y Estado Inicial
        reservation.setPaymentVerified(dto.getPaymentVerified() != null ? dto.getPaymentVerified() : false);
        reservation.setStatus(Boolean.TRUE.equals(reservation.getPaymentVerified()) ? "CONFIRMED" : "PENDING_PAYMENT");
        
        // Seteamos el costo inicial estimativo (el servicio luego lo dividirá si es RoundTrip)
        reservation.setAmount(BigDecimal.ZERO); 

        // 3. Procesamos la reserva a través de tu lógica transaccional de negocio
        reservationService.saveReservationFlow(reservation);

        return "redirect:/agenda?success=true";
    }
}