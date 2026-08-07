package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.application.conversation.GoogleMapsParameterFormatter;
import com.lunaris.ansenuza.domain.port.in.ResolveEffectiveTripOriginUseCase;
import com.lunaris.ansenuza.domain.port.in.RouteOriginResolution;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminDashboardController {

    private final ReservationRepository reservationRepository;
    private final PricingAndScheduleService scheduleService;
    private final ConversationSessionRepository sessionRepository; // 💬 ¡Inyectamos las sesiones del bot!
    private final ResolveEffectiveTripOriginUseCase resolveEffectiveTripOriginUseCase;

    @GetMapping("/hoja-ruta")
    public String getHojaRuta(@RequestParam(value = "fecha", required = false) String fechaStr,
            @RequestParam(value = "schedule", defaultValue = "03:00") String scheduleBlock, Model model) {
        // 1. Parseamos la fecha elegida o usamos la de hoy por defecto
        LocalDate fecha = (fechaStr == null || fechaStr.isEmpty())
                ? com.lunaris.ansenuza.shared.ArgentinaTime.today()
                : LocalDate.parse(fechaStr);
        
        // 2. Traemos solo las reservas activas, IGNORANDO por completo las canceladas
        RouteOriginResolution originResolution = resolveEffectiveTripOriginUseCase.resolve(fecha, scheduleBlock);
        List<Reservation> reservas = reservationRepository.findByTravelDateAndStatusNot(fecha, "CANCELLED")
                .stream()
                .sorted(dynamicRouteComparator(originResolution))
                .toList();
        
        // 3. Calculamos el total yendo desde la zona de los pueblos hacia Córdoba (filtrado automático)
        int totalYendoDesdeZona = reservas.stream()
                .filter(r -> !"Córdoba".equalsIgnoreCase(r.getPickupLocality()))
                .mapToInt(Reservation::getTotalSeats)
                .sum();

        // 4. Calculamos el total volviendo desde Córdoba hacia el norte
        int totalVolviendoDesdeCba = reservas.stream()
                .filter(r -> "Córdoba".equalsIgnoreCase(r.getPickupLocality()))
                .mapToInt(Reservation::getTotalSeats)
                .sum();
                
        // 5. Contamos de manera segura cuántos pasajeros activos viajan en el turno crítico de las 08:00 AM
        int pasajeros0800 = reservas.stream()
                .filter(r -> r.getNotes() != null && r.getNotes().contains("08:00 AM"))
                .mapToInt(Reservation::getTotalSeats)
                .sum();

        // 6. Traemos las conversaciones reales del bot desde la base de datos
        List<ConversationSession> sesionesChat = sessionRepository.findAll();

        // 7. Inyectamos los datos limpios al modelo de Thymeleaf (respetando las variables sin "data.")
        model.addAttribute("fechaSeleccionada", fecha);
        model.addAttribute("pasajeros0800Count", pasajeros0800);
        model.addAttribute("hubActivado", pasajeros0800 > 4);
        model.addAttribute("reservas", reservas);
        model.addAttribute("totalYendo", totalYendoDesdeZona);
        model.addAttribute("totalVolviendo", totalVolviendoDesdeCba);
        model.addAttribute("sesionesChat", sesionesChat); // 👈 ¡Ahora van las reales!
        addOriginAttributes(model, originResolution);
        model.addAttribute(
                "navigationUrl",
                GoogleMapsParameterFormatter.buildDirectionsUrl(reservas));

        return "admin/hoja-ruta"; 
    }

    static java.util.Comparator<Reservation> dynamicRouteComparator(RouteOriginResolution resolution) {
        return java.util.Comparator.comparingInt((Reservation reservation) -> {
            if ("Córdoba".equalsIgnoreCase(reservation.getPickupLocality())
                    || "Córdoba Capital".equalsIgnoreCase(reservation.getPickupLocality())) return Integer.MAX_VALUE;
            return resolution.minuteOffsets().getOrDefault(reservation.getPickupLocality(), Integer.MAX_VALUE - 1);
        }).thenComparing(Reservation::getRouteSequence,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
    }

    static void addOriginAttributes(Model model, RouteOriginResolution resolution) {
        model.addAttribute("effectiveOrigin", resolution.effectiveOrigin());
        model.addAttribute("originRecalculationMessage", resolution.summary());
        model.addAttribute("routeMinuteOffsets", resolution.minuteOffsets());
        model.addAttribute("selectedScheduleBlock", resolution.scheduleBlock());
    }
}
