package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lunaris.ansenuza.application.usecase.WaitingListConversionService;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.application.usecase.WaitingListReengagementService;
import org.springframework.web.server.ResponseStatusException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
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
    private final DriverRepository driverRepository;
    private final WhatsAppService whatsAppService;
    private final WaitingListService waitingListService;
    private final WaitingListConversionService waitingListConversionService;
    private final WaitingListReengagementService waitingListReengagementService;

    @GetMapping("/new")
    public String newReservation(Model model) {
        model.addAttribute("reservation", new CreateReservationForm());
        
        // 🎯 Usamos el método filtrado para traer solo los pueblos con tarifas comerciales activas
        var localidadesConTarifa = localityRepository.findLocalitiesWithFares();
        
        model.addAttribute("origenes", localidadesConTarifa);
        model.addAttribute("destinos", localidadesConTarifa);
        
        return "reservation-form";
    }

   /* @PostMapping("/new")
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
 */
    // 👥 VISTA WEB: Renderiza el panel HTML de pasajeros con link directo a WhatsApp
    @GetMapping("/passengers-panel")
    public String listPassengersPanel(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(defaultValue = "all") String waitingListMode,
            Model model) {
        List<Passenger> todosLosPasajeros = passengerRepository.findAll();
        List<com.lunaris.ansenuza.domain.model.WaitingListEntry> waitingEntries = travelDate == null
                ? waitingListService.findAllActiveWaiting()
                : waitingListService.findWaiting(travelDate);
        model.addAttribute("pasajeros", todosLosPasajeros);
        model.addAttribute("waitingListEntries", waitingEntries);
        model.addAttribute("waitingListCount", waitingEntries.size());
        model.addAttribute("waitingListTotal", waitingEntries.size());
        model.addAttribute("waitingListMode", waitingListMode);
        model.addAttribute("selectedTravelDate", travelDate);
        return "passengers";
    }

    @PostMapping("/waiting-list/{id}/convert")
    public String convertWaitingListEntry(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            RedirectAttributes redirectAttributes) {
        try {
            waitingListReengagementService.promote(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Se notificó al pasajero para confirmar y pagar por WhatsApp.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return passengersPanelRedirect(travelDate);
    }

    @PostMapping("/waiting-list/{id}/promote")
    public String promoteWaitingListEntryToBot(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            RedirectAttributes redirectAttributes) {
        try {
            waitingListReengagementService.promote(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Se notificó al pasajero por WhatsApp.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return passengersPanelRedirect(travelDate);
    }

    @PostMapping("/waiting-list/{id}/cancel")
    public String cancelWaitingListEntry(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            RedirectAttributes redirectAttributes) {
        try {
            waitingListConversionService.cancel(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "La entrada fue cancelada.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return passengersPanelRedirect(travelDate);
    }

    private String passengersPanelRedirect(LocalDate travelDate) {
        return travelDate == null
                ? "redirect:/reservations/passengers-panel"
                : "redirect:/reservations/passengers-panel?travelDate=" + travelDate;
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
            boolean isOpenReturn = original.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN
                    || original.getTravelDate() != null && original.getTravelDate().equals(sentinelDate);

            // Caso Crítico: Cancelación parcial de una butaca dentro de un grupo en Vueltas Abiertas
            if (isOpenReturn && original.getPassengerCount() != null && original.getPassengerCount() > 1 && "vueltas".equals(source)) {
                reservationService.cancelOneUnusedReturnSeat(id, "ADMIN_PANEL");
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

@Transactional
@PostMapping("/update/{id}")
public String updateFromPanel(
        @PathVariable(value = "id") UUID id,
        @RequestParam(value = "travelDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
        @RequestParam(value = "departureSchedule", required = false) String departureSchedule,
        @RequestParam(value = "pickupAddress", required = false) String pickupAddress,
        @RequestParam(value = "driverId", required = false) UUID driverId,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "travelStatus", required = false) String rawTravelStatus,
        @RequestParam(value = "cantidadVuelven", defaultValue = "1") int cantidadVuelven,
        @RequestParam(value = "source", defaultValue = "agenda") String source) {
        
    log.info(
            "[Reservation Update] Incoming travelStatus={} for reservationId={}",
            rawTravelStatus, id);
    Reservation.TravelStatus travelStatus = parseTravelStatus(id, rawTravelStatus);
    Reservation original = reservationRepository.findById(id).orElse(null);
    
    if (original != null) {
        if ("COMPLETED".equalsIgnoreCase(original.getStatus())
                || original.getTravelStatus() == Reservation.TravelStatus.COMPLETED) {
            throw new ReservationAlreadyCompletedException();
        }
        Driver assignedDriver = driverId == null ? null : driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Chofer no encontrado: " + driverId));
        LocalDate sentinelDate = LocalDate.of(2099, 12, 31);
        boolean isOpenReturn = original.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN
                || original.getTravelDate() != null && original.getTravelDate().equals(sentinelDate);
        
        if (isOpenReturn) {
            Reservation scheduledReturn;
            if (travelDate == null || departureSchedule == null || departureSchedule.isBlank()) {
                throw new IllegalArgumentException(
                        "Para programar una vuelta abierta se requieren fecha y horario.");
            }
            int asientosOriginales = original.getPassengerCount() != null ? original.getPassengerCount() : 1;
            String normalizedSchedule = normalizeDepartureSchedule(departureSchedule);
            
            // A. Si eligen volver MENOS pasajeros de los que tiene el grupo actualmente (Split por Bloque)
            if (cantidadVuelven < asientosOriginales) {
                java.math.BigDecimal montoTotal = original.getAmount() != null ? original.getAmount() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal valorButacaIndividual = montoTotal.divide(java.math.BigDecimal.valueOf(asientosOriginales), 2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal montoBloqueDesglosado = valorButacaIndividual.multiply(java.math.BigDecimal.valueOf(cantidadVuelven));

                // Restamos el bloque que se va del registro base que se queda en "Vueltas Abiertas"
                original.setPassengerCount(asientosOriginales - cantidadVuelven);
                original.setAmount(montoTotal.subtract(montoBloqueDesglosado));
                reservationRepository.saveAndFlush(original);

                // Generamos la nueva fila física en la base de datos para los pasajeros que sí viajan
                Reservation tramoIndependiente = new Reservation();
                tramoIndependiente.setPassenger(original.getPassenger());
                tramoIndependiente.setTravelDate(travelDate);
                tramoIndependiente.setPickupLocality(original.getPickupLocality());
                tramoIndependiente.setPickupAddress(pickupAddress != null && !pickupAddress.isBlank() ? pickupAddress : original.getPickupAddress());
                tramoIndependiente.setDestination(original.getDestination());
                tramoIndependiente.setAmount(montoBloqueDesglosado);
                tramoIndependiente.setPassengerCount(cantidadVuelven); // Cantidad exacta elegida por Martín
                tramoIndependiente.setStatus("CONFIRMED");
                tramoIndependiente.setRoundTrip(false); // Desactivado para evitar bucles de combo
                tramoIndependiente.setPaymentVerified(true);
                tramoIndependiente.setDriver(assignedDriver);
                tramoIndependiente.setReturnDate(travelDate);
                tramoIndependiente.setDepartureSchedule(normalizedSchedule);
                tramoIndependiente.setTravelStatus(Reservation.TravelStatus.PENDING);
                tramoIndependiente.setNotes(original.getNotes() != null ? original.getNotes() + " | Split Bloque" : "Split Bloque");
                
                String shortTimestamp = String.valueOf(System.currentTimeMillis()).substring(10);
                tramoIndependiente.setReservationCode("VTA-BLK-" + original.getId().toString().substring(0, 4) + "-" + shortTimestamp);
                tramoIndependiente.setBookingGroupCode(resolveBookingGroupCode(original));
                
                scheduledReturn = reservationRepository.saveAndFlush(tramoIndependiente);
                sendOpenReturnConfirmation(tramoIndependiente);
                log.info("[Split Bloque] Se procesó el regreso de {} pasajeros. Quedan {} en espera.", cantidadVuelven, original.getPassengerCount());
            } 
            // B. Si vuelven TODOS los pasajeros que quedaban en el grupo (Cierre definitivo)
            else {
                original.setTravelDate(travelDate);
                if (pickupAddress != null && !pickupAddress.isBlank()) original.setPickupAddress(pickupAddress);
                original.setRoundTrip(false); // Apagamos el flag de combo de raíz
                original.setReturnDate(travelDate);
                original.setDepartureSchedule(normalizedSchedule);
                original.setTravelStatus(Reservation.TravelStatus.PENDING);
                original.setStatus("CONFIRMED");
                original.setPaymentVerified(true);
                original.setDriver(assignedDriver);
                if (status != null && "CONFIRMED".equals(status)) original.setStatus("CONFIRMED");
                
                scheduledReturn = reservationRepository.saveAndFlush(original);
                sendOpenReturnConfirmation(original);
                log.info("[Cierre Grupo] Volvieron los últimos {} pasajeros del grupo.", cantidadVuelven);
            }
            if (travelStatus != null) {
                Reservation travelStatusUpdate = new Reservation();
                travelStatusUpdate.setTravelStatus(travelStatus);
                reservationService.updateReservation(
                        scheduledReturn.getId(), travelStatusUpdate, "ADMIN_PANEL");
            }
        } else {
            // Caso Ordinario para reservas normales de la agenda
            Reservation updateData = new Reservation();
            updateData.setTravelStatus(null);
            if (travelDate != null) updateData.setTravelDate(travelDate);
            if (pickupAddress != null) updateData.setPickupAddress(pickupAddress);
            if (status != null) {
                updateData.setStatus(status);
                if ("CONFIRMED".equals(status)) updateData.setPaymentVerified(true);
            }
            if (travelStatus != null) updateData.setTravelStatus(travelStatus);
            reservationService.updateReservation(id, updateData, "ADMIN_PANEL");
        }
    }
    
    if ("vueltas".equals(source)) {
        return "redirect:/reservations/vueltas-abiertas";
    }
    return "redirect:/agenda";
    }

    static String normalizeDepartureSchedule(String rawSchedule) {
        String schedule = rawSchedule.trim().toUpperCase(java.util.Locale.ROOT);
        if (schedule.endsWith(" AM") || schedule.endsWith(" PM")) {
            return schedule;
        }
        if (schedule.matches("\\d{2}:\\d{2}")) {
            int hour = Integer.parseInt(schedule.substring(0, 2));
            return schedule + (hour < 12 ? " AM" : " PM");
        }
        throw new IllegalArgumentException("Formato de horario inválido: " + rawSchedule);
    }

    private static String resolveBookingGroupCode(Reservation reservation) {
        if (reservation.getBookingGroupCode() != null
                && !reservation.getBookingGroupCode().isBlank()) {
            return reservation.getBookingGroupCode();
        }
        String code = reservation.getReservationCode();
        return code == null ? null : code.replaceFirst("-(IDA|VUELTA)$", "");
    }

    private Reservation.TravelStatus parseTravelStatus(UUID reservationId, String rawTravelStatus) {
        if (rawTravelStatus == null || rawTravelStatus.isBlank()) {
            log.warn(
                    "[Reservation Update] Null or blank travelStatus received. "
                            + "reservationId={}, payload={}",
                    reservationId, rawTravelStatus);
            return null;
        }
        try {
            return Reservation.TravelStatus.valueOf(rawTravelStatus);
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "[Reservation Update] Invalid travelStatus received. "
                            + "reservationId={}, payload={}",
                    reservationId, rawTravelStatus);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado de viaje inválido: " + rawTravelStatus,
                    exception);
        }
    }

    private void sendOpenReturnConfirmation(Reservation reservation) {
        Passenger passenger = reservation.getPassenger();
        if (passenger == null || passenger.getPhone() == null || passenger.getPhone().isBlank()) {
            return;
        }
        String driverName = reservation.getDriver() != null
                ? reservation.getDriver().getFullName()
                : "a confirmar";
        whatsAppService.sendMessage(
                passenger.getPhone(),
                "✅ Tu vuelta quedó confirmada para el " + reservation.getTravelDate()
                        + ". Chofer: " + driverName + "."
                        + " Código de reserva: " + reservation.getReservationCode() + ".");
    }

    // 🛑 VISTA WEB: Muestra la pantalla de pasajes con Vuelta Abierta bajo /reservations/vueltas-abiertas
    @GetMapping("/vueltas-abiertas")
    public String listOpenReturns(Model model) {
        java.time.LocalDate fechaCentinela = java.time.LocalDate.of(2099, 12, 31);
        List<Reservation> abiertas = reservationRepository.findVueltasAbiertasActive(fechaCentinela);
        abiertas = abiertas.stream()
                .sorted(AgendaViewController.dispatchedLastComparator())
                .toList();
        model.addAttribute("vueltasAbiertas", abiertas);
        model.addAttribute("choferes", driverRepository.findByActiveTrue());
        return "vueltas-abiertas";
    }
}
