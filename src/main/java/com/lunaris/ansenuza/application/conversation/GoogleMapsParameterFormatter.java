package com.lunaris.ansenuza.application.conversation;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import com.lunaris.ansenuza.domain.model.Reservation;

public final class GoogleMapsParameterFormatter {

    private GoogleMapsParameterFormatter() {
    }

    public static String encode(String location) {
        String normalized = normalize(location);
        return URLEncoder.encode(normalized, StandardCharsets.UTF_8)
                .replace("%2C", ",");
    }

    public static String buildDirectionsUrl(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return "https://www.google.com/maps/dir/?api=1&destination=Cordoba";
        }
        List<String> pickups = reservations.stream()
                .filter(java.util.Objects::nonNull)
                .filter(Reservation::isScheduledConfirmedTrip)
                .map(GoogleMapsParameterFormatter::pickupLocation)
                .filter(location -> !location.isBlank())
                .toList();
        if (pickups.isEmpty()) {
            return "https://www.google.com/maps/dir/?api=1&destination=Cordoba";
        }
        StringBuilder url = new StringBuilder(
                "https://www.google.com/maps/dir/?api=1&origin=")
                .append(encode(pickups.getFirst()))
                .append("&destination=Cordoba");
        if (pickups.size() > 1) {
            url.append("&waypoints=")
                    .append(encode(String.join("|", pickups.subList(1, pickups.size())))
                            .replace("%7C", "|"));
        }
        return url.toString();
    }

    private static String pickupLocation(Reservation reservation) {
        String address = normalize(reservation.getPickupAddress());
        if (address.matches("-?\\d+(?:\\.\\d+)?,-?\\d+(?:\\.\\d+)?")) {
            return address;
        }
        String locality = reservation.getPickupLocality() == null
                || reservation.getPickupLocality().isBlank()
                ? "Córdoba"
                : reservation.getPickupLocality().trim();
        return (address.isBlank() ? locality : address + ", " + locality)
                + ", Córdoba, Argentina";
    }

    static String normalize(String location) {
        if (location == null || location.isBlank()) {
            return "";
        }
        String trimmed = location.trim();
        try {
            URI uri = URI.create(trimmed);
            if (uri.getHost() == null || !uri.getHost().endsWith("google.com")) {
                return trimmed;
            }
            return Arrays.stream(uri.getRawQuery() == null ? new String[0] : uri.getRawQuery().split("&"))
                    .map(parameter -> parameter.split("=", 2))
                    .filter(parts -> parts.length == 2 && "q".equals(parts[0]))
                    .map(parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8))
                    .findFirst()
                    .orElse("");
        } catch (IllegalArgumentException ignored) {
            return trimmed;
        }
    }
}
