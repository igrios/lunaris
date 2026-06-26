package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationViewController {

    private final PassengerRepository passengerRepository;
    private final LocalityRepository localityRepository;
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final PricingAndScheduleService pricingAndScheduleService;

    @GetMapping("/new")
    public String newReservation(Model model) {
        model.addAttribute("reservation", new CreateReservationForm());
        
        // 🎯 Usamos el método filtrado para traer solo los pueblos con tarifas comerciales activas
        var localidadesConTarifa = localityRepository.findLocalitiesWithFares();
        
        model.addAttribute("origenes", localidadesConTarifa);
        model.addAttribute("destinos", localidadesConTarifa);
        
        return "reservation-form";
    }

    @PostMapping("/new")
    public String createReservation(
            @Valid @ModelAttribute("reservation") CreateReservationForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            // Si hay errores de validación, volvemos a inyectar la lista filtrada
            var localidadesConTarifa = localityRepository.findLocalitiesWithFares();
            model.addAttribute("origenes", localidadesConTarifa);
            model.addAttribute("destinos", localidadesConTarifa);
            return "reservation-form";
        }

        Passenger passenger = Passenger.builder()
                .firstName(form.getFirstName())
                .lastName(form.getLastName())
                .phone(form.getPhone())
                .cuil(form.getCuil())
                .build();

        passenger = passengerRepository.save(passenger);

        // Monto unificado con el bot y el alta REST.
        var computedAmount = pricingAndScheduleService.calculateReservationAmount(
                form.getPickupLocality(),
                form.getDestination(),
                form.getRoundTrip(),
                form.getPassengerCount() != null ? form.getPassengerCount() : 1);

        // 🕒 Horario en el mismo formato que el bot, para que la agenda lo muestre igual.
        // Mantenemos las observaciones del operador a continuación, separadas por " | ".
        String schedule = (form.getDepartureSchedule() != null && !form.getDepartureSchedule().isBlank())
                ? form.getDepartureSchedule().trim()
                : "03:00 AM";
        if (Boolean.TRUE.equals(form.getRoundTrip()) && form.getReturnDate() == null) {
            schedule += " (Abierta)";
        }
        String notes = schedule;
        if (form.getNotes() != null && !form.getNotes().isBlank()) {
            notes += " | " + form.getNotes().trim();
        }

        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .travelDate(form.getTravelDate())
                .pickupLocality(form.getPickupLocality())
                .pickupAddress(form.getPickupAddress())
                .destination(form.getDestination())
                .roundTrip(Boolean.TRUE.equals(form.getRoundTrip()))
                .returnDate(form.getReturnDate())
                .paymentVerified(Boolean.TRUE.equals(form.getPaymentVerified()))
                .amount(computedAmount)
                .notes(notes)
                .passengerCount(form.getPassengerCount() != null ? form.getPassengerCount() : 1)
                .companionNames(form.getCompanionNames())
                .build();

        reservationService.saveReservationFlow(reservation);

        return "redirect:/agenda";
    }

    // 👥 VISTA WEB: Renderiza el panel HTML de pasajeros con link directo a WhatsApp
    @GetMapping("/passengers-panel")
    public String listPassengersPanel(Model model) {
        List<Passenger> todosLosPasajeros = passengerRepository.findAll();
        model.addAttribute("pasajeros", todosLosPasajeros);
        return "passengers"; // Apunta al archivo templates/passengers.html
    }

    // 🗑️ BAJA DESDE EL PANEL DE ADMINISTRACIÓN
    @PostMapping("/delete/{id}")
    public String deleteFromPanel(@PathVariable UUID id) {
        reservationService.cancelReservation(id, "ADMIN_PANEL");
        return "redirect:/agenda";
    }

    // 🤖 BAJA DESDE EL BOT / REST ASÍNCRONO
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteFromBot(@PathVariable UUID id) {
        try {
            reservationService.cancelReservation(id, "BOT_CHAT");
            return ResponseEntity.ok().body(Map.of("status", "CANCELLED"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 🔄 MODIFICACIÓN DESDE EL PANEL DE ADMINISTRACIÓN
    @PostMapping("/update/{id}")
    public String updateFromPanel(@PathVariable UUID id, @ModelAttribute Reservation updatedData) {
        reservationService.updateReservation(id, updatedData, "ADMIN_PANEL");
        return "redirect:/agenda";
    }

    // 🤖 MODIFICACIÓN DESDE EL BOT / REST ASÍNCRONO
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateFromBot(@PathVariable UUID id, @RequestBody Reservation updatedData) {
        try {
            Reservation result = reservationService.updateReservation(id, updatedData, "BOT_CHAT");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 🛑 VISTA WEB: Muestra la pantalla de pasajes con Vuelta Abierta bajo /reservations/vueltas-abiertas
    @GetMapping("/vueltas-abiertas")
    public String listOpenReturns(Model model) {
        java.time.LocalDate fechaCentinela = java.time.LocalDate.of(2099, 12, 31);
        List<Reservation> abiertas = reservationRepository.findByTravelDate(fechaCentinela);
        model.addAttribute("vueltasAbiertas", abiertas);
        return "vueltas-abiertas";
    }
}
