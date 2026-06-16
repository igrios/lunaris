package com.lunaris.ansenuza.domain.model.service; // Tu subpaquete actual

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// 🔍 REVISÁ QUE ESTOS IMPORTS COINCIDAN CON TUS CARPETAS REALES:
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

    // 🕒 CÁLCULO DE HORARIO DINÁMICO DESDE POSTGRES (Salida base Morteros 03:40 AM)
    public String calculatePickupTime(String localityName) {
        return localityRepository.findByName(localityName)
                .map(locality -> {
                    LocalTime baseTime = LocalTime.of(3, 40);
                    // Leemos los minutos desde la columna de la base de datos
                    int minutes = locality.getMinutesFromOrigin(); 
                    return baseTime.plusMinutes(minutes).format(DateTimeFormatter.ofPattern("HH:mm")) + " hs";
                })
                .orElse("04:00 hs (A coor.)");
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
                                .orElse(150); // Distancia genérica por las dudas
                        
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