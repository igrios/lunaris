package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminDashboardController {

    private final ReservationRepository reservationRepository;
    private final PricingAndScheduleService scheduleService;

    @GetMapping("/hoja-ruta")
    public String getHojaRuta(@RequestParam(value = "fecha", required = false) String fechaStr, Model model) {
        // 1. Parseamos la fecha elegida o usamos la de hoy por defecto
        LocalDate fecha = (fechaStr == null || fechaStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(fechaStr);
        
        // 2. Traemos solo las reservas activas, IGNORANDO por completo las canceladas
        List<Reservation> reservas = reservationRepository.findByTravelDateAndStatusNot(fecha, "CANCELLED");
        
        // 3. Calculamos el total yendo desde la zona de los pueblos hacia Córdoba (filtrado automático)
        int totalYendoDesdeZona = reservas.stream()
                .filter(r -> !"Córdoba".equalsIgnoreCase(r.getPickupLocality()))
                .mapToInt(Reservation::getPassengerCount)
                .sum();

        // 4. Calculamos el total volviendo desde Córdoba hacia el norte
        int totalVolviendoDesdeCba = reservas.stream()
                .filter(r -> "Córdoba".equalsIgnoreCase(r.getPickupLocality()))
                .mapToInt(Reservation::getPassengerCount)
                .sum();
                
        // 5. Contamos de manera segura cuántos pasajeros activos viajan en el turno crítico de las 08:00 AM
        int pasajeros0800 = reservas.stream()
                .filter(r -> r.getNotes() != null && r.getNotes().contains("08:00 AM"))
                .mapToInt(Reservation::getPassengerCount)
                .sum();

        // 6. Inyectamos los datos limpios al modelo de Thymeleaf
        model.addAttribute("fechaSeleccionada", fecha);
        model.addAttribute("pasajeros0800Count", pasajeros0800);
        model.addAttribute("hubActivado", pasajeros0800 > 8);
        model.addAttribute("reservas", reservas);
        model.addAttribute("totalYendo", totalYendoDesdeZona);
        model.addAttribute("totalVolviendo", totalVolviendoDesdeCba);
        
        // Lista vacía temporal para evitar errores de compilación con repositorios de infraestructura
        model.addAttribute("sesionesChat", java.util.Collections.emptyList());

        return "admin/hoja-ruta"; // Renderiza el archivo html unificado
    }
}