package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.model.TripType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingAndScheduleService {

    private static final List<String> DEPARTURE_BLOCKS = List.of("03:00 AM", "08:00 AM");
    private static final String ONE_WAY_EXTRA_AMOUNT = "ONE_WAY_EXTRA_AMOUNT";
    private static final String PRICE_PER_KM = "PRICE_PER_KM";
    private static final String DEFAULT_FARE = "DEFAULT_FARE";
    private static final java.math.BigDecimal DEFAULT_ONE_WAY_EXTRA = new java.math.BigDecimal("8000");
    private static final java.math.BigDecimal DEFAULT_PRICE_PER_KM = new java.math.BigDecimal("1000");
    private static final java.math.BigDecimal FALLBACK_FARE = new java.math.BigDecimal("100000");

    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;
    private final BusinessParameterRepository businessParameterRepository;
    private final ReservationRepository reservationRepository; 

    @Value("${lunaris.trips.capacity:12}")
    private int tripCapacity = 12;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<String, Integer> MINUTES_VUELTA_FROM_HUB = new HashMap<>();
    private static final Map<String, LocalTime> SECOND_MORNING_SCHEDULE = Map.ofEntries(
            Map.entry("san guillermo", LocalTime.of(7, 20)),
            Map.entry("suardi", LocalTime.of(7, 40)),
            Map.entry("morteros", LocalTime.of(8, 0)),
            Map.entry("brinkmann", LocalTime.of(8, 20)),
            Map.entry("portena", LocalTime.of(8, 40)),
            Map.entry("freyre", LocalTime.of(9, 0)),
            Map.entry("la paquita", LocalTime.of(8, 30)),
            Map.entry("altos de chipion", LocalTime.of(8, 40)),
            Map.entry("balnearia", LocalTime.of(9, 0)),
            Map.entry("miramar", LocalTime.of(9, 10)));

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
            String key = normalizeLocality(localityName);
            LocalTime scheduledTime = "08:00".equals(baseTimeStr.trim())
                    ? SECOND_MORNING_SCHEDULE.getOrDefault(key, baseTime)
                    : baseTime.plusMinutes(MINUTES_VUELTA_FROM_HUB.getOrDefault(key, 0));

            int pasajerosRegreso = reservationRepository.countPassengersByReturnDateAndNotesContaining(
                    travelDate != null ? travelDate : com.lunaris.ansenuza.shared.ArgentinaTime.today(), "08:00 AM");
            
            if (pasajerosRegreso <= 8) {
                return scheduledTime.format(TIME_FORMATTER) + " hs";
            } else {
                int delayPorDobleViaje = 45; 
                log.warn("Capacidad excedida para el retorno. Retorno activado con demora.");
                LocalTime horarioConRetorno = scheduledTime.plusMinutes(delayPorDobleViaje);
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

    private static String normalizeLocality(String localityName) {
        return java.text.Normalizer.normalize(localityName.trim().toLowerCase(),
                        java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    /**
     * 💰 PRIORIDAD URGENTE: Calcula el precio aplicando la regla (Tarifa / 2) + 8000 si es SOLO IDA
     */
    public java.math.BigDecimal calculateTripPrice(String localityName, Boolean isRoundTrip, int passengerCount) {
        if (localityName == null || localityName.isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }

        // 1. Buscamos la tarifa completa paramétrica de la base de datos
        java.math.BigDecimal baseFare = resolveBaseFare(localityName.trim());

        java.math.BigDecimal finalPricePerPassenger;

        // 2. Evaluamos si es "Solo Ida" (isRoundTrip == false)
        if (Boolean.FALSE.equals(isRoundTrip)) {
            java.math.BigDecimal extraOneWayFee = businessParameterRepository
                    .findByParameterKey(ONE_WAY_EXTRA_AMOUNT)
                    .map(parameter -> new java.math.BigDecimal(parameter.getParameterValue()))
                    .orElse(DEFAULT_ONE_WAY_EXTRA);
            finalPricePerPassenger = baseFare.divide(new java.math.BigDecimal("2"), 2, java.math.RoundingMode.HALF_UP)
                                             .add(extraOneWayFee);
            log.info("[Tarifa Solo Ida Aplicada] Pueblo: {} | Base original: {} | Con regla aplicada: {}", 
                     localityName, baseFare, finalPricePerPassenger);
        } else {
            // Si es Ida y Vuelta completo, mantiene la tarifa base paramétrica normal
            finalPricePerPassenger = baseFare;
        }

        // 3. Multiplicamos por la cantidad total de asientos requeridos
        return finalPricePerPassenger.multiply(java.math.BigDecimal.valueOf(passengerCount));
    }

    /** Diferencia a reliquidar cuando una tarifa ida/vuelta termina siendo solo ida. */
    public java.math.BigDecimal calculateOneWaySurcharge(int passengerCount) {
        if (passengerCount <= 0) {
            return java.math.BigDecimal.ZERO;
        }
        return positiveBusinessParameter(ONE_WAY_EXTRA_AMOUNT, DEFAULT_ONE_WAY_EXTRA)
                .multiply(java.math.BigDecimal.valueOf(Math.min(passengerCount, 4)));
    }

    private java.math.BigDecimal resolveBaseFare(String localityName) {
        return fareRepository.findByLocalityNameIgnoreCase(localityName)
                .map(fare -> fare.getAmount())
                .filter(amount -> amount != null && amount.signum() > 0)
                .orElseGet(() -> calculateFallbackFare(localityName));
    }

    private java.math.BigDecimal calculateFallbackFare(String localityName) {
        java.math.BigDecimal pricePerKm = positiveBusinessParameter(
                PRICE_PER_KM, DEFAULT_PRICE_PER_KM);
        java.math.BigDecimal calculatedFare = localityRepository
                .findFirstByNameIgnoreCase(localityName)
                .map(com.lunaris.ansenuza.domain.model.Locality::getKmsToCordoba)
                .filter(kms -> kms > 0)
                .map(kms -> pricePerKm.multiply(java.math.BigDecimal.valueOf(kms)))
                .orElseGet(() -> positiveBusinessParameter(DEFAULT_FARE, FALLBACK_FARE));

        log.warn("No se encontró tarifa explícita para {}. Se utiliza tarifa de respaldo: {}.",
                localityName, calculatedFare);
        return calculatedFare;
    }

    private java.math.BigDecimal positiveBusinessParameter(
            String key, java.math.BigDecimal defaultValue) {
        return businessParameterRepository.findByParameterKey(key)
                .map(parameter -> parameter.getParameterValue())
                .flatMap(value -> {
                    try {
                        java.math.BigDecimal parsed = new java.math.BigDecimal(value);
                        return parsed.signum() > 0
                                ? java.util.Optional.of(parsed)
                                : java.util.Optional.empty();
                    } catch (NumberFormatException exception) {
                        log.warn("Parámetro de negocio {} inválido: {}.", key, value);
                        return java.util.Optional.empty();
                    }
                })
                .orElse(defaultValue);
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

    public java.math.BigDecimal calculateReservationAmount(String pickupLocality, String destination,
            TripType tripType, int passengerCount) {
        boolean fullRoundTripFare = tripType == TripType.ROUND_TRIP || tripType == TripType.OPEN_RETURN;
        return calculateReservationAmount(
                pickupLocality, destination, fullRoundTripFare, passengerCount);
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

    public long countReservedSeats(LocalDate date, String schedule) {
        return reservationRepository.countReservedSeats(date, schedule);
    }

    public List<String> departureSchedules() {
        return DEPARTURE_BLOCKS;
    }

    public int availableSeats(LocalDate date, String schedule) {
        long remainingSeats = (long) tripCapacity - countReservedSeats(date, schedule);
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, remainingSeats));
    }

    /**
     * Bloques de salida compartidos por el bot y la API pública.
     * La disponibilidad corresponde únicamente al tramo de ida y a su fecha de viaje.
     */
    public List<String> availableDepartureSchedules(
            String pickupLocality, String destination, LocalDate travelDate) {
        if (pickupLocality == null || pickupLocality.isBlank() || travelDate == null) {
            return List.of();
        }
        return DEPARTURE_BLOCKS.stream()
                .filter(schedule -> availableSeats(travelDate, schedule) > 0)
                .toList();
    }

    /**
     * ⏱️ REESCRITO COMPATIBILIDAD Y URGENCIA: Corrige el error que clavaba a las 03:00 AM el turno de las 08:00
     */
    public String calculateEstimatedPickupTime(String localityName, String baseTimeStr) {
        if ("08:00".equals(baseTimeStr.trim())) {
            // Redirige dinámicamente usando la fecha de hoy como fallback seguro para calcular el desvío de las 08:00 AM
            return calculateEstimatedPickupTime(
                    localityName, baseTimeStr, false,
                    com.lunaris.ansenuza.shared.ArgentinaTime.today());
        }
        
        // Mantiene el fallback de las 03:00 AM para el resto
        return localityRepository.findByName(localityName)
                .map(locality -> LocalTime.of(3, 0).plusMinutes(locality.getMinutesFromOrigin()).format(TIME_FORMATTER) + " hs")
                .orElse(baseTimeStr + " hs");
    }
}
