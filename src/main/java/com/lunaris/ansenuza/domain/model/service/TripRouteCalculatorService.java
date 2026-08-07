package com.lunaris.ansenuza.domain.model.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import com.lunaris.ansenuza.domain.model.Reservation;

public class TripRouteCalculatorService {
    public enum RouteDirection { OUTBOUND, RETURN }

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

    public boolean matchesManifest(
            Reservation reservation, RouteDirection direction, String scheduleBlock) {
        if (reservation == null || direction == null) return false;
        boolean fromCordoba = isCordoba(reservation.getPickupLocality());
        boolean toCordoba = isCordoba(reservation.getDestination());
        boolean routeMatches = direction == RouteDirection.RETURN
                ? fromCordoba && !toCordoba
                : !fromCordoba && toCordoba;
        boolean legCodeMatches = reservation.getReservationCode() == null
                || direction == RouteDirection.RETURN
                        && reservation.getReservationCode().endsWith("-VUELTA")
                || direction == RouteDirection.OUTBOUND
                        && !reservation.getReservationCode().endsWith("-VUELTA");
        return routeMatches && legCodeMatches
                && normalizeSchedule(scheduleBlock).equals(
                        normalizeSchedule(reservation.getDepartureSchedule()));
    }

    public boolean sameManifest(Reservation first, Reservation candidate) {
        if (first == null || candidate == null) return false;
        RouteDirection direction = isCordoba(first.getPickupLocality())
                ? RouteDirection.RETURN : RouteDirection.OUTBOUND;
        return matchesManifest(candidate, direction, first.getDepartureSchedule());
    }

    public static String normalizeSchedule(String schedule) {
        if (schedule == null) return "";
        String normalized = schedule.trim().toUpperCase(Locale.ROOT);
        return normalized.endsWith(" AM") || normalized.endsWith(" PM")
                ? normalized.substring(0, normalized.length() - 3)
                : normalized;
    }

    public static boolean isCordoba(String locality) {
        if (locality == null) return false;
        String normalized = normalize(locality);
        return normalized.equals("cordoba") || normalized.equals("cordoba capital");
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
