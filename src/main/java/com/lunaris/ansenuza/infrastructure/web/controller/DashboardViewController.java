package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.GetDailyOperationSummaryUseCase;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardViewController {

    private final GetDailyOperationSummaryUseCase useCase;
    private final ReservationRepository reservationRepository; // 👈 Inyectamos el repositorio para la grilla

    /**
     * 📊 Vista del Dashboard Principal (Original)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DailyOperationSummaryResponse summary = useCase.execute(LocalDate.now());
        model.addAttribute("summary", summary);
        return "dashboard";
    }

    /**
     * 🗃️ 1. LA GRILLA PLANA DE RESERVAS (Evita errores 404 de recursos estáticos)
     */
    @GetMapping("/reservas-panel")
    public String grillaReservasPlana(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        log.info("[Bypass Visual] Renderizando grilla de gestión de pasajeros");
        List<Reservation> reservations = reservationRepository.findAll();

        // 🔍 Buscador global interactivo
        if (search != null && !search.isBlank()) {
            String query = search.trim().toLowerCase();
            reservations = reservations.stream()
                    .filter(r -> (r.getPassenger().getFirstName() != null && r.getPassenger().getFirstName().toLowerCase().contains(query)) ||
                                 (r.getPassenger().getLastName() != null && r.getPassenger().getLastName().toLowerCase().contains(query)) ||
                                 (r.getPassenger().getPhone() != null && r.getPassenger().getPhone().contains(query)) ||
                                 (r.getReservationCode() != null && r.getReservationCode().toLowerCase().contains(query)))
                    .collect(Collectors.toList());
        }

        // 🎛️ Filtro rápido por estado canónico de pago
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            reservations = reservations.stream()
                    .filter(r -> status.equals(r.getStatus()))
                    .collect(Collectors.toList());
        }

        // Cargamos las variables que va a leer tu HTML
        model.addAttribute("reservas", reservations);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentStatus", status != null ? status : "ALL");

        // 🎯 IMPORTANTE: Esto le dice a Spring que busque el archivo "reservations-grid.html" suelto en templates/
        return "reservations-grid"; 
    }

    /**
     * 🎯 2. ACCIÓN DE VERIFICACIÓN EN TÁNDEM (Ida y Vuelta Juntas)
    /**
     * 🎯 2. ACCIÓN DE VERIFICACIÓN EN TÁNDEM (Ida y Vuelta Juntas) - CORREGIDO UUID
     */
    @PostMapping("/reservas-panel/verify-tandem/{id}")
    public String verifyPaymentTandemPlano(@PathVariable("id") String rawId) {
        try {
            log.info("[Tándem] Procesando ID de reserva entrante para UUID: {}", rawId);
            
            // Convertimos la cadena de texto directamente al UUID que exige tu Repositorio
            java.util.UUID uuid = java.util.UUID.fromString(rawId);
            
            Reservation currentRes = reservationRepository.findById(uuid).orElse(null);

            if (currentRes != null) {
                // Buscamos todos los tramos de este mismo pasajero para aprobarlos en lote
                List<Reservation> passengerReservations = reservationRepository.findByPassengerOrderByTravelDateAsc(currentRes.getPassenger());
                List<Reservation> toVerify = passengerReservations.stream()
                        .filter(r -> "PAYMENT_RECEIVED".equals(r.getStatus()) || "PENDING_PAYMENT".equals(r.getStatus()))
                        .toList();

                for (Reservation res : toVerify) {
                    res.setStatus("CONFIRMED");
                    res.setPaymentVerified(true);
                    reservationRepository.saveAndFlush(res);
                }
                log.info("[Tándem] Éxito. Se verificaron {} tramos para el pasajero.", toVerify.size());
            }
        } catch (IllegalArgumentException e) {
            log.error("[Tándem] La cadena provista no es un UUID válido: {}", rawId);
        } catch (Exception e) {
            log.error("[Tándem] Error al intentar verificar tramos: ", e);
        }
        return "redirect:/reservas-panel";
    }

@PostMapping("/reservas-panel/cancel/{id}")
    public String cancelFromGridPlano(
            @PathVariable("id") String rawId,
            com.lunaris.ansenuza.domain.model.service.ReservationService reservationService) {
        try {
            log.info("[Baja] Solicitando cancelación desde la grilla para ID (UUID): {}", rawId);
            
            // Convertimos el texto del HTML directamente al java.util.UUID que requiere tu base de datos
            java.util.UUID uuid = java.util.UUID.fromString(rawId);
            
            // Buscamos la reserva por su UUID y le pasamos ese mismo UUID al servicio de cancelación
            reservationRepository.findById(uuid).ifPresent(res -> 
                reservationService.cancelReservation(uuid, "OPERADOR_PANEL") // 👈 CORREGIDO: Viaja el UUID directo
            );
            
            log.info("[Baja] Reserva cancelada exitosamente y butaca liberada en Postgres.");
        } catch (IllegalArgumentException e) {
            log.error("[Baja] El ID enviado no es un UUID válido: {}", rawId);
        } catch (Exception e) {
            log.error("[Baja] Error al cancelar la reserva desde el panel: ", e);
        }
        return "redirect:/reservas-panel";
    }

}