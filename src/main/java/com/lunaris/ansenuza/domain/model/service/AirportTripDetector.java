package com.lunaris.ansenuza.domain.model.service;

import java.text.Normalizer;
import java.util.Locale;

/** Identifica solicitudes especiales hacia o desde el Aeropuerto de Córdoba. */
public final class AirportTripDetector {

    private static final String AIRPORT = "aeropuerto";
    private static final String PAJAS_BLANCAS = "pajas blancas";

    private AirportTripDetector() {
    }

    public static boolean isAirportTrip(String pickupLocality, String destination) {
        return isAirportLocation(pickupLocality) || isAirportLocation(destination);
    }

    private static boolean isAirportLocation(String location) {
        if (location == null || location.isBlank()) {
            return false;
        }
        String normalized = Normalizer.normalize(location, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return normalized.contains(AIRPORT) || normalized.contains(PAJAS_BLANCAS);
    }
}
