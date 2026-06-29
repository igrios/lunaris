package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
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
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Slf4j    
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

        var computedAmount = pricingAndScheduleService.calculateReservationAmount(
                form.getPickupLocality(),
                form.getDestination(),
                form.getRoundTrip(),
                form.getPassengerCount() != null ? form.getPassengerCount() : 1);

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
        return "passengers";
    }

    // 🗑️ BAJA DESDE EL PANEL DE ADMINISTRACIÓN (Maneja redirección dinámica por origen)
    // 🗑️ BAJA CONTROLADA PROPORCIONAL (Manejo Seguro de BigDecimal)
    @PostMapping("/delete/{id}")
    public String deleteFromPanel(
            @PathVariable(value = "id") UUID id, 
            @RequestParam(value = "source", defaultValue = "agenda") String source) {
        
        Reservation original = reservationRepository.findById(id).orElse(null);
        
        if (original != null) {
            LocalDate sentinelDate = LocalDate.of(2099, 12, 31);
            boolean isOpenReturn = original.getTravelDate() != null && original.getTravelDate().equals(sentinelDate);

            // Caso Crítico: Cancelación parcial de una butaca dentro de un grupo en Vueltas Abiertas
            if (isOpenReturn && original.getPassengerCount() != null && original.getPassengerCount() > 1 && "vueltas".equals(source)) {
                int asientosAntes = original.getPassengerCount();
                
                // 💰 1. Calculamos estrictamente el valor de UNA Sola Butaca (Monto Total / Cantidad Asientos)
                java.math.BigDecimal montoTotal = original.getAmount() != null ? original.getAmount() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal valorUnaButaca = montoTotal.divide(
                        java.math.BigDecimal.valueOf(asientosAntes), 2, java.math.RoundingMode.HALF_UP);
                
                // 📉 2. Restamos 1 asiento y descontamos su valor proporcional de la reserva base
                original.setPassengerCount(asientosAntes - 1);
                original.setAmount(montoTotal.subtract(valorUnaButaca));
                reservationRepository.saveAndFlush(original);
                
                // 💳 3. Le acreditamos ÚNICAMENTE el valor de esa butaca en la Cuenta Corriente del Titular
                var pasajero = original.getPassenger();
                if (pasajero != null) {
                    java.math.BigDecimal saldoActual = pasajero.getCurrentBalance() != null ? pasajero.getCurrentBalance() : java.math.BigDecimal.ZERO;
                    pasajero.setCurrentBalance(saldoActual.add(valorUnaButaca));
                    passengerRepository.save(pasajero);
                }
                
                log.info("[Baja Parcial] Se canceló 1 asiento de {}. Reintegro a billetera: ${}. Quedan {} asientos.", 
                        asientosAntes, valorUnaButaca, original.getPassengerCount());
            } else {
                // Caso Ordinario: Si le queda un solo asiento o viene de la agenda general, se da de baja completa
                reservationService.cancelReservation(id, "ADMIN_PANEL");
            }
        }
        
        if ("vueltas".equals(source)) {
            return "redirect:/reservations/vueltas-abiertas";
        }
        return "redirect:/agenda";
    }

    // 📅 ENDPOINT DE ACTUALIZACIÓN CON DESGLOSE FÍSICO (Manejo estricto de BigDecimal)
    @PostMapping("/update/{id}")
    public String updateFromPanel(
            @PathVariable(value = "id") UUID id, 
            @RequestParam(value = "travelDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(value = "pickupAddress", required = false) String pickupAddress,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "source", defaultValue = "agenda") String source) {
        
        Reservation original = reservationRepository.findById(id).orElse(null);
        
        if (original != null) {
            LocalDate sentinelDate = LocalDate.of(2099, 12, 31);
            boolean isOpenReturn = original.getTravelDate() != null && original.getTravelDate().equals(sentinelDate);

            // Caso Crítico: Si viene de Vueltas Abiertas (fecha centinela 2099-12-31) y agrupa a más de 1 persona
            if (isOpenReturn && original.getPassengerCount() != null && original.getPassengerCount() > 1) {
                int asientosOriginales = original.getPassengerCount();
                
                // 💰 Manejo seguro de dinero con BigDecimal para la división proporcional
                java.math.BigDecimal montoTotal = original.getAmount() != null ? original.getAmount() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal montoProporcional = montoTotal.divide(
                        java.math.BigDecimal.valueOf(asientosOriginales), 2, java.math.RoundingMode.HALF_UP);
                
                // A. Reducimos la capacidad y restamos el monto proporcional en la reserva base
                original.setPassengerCount(asientosOriginales - 1);
                original.setAmount(montoTotal.subtract(montoProporcional));
                reservationRepository.saveAndFlush(original);
                
                // B. Creamos el registro físico nuevo para el asiento individualizado de forma pura
                Reservation tramoIndependiente = new Reservation();
                tramoIndependiente.setPassenger(original.getPassenger());
                tramoIndependiente.setTravelDate(travelDate);
                tramoIndependiente.setPickupLocality(original.getPickupLocality());
                tramoIndependiente.setPickupAddress(pickupAddress != null && !pickupAddress.isBlank() ? pickupAddress : original.getPickupAddress());
                tramoIndependiente.setDestination(original.getDestination());
                tramoIndependiente.setAmount(montoProporcional);
                tramoIndependiente.setPassengerCount(1);
                tramoIndependiente.setStatus("CONFIRMED");
                tramoIndependiente.setRoundTrip(true);
                tramoIndependiente.setPaymentVerified(true);
                tramoIndependiente.setReturnDate(travelDate);
                tramoIndependiente.setNotes(original.getNotes() != null ? original.getNotes() + " | Split Físico" : "Split Físico");

                if (original.getReservationCode() != null) {
                    tramoIndependiente.setReservationCode(original.getReservationCode().replace("-VUELTA", "") + "-IND-" + System.currentTimeMillis() % 1000);
                } else {
                    tramoIndependiente.setReservationCode("VTA-IND-" + System.currentTimeMillis() % 1000);
                }
                
                reservationService.saveReservationFlow(tramoIndependiente);
                
                log.info("[Split Físico] Se dividió 1 asiento para nueva fecha {}. Monto: ${}.", travelDate, montoProporcional);
            } else {
                // Caso Ordinario: Fila única o agenda común
                Reservation updateData = new Reservation();
                if (travelDate != null) updateData.setTravelDate(travelDate);
                if (pickupAddress != null) updateData.setPickupAddress(pickupAddress);
                
                if (status != null) {
                    updateData.setStatus(status);
                    if ("CONFIRMED".equals(status)) {
                        updateData.setPaymentVerified(true);
                    }
                }
                reservationService.updateReservation(id, updateData, "ADMIN_PANEL");
            }
        }
        
        if ("vueltas".equals(source)) {
            return "redirect:/reservations/vueltas-abiertas";
        }
        return "redirect:/agenda";
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