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

@Service
@AllArgsConstructor
public class PricingAndScheduleService {

    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;
    private final BusinessParameterRepository businessParameterRepository;

    // 🔄 TABLA DE MINUTOS DE VUELTA: Reparto hacia los pueblos partiendo desde el Hub de La Puerta
    // Esto es necesario porque el sentido del viaje es inverso al origen en Postgres
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

    // 🕒 1. MANTENEMOS TU MÉTODO ORIGINAL (Por si otras partes del sistema lo llaman)
    public String calculatePickupTime(String localityName) {
        return localityRepository.findByName(localityName)
                .map(locality -> {
                    LocalTime baseTime = LocalTime.of(3, 40);
                    int minutes = locality.getMinutesFromOrigin(); 
                    return baseTime.plusMinutes(minutes).format(DateTimeFormatter.ofPattern("HH:mm")) + " hs";
                })
                .orElse("04:00 hs (A coor.)");
    }

    // ⏱️ 2. NUEVO MÉTODO SOBRECARGADO DE CONSULTA RÁPIDA (Ida por defecto de dos parámetros)
    public String calculateEstimatedPickupTime(String localityName, String baseTimeStr) {
        return calculateEstimatedPickupTime(localityName, baseTimeStr, false);
    }

    // ⏱️ 3. INTELIGENCIA DE RUTA COMPLETA (Ida desde Postgres y Regreso desde el Hub de La Puerta)
    public String calculateEstimatedPickupTime(String localityName, String baseTimeStr, boolean isReturn) {
        if (localityName == null) return baseTimeStr + " hs";
        
        // Parseamos la hora base elegida ("03:00" u "08:00" en la ida, o la hora del Hub en la vuelta)
        LocalTime baseTime = LocalTime.parse(baseTimeStr, DateTimeFormatter.ofPattern("HH:mm"));

        if (isReturn) {
            // Regreso: Calculamos partiendo desde el Hub de La Puerta con el mapa estático
            String key = localityName.trim().toLowerCase();
            int minutesFromHub = MINUTES_VUELTA_FROM_HUB.getOrDefault(key, 0);
            return baseTime.plusMinutes(minutesFromHub).format(DateTimeFormatter.ofPattern("HH:mm")) + " hs";
        } else {
            // Ida: Usamos los minutos reales que ya tenés cargados en tu base de datos Postgres
            return localityRepository.findByName(localityName)
                    .map(locality -> {
                        int minutesFromOrigin = locality.getMinutesFromOrigin();
                        return baseTime.plusMinutes(minutesFromOrigin).format(DateTimeFormatter.ofPattern("HH:mm")) + " hs";
                    })
                    .orElse(baseTimeStr + " hs");
        }
    }

    // 💵 MOTOR DE PRECIOS DINÁMICO INTEGRADO
    public BigDecimal calculateTripPrice(String localityName, boolean isRoundTrip, int passengerCount) {
        
        // 1. Cargamos los parámetros globales de tu tabla 'business_parameters'
        BigDecimal fixedOneWaySurcharge = businessParameterRepository.findByParameterKey("ONE_WAY_EXTRA_AMOUNT")
                .map(param -> new BigDecimal(param.getParameterValue()))
                .orElse(new BigDecimal("8000"));

        BigDecimal pricePerKm = businessParameterRepository.findByParameterKey("PRICE_PER_KM")
                .map(param -> new BigDecimal(param.getParameterValue()))
                .orElse(new BigDecimal("1000"));

        // 2. Buscamos si el pueblo tiene tarifa fija cargada en la tabla 'fares'
        BigDecimal basePriceRoundTrip = fareRepository.findByLocalityName(localityName)
                .map(fare -> fare.getAmount()) 
                .orElseGet(() -> {
                        // FALLBACK POR KM: Si no hay tarifa fija, busca los kilómetros en la tabla 'localities' y calcula vivo
                        int kms = localityRepository.findByName(localityName)
                                .map(Locality::getKmsToCordoba)
                                .orElse(150);
                        
                        return pricePerKm.multiply(new BigDecimal(kms)).multiply(new BigDecimal("2"));
                });

        BigDecimal finalPricePerPassenger;

        if (isRoundTrip) {
            finalPricePerPassenger = basePriceRoundTrip;
        } else {
            BigDecimal halfPrice = basePriceRoundTrip.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            finalPricePerPassenger = halfPrice.add(fixedOneWaySurcharge);
        }

        return finalPricePerPassenger.multiply(new BigDecimal(passengerCount));
    }
}