package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.lunaris.ansenuza.application.usecase.GetDailyOperationSummaryUseCase;
import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardViewController {

    private final GetDailyOperationSummaryUseCase useCase;
    private final ReservationRepository reservationRepository;
    private final FareRepository fareRepository;
    private final ReservationService reservationService;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    /**
     * 📊 Vista del Dashboard Principal
     */
    @GetMapping({"/dashboard", "/admin/dashboard"})
    public String dashboard(Model model) {
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        DailyOperationSummaryResponse summary = useCase.execute(today);
        model.addAttribute("summary", summary);

        model.addAttribute("ingresoHoy", reservationRepository.sumConfirmedIncomeBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        model.addAttribute("ingresoMes", reservationRepository.sumConfirmedIncomeBetween(
                today.withDayOfMonth(1).atStartOfDay(), today.withDayOfMonth(1).plusMonths(1).atStartOfDay()));

        return "dashboard";
    }

    /**
     * 🗃️ 1. LA GRILLA DE RESERVAS POTENCIADA (Filtrado por Status, Texto y FECHA)
     */
    @GetMapping("/reservas-panel")
    public String grillaReservasPlana(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        log.info("[Grilla] Cargando panel operativo avanzado. Filtro Fecha: {}", date);
        
        List<Reservation> reservations;
        
        if (date != null) {
            reservations = reservationRepository.findByTravelDate(date);
        } else {
            reservations = reservationRepository.findAll();
        }

        // Regla de Ocultamiento de Cancelados
        if ("CANCELLED".equals(status)) {
            reservations = reservations.stream()
                    .filter(r -> r != null && "CANCELLED".equals(r.getStatus()))
                    .collect(Collectors.toList());
        } else {
            reservations = reservations.stream()
                    .filter(r -> r != null && !"CANCELLED".equals(r.getStatus()))
                    .collect(Collectors.toList());
            
            if (status != null && !status.isBlank() && !"ALL".equals(status)) {
                reservations = reservations.stream()
                        .filter(r -> status.equals(r.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        // Buscador global interactivo
        if (search != null && !search.isBlank()) {
            String query = search.trim().toLowerCase();
            reservations = reservations.stream()
                    .filter(r -> r.getPassenger() != null && 
                                 ((r.getPassenger().getFirstName() != null && r.getPassenger().getFirstName().toLowerCase().contains(query)) ||
                                 (r.getPassenger().getLastName() != null && r.getPassenger().getLastName().toLowerCase().contains(query)) ||
                                 (r.getPassenger().getPhone() != null && r.getPassenger().getPhone().contains(query)) ||
                                 (r.getReservationCode() != null && r.getReservationCode().toLowerCase().contains(query))))
                    .collect(Collectors.toList());
        }

        model.addAttribute("reservas", reservations);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        model.addAttribute("currentDate", date);

        return "reservations-grid"; 
    }

    /**
     * 🎯 2. ACCIÓN DE VERIFICACIÓN CONTROLADA INDIVIDUAL (Firma nativa con UUID)
     */
    @PostMapping("/reservas-panel/verify-tandem/{id}")
    public String verifyPaymentTandemPlano(@PathVariable(value = "id") UUID id) {
        try {
            log.info("[Validación] Procesando confirmación individual para UUID: {}", id);
            
            confirmPaymentUseCase.execute(id);
            log.info("[Validación] Éxito. Tramo validado de forma individual.");
            
        } catch (Exception e) {
            log.error("[Validación] Error crítico al verificar el tramo individual: ", e);
        }
        return "redirect:/reservas-panel";
    }

    @PostMapping("/reservations/{id}/verify-payment")
    public String verifyPayment(@PathVariable UUID id) {
        reservationService.verifyPayment(id);
        return "redirect:/reservas-panel";
    }

    /**
     * ❌ 3. BAJA INTEGRADA CON CUENTA CORRIENTE (Firma nativa con UUID)
     */
    @PostMapping("/reservas-panel/cancel/{id}")
    public String cancelFromGridPlano(@PathVariable(value = "id") UUID id) {
        try {
            log.info("[Baja Controlada] Procesando cancelación para UUID: {}", id);
            
            reservationService.cancelReservation(id, "ADMIN_PANEL");
            log.info("[Baja Controlada] Éxito. Saldo impactado en la cuenta del pasajero.");
        } catch (Exception e) {
            log.error("[Baja Controlada] Error crítico en el flujo de cancelación: ", e);
        }
        return "redirect:/reservas-panel";
    }

    /**
     * 💵 4. VISTA DE TARIFAS VIGENTES desde Postgres
     */
    @GetMapping("/fares")
    public String verTarifasComerciales(Model model) {
        List<Fare> tarifas = fareRepository.findAll();
        model.addAttribute("tarifas", tarifas);
        return "fares";
    }
}
