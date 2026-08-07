package com.lunaris.ansenuza.domain.model.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

public class TripRouteCalculatorService {
    public static final List<String> NORTH_TERMINAL_CORRIDOR = List.of(
            "Arrufó", "Villa Trinidad", "San Guillermo", "Suardi", "Morteros", "Brinkmann",
            "Porteña", "Freyre", "La Paquita", "Altos de Chipión", "Balnearia", "Miramar", "Córdoba");

    private static final Map<String, Integer> CORRIDOR_INDEX = IntStream.range(0, NORTH_TERMINAL_CORRIDOR.size())
            .boxed().collect(java.util.stream.Collectors.toUnmodifiableMap(
                    index -> normalize(NORTH_TERMINAL_CORRIDOR.get(index)), index -> index));

    public Calculation calculate(List<BookingDemand> bookings) {
        List<BookingDemand> safeBookings = bookings == null ? List.of() : bookings;
        int effectiveIndex = safeBookings.stream()
                .filter(booking -> booking != null && booking.passengers() > 0)
                .mapToInt(booking -> corridorIndex(booking.locality()))
                .filter(index -> index >= 0)
                .min()
                .orElse(-1);
        if (effectiveIndex < 0) {
            return new Calculation(null, NORTH_TERMINAL_CORRIDOR.getFirst(), List.of(),
                    "Sin pasajeros confirmados para el corredor en este turno.");
        }

        String effectiveOrigin = NORTH_TERMINAL_CORRIDOR.get(effectiveIndex);
        List<String> skipped = List.copyOf(NORTH_TERMINAL_CORRIDOR.subList(0, effectiveIndex));
        String message = skipped.isEmpty()
                ? "Cabecera del día confirmada: " + effectiveOrigin + "."
                : "Cabecera del día recalculada: " + effectiveOrigin
                        + " (Sin pasajeros en " + String.join("/", skipped) + ").";
        return new Calculation(effectiveOrigin, NORTH_TERMINAL_CORRIDOR.getFirst(), skipped, message);
    }

    public int corridorIndex(String locality) {
        if (locality == null) return -1;
        String normalized = normalize(locality).replace(" capital", "");
        return CORRIDOR_INDEX.getOrDefault(normalized, -1);
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    public record BookingDemand(String locality, int passengers) {
    }

    public record Calculation(
            String effectiveOrigin, String theoreticalOrigin, List<String> skippedLocalities, String message) {
    }
}
