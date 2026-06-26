package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class PricingAndScheduleService {

    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;
    private final BusinessParameterRepository businessParameterRepository;
    private final ReservationRepository reservationRepository; 

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<String, Integer> MINUTES_VUELTA_FROM_HUB = new HashMap<>();

    static {
        MINUTES_VUELTA_FROM_HUB.put("la puerta", 0);
        MINUTES_VUELTA_FROM_HUB.put("marull", 20);
        MINUTES_VUELTA_FROM_HUB.put("balnearia", 35);
        MINUTES_VUELTA_FROM_HUB.put("miramar", 50);
        MINUTES_VUELTA_FROM_HUB.put("freyre", 65);
        MINUTES_VUELTA_FROM_HUB.put("porteña", 85);
        MINUTES_VUELTA_FROM_HUB.put("brinkmann", 100);
        MINUTES_VUELTA_FROM_HUB.put("morteros", 115);
        MINUTES_VUELTA_FROM_HUB.put("suardi", 140);
        MINUTES_VUELTA_FROM_HUB.put("san guillermo", 160);
    }

    /**
     * ⏱️ Calcula dinámicamente el horario basándose en la ocupación física o el tipo de tramo.
     */
    public String calculateEstimatedPickupTime(String localityName, String baseTimeStr, boolean isReturn, LocalDate travelDate) {
        if (localityName == null) return baseTimeStr + " hs";
        
        LocalTime baseTime = LocalTime.parse(baseTimeStr.trim(), TIME_FORMATTER);

        // Controlamos el bloque de regresos o tramo de las 08:00 AM desde Córdoba
        if (isReturn || "08:00".equals(baseTimeStr.trim())) {
            String key = localityName.trim().toLowerCase();
            int minutesFromHub = MINUTES_VUELTA_FROM_HUB.getOrDefault(key, 0);

            int pasajerosRegreso = reservationRepository.countPassengersByReturnDateAndNotesContaining(
                    travelDate != null ? travelDate : LocalDate.now(), "08:00 AM");
            
            if (pasajerosRegreso <= 8) {
                return baseTime.plusMinutes(minutesFromHub).format(TIME_FORMATTER) + " hs";
            } else {
                int delayPorDobleViaje = 45; 
                log.warn("Capacidad excedida para el retorno. Retorno activado con demora.");
                LocalTime horarioConRetorno = baseTime.plusMinutes(minutesFromHub).plusMinutes(delayPorDobleViaje);
                return horarioConRetorno.format(TIME_FORMATTER) + " hs (Demorado por Alta Demanda)";
            }
        } else {
            // Ida tradicional de la madrugada (03:00 AM)
            return localityRepository.findByName(localityName)
                    .map(locality -> {
                        LocalTime startTime = LocalTime.of(3, 0); 
                        int minutesFromOrigin = locality.getMinutesFromOrigin();
                        return startTime.plusMinutes(minutesFromOrigin).format(TIME_FORMATTER) + " hs";
                    })
                    .orElse(baseTimeStr + " hs");
        }
    }

    /**
     * 💰 PRIORIDAD URGENTE: Calcula el precio aplicando la regla (Tarifa / 2) + 8000 si es SOLO IDA
     */
    public java.math.BigDecimal calculateTripPrice(String localityName, Boolean isRoundTrip, int passengerCount) {
        if (localityName == null || localityName.isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }

        // 1. Buscamos la tarifa completa paramétrica de la base de datos
        java.math.BigDecimal baseFare = fareRepository.findByLocalityNameIgnoreCase(localityName.trim())
                .map(fare -> fare.getAmount()) 
                .orElse(java.math.BigDecimal.ZERO);

        if (baseFare.compareTo(java.math.BigDecimal.ZERO) == 0) {
            log.warn("No se encontró tarifa cargada para la localidad: {}.", localityName);
            return java.math.BigDecimal.ZERO;
        }

        java.math.BigDecimal finalPricePerPassenger;

        // 2. Evaluamos si es "Solo Ida" (isRoundTrip == false)
        if (Boolean.FALSE.equals(isRoundTrip)) {
            // Regla de Negocio: (Tarifa Base / 2) + 8000
            finalPricePerPassenger = baseFare.divide(new java.math.BigDecimal("2"), 2, java.math.RoundingMode.HALF_UP)
                                             .add(new java.math.BigDecimal("8000"));
            log.info("[Tarifa Solo Ida Aplicada] Pueblo: {} | Base original: {} | Con regla aplicada: {}", 
                     localityName, baseFare, finalPricePerPassenger);
        } else {
            // Si es Ida y Vuelta completo, mantiene la tarifa base paramétrica normal
            finalPricePerPassenger = baseFare;
        }

        // 3. Multiplicamos por la cantidad total de asientos requeridos
        return finalPricePerPassenger.multiply(java.math.BigDecimal.valueOf(passengerCount));
    }

    public java.math.BigDecimal calculateTripPrice(String localityName, boolean isRoundTrip, int passengerCount) {
        return calculateTripPrice(localityName, Boolean.valueOf(isRoundTrip), passengerCount);
    }

    /**
     * Calcula el importe total de una reserva tomando la ruta completa.
     *
     * <p>Centraliza la regla de negocio usada por el bot, el formulario web y la API:
     * la localidad "de zona" es la que no corresponde a Córdoba.
     */
    public java.math.BigDecimal calculateReservationAmount(String pickupLocality, String destination,
            Boolean isRoundTrip, int passengerCount) {
        String zoneLocality = resolveZoneLocality(pickupLocality, destination);
        return calculateTripPrice(zoneLocality, isRoundTrip, passengerCount);
    }

    private String resolveZoneLocality(String pickupLocality, String destination) {
        if (pickupLocality == null || pickupLocality.isBlank()) {
            return destination;
        }
        if (destination == null || destination.isBlank()) {
            return pickupLocality;
        }

        return pickupLocality.toLowerCase().contains("córdoba") ? destination : pickupLocality;
    }

    /**
     * ⏱️ REESCRITO COMPATIBILIDAD Y URGENCIA: Corrige el error que clavaba a las 03:00 AM el turno de las 08:00
     */
    public String calculateEstimatedPickupTime(String localityName, String baseTimeStr) {
        if ("08:00".equals(baseTimeStr.trim())) {
            // Redirige dinámicamente usando la fecha de hoy como fallback seguro para calcular el desvío de las 08:00 AM
            return calculateEstimatedPickupTime(localityName, baseTimeStr, false, LocalDate.now());
        }
        
        // Mantiene el fallback de las 03:00 AM para el resto
        return localityRepository.findByName(localityName)
                .map(locality -> LocalTime.of(3, 0).plusMinutes(locality.getMinutesFromOrigin()).format(TIME_FORMATTER) + " hs")
                .orElse(baseTimeStr + " hs");
    }
}
