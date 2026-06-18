package com.lunaris.ansenuza.domain.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class PricingAndScheduleService {

    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;
    private final BusinessParameterRepository businessParameterRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // 🔄 TABLA DE MINUTOS DE VUELTA: Reparto hacia los pueblos partiendo desde el Hub de La Puerta
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

    // 🕒 1. Horario de retiro (Ida arranca a las 03:00 en San Guillermo)
    public String calculatePickupTime(String localityName) {
        if (localityName == null) return "03:00 hs";
        
        return localityRepository.findByName(localityName)
                .map(locality -> {
                    LocalTime startTime = LocalTime.of(3, 0); 
                    int minutes = locality.getMinutesFromOrigin(); 
                    return startTime.plusMinutes(minutes).format(TIME_FORMATTER) + " hs";
                })
                .orElse("03:00 hs (A coor.)");
    }

    // ⏱️ 2. Sobrecarga rápida
    public String calculateEstimatedPickupTime(String localityName, String baseTimeStr) {
        return calculateEstimatedPickupTime(localityName, baseTimeStr, false);
    }

    // ⏱️ 3. Inteligencia de ruta (Detecta automáticamente el regreso de las 08:00 hs)
    public String calculateEstimatedPickupTime(String localityName, String baseTimeStr, boolean isReturn) {
        if (localityName == null) return baseTimeStr + " hs";
        
        LocalTime baseTime = LocalTime.parse(baseTimeStr, TIME_FORMATTER);

        if (isReturn || "08:00".equals(baseTimeStr.trim())) {
            String key = localityName.trim().toLowerCase();
            int minutesFromHub = MINUTES_VUELTA_FROM_HUB.getOrDefault(key, 0);
            return baseTime.plusMinutes(minutesFromHub).format(TIME_FORMATTER) + " hs";
        } else {
            return localityRepository.findByName(localityName)
                    .map(locality -> {
                        LocalTime startTime = LocalTime.of(3, 0); 
                        int minutesFromOrigin = locality.getMinutesFromOrigin();
                        return startTime.plusMinutes(minutesFromOrigin).format(TIME_FORMATTER) + " hs";
                    })
                    .orElse(baseTimeStr + " hs");
        }
    }

    // 💵 MOTOR DE PRECIOS DINÁMICO COMPLETAMENTE BLINDADO
    public BigDecimal calculateTripPrice(String localityName, boolean isRoundTrip, int passengerCount) {
        
        // Agregamos un log para ver en la consola de la Lenovo qué te está mandando el bot realmente
        log.info("Calculando precio para: {}, ¿Es Ida y Vuelta?: {}, Pasajeros: {}", localityName, isRoundTrip, passengerCount);

        // 1. Buscamos el monto base en la tabla 'fares'
        BigDecimal basePriceRoundTrip = fareRepository.findByLocalityName(localityName)
                .map(fare -> fare.getAmount())
                .orElseThrow(() -> new IllegalArgumentException("no hay tarifa para esa ciudad"));

        if (basePriceRoundTrip == null) {
            throw new IllegalArgumentException("no hay tarifa para esa ciudad");
        }

        BigDecimal finalPricePerPassenger;

        // 2. Ejecutamos la división estricta de tramos
        if (isRoundTrip) {
            // Si el bot pide Ida y Vuelta, va el monto entero de la base (Ej: Suardi = 99000)
            finalPricePerPassenger = basePriceRoundTrip;
        } else {
            // Solo ida: (Monto / 2) + extra parametrizado (Ej: Suardi = 49500 + 8000 = 57500)
            BigDecimal halfPrice = basePriceRoundTrip.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            
            BigDecimal fixedOneWaySurcharge = businessParameterRepository.findByParameterKey("ONE_WAY_EXTRA_AMOUNT")
                    .map(param -> new BigDecimal(param.getParameterValue()))
                    .orElse(new BigDecimal("8000"));

            finalPricePerPassenger = halfPrice.add(fixedOneWaySurcharge);
        }

        // 3. Multiplicamos por la cantidad real de asientos físicos de la reserva
        BigDecimal totalPrice = finalPricePerPassenger.multiply(new BigDecimal(passengerCount));
        log.info("Precio final calculado enviado al bot: {}", totalPrice);
        
        return totalPrice;
    }
}