package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.lunaris.ansenuza.application.usecase.GetDailyOperationSummaryUseCase;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Reservation;
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

    /**
     * 📊 Vista del Dashboard Principal (Original)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DailyOperationSummaryResponse summary = useCase.execute(LocalDate.now());
        model.addAttribute("summary", summary);

        // 💰 Ingreso de dinero (mismo criterio que el panel de Facturación: sin duplicar el tramo de vuelta)
        LocalDate today = LocalDate.now();
        model.addAttribute("ingresoHoy", reservationRepository.sumConfirmedIncomeBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        model.addAttribute("ingresoMes", reservationRepository.sumConfirmedIncomeBetween(
                today.withDayOfMonth(1).atStartOfDay(), today.withDayOfMonth(1).plusMonths(1).atStartOfDay()));

        return "dashboard";
    }

    /**
     * 🗃️ 1. LA GRILLA PLANA DE RESERVAS (Filtrado por Status Ordinario)
     */
    @GetMapping("/reservas-panel")
    public String grillaReservasPlana(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        log.info("[Bypass Visual] Cargando grilla ordinaria optimizada por estados");
        List<Reservation> reservations = reservationRepository.findAll();

        // 🎛️ Regla de Ocultamiento de Cancelados:
        if ("CANCELLED".equals(status)) {
            // Si eligen "Cancelados", mostramos exclusivamente los que tienen estado CANCELLED
            reservations = reservations.stream()
                    .filter(r -> "CANCELLED".equals(r.getStatus()))
                    .collect(Collectors.toList());
        } else {
            // REGLA CLAVE: Si están en "Todos" o cualquier otro filtro, ESCONDEMOS los cancelados
            reservations = reservations.stream()
                    .filter(r -> !"CANCELLED".equals(r.getStatus()))
                    .collect(Collectors.toList());
            
            // Si además eligieron un sub-estado específico (ej: PENDING_PAYMENT)
            if (status != null && !status.isBlank() && !"ALL".equals(status)) {
                reservations = reservations.stream()
                        .filter(r -> status.equals(r.getStatus()))
                        .collect(Collectors.toList());
            }
        }

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

        model.addAttribute("reservas", reservations);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentStatus", status != null ? status : "ALL");

        return "reservations-grid"; 
    }

    /**
     * 🎯 2. ACCIÓN DE VERIFICACIÓN EN TÁNDEM (Ida y Vuelta Juntas)
     */
    @PostMapping("/reservas-panel/verify-tandem/{id}")
    public String verifyPaymentTandemPlano(@PathVariable("id") String rawId) {
        try {
            log.info("[Tándem] Procesando ID de reserva entrante para UUID: {}", rawId);
            UUID uuid = UUID.fromString(rawId);
            Reservation currentRes = reservationRepository.findById(uuid).orElse(null);

            if (currentRes != null) {
                List<Reservation> passengerReservations = reservationRepository.findByPassengerOrderByTravelDateAsc(currentRes.getPassenger());
                List<Reservation> toVerify = passengerReservations.stream()
                        .filter(r -> !"CANCELLED".equals(r.getStatus())) // Ignoramos tramos dados de baja
                        .filter(r -> "PAYMENT_RECEIVED".equals(r.getStatus()) || "PENDING_PAYMENT".equals(r.getStatus()))
                        .toList();

                for (Reservation res : toVerify) {
                    res.setStatus("CONFIRMED");
                    res.setPaymentVerified(true);
                    res.setPaymentConfirmedAt(LocalDateTime.now());
                    reservationRepository.saveAndFlush(res);
                }
                log.info("[Tándem] Éxito. Se verificaron {} tramos activos para el pasajero.", toVerify.size());
            }
        } catch (IllegalArgumentException e) {
            log.error("[Tándem] La cadena provista no es un UUID válido: {}", rawId);
        } catch (Exception e) {
            log.error("[Tándem] Error al intentar verificar tramos: ", e);
        }
        return "redirect:/reservas-panel";
    }

    /**
     * ❌ 3. BAJA POR ESTADO DIRECTA (Pasa a CANCELLED y se oculta automáticamente)
     */
    @PostMapping("/reservas-panel/cancel/{id}")
    public String cancelFromGridPlano(@PathVariable("id") String rawId) {
        try {
            log.info("[Baja por Estado] Pasando a CANCELLED la reserva ID: {}", rawId);
            UUID uuid = UUID.fromString(rawId);
            
            reservationRepository.findById(uuid).ifPresent(res -> {
                res.setStatus("CANCELLED"); // 👈 Cambiamos el estado clásico
                reservationRepository.saveAndFlush(res); // 👈 Guardamos directo en Postgres
                log.info("[Baja por Estado] Éxito. Guardado y ocultado de la vista general.");
            });
            
        } catch (IllegalArgumentException e) {
            log.error("[Baja por Estado] El ID provisto no cumple el estándar de UUID: {}", rawId);
        } catch (Exception e) {
            log.error("[Baja por Estado] Error crítico al procesar la baja: ", e);
        }
        return "redirect:/reservas-panel";
    }

    /**
     * 💵 4. VISTA DE TARIFAS VIGENTES desde Postgres
     */
    @GetMapping("/fares")
    public String verTarifasComerciales(Model model) {
        log.info("[Tarifas] Extrayendo lista de precios comerciales desde tu repositorio real");
        List<Fare> tarifas = fareRepository.findAll();
        model.addAttribute("tarifas", tarifas);
        return "fares";
    }
}