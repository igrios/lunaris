package com.lunaris.ansenuza.application.port;

import java.util.List;

/** Contrato compartido con la plantilla de reactivación del Chat en Vivo. */
public final class PassengerContactTemplate {
    public static final String NAME = "contacto_pasajero";

    private PassengerContactTemplate() {}

    public static List<String> parameters(String passengerName) {
        return List.of(passengerName == null || passengerName.isBlank()
                ? "Pasajero" : passengerName.replaceAll("\\s+", " ").trim());
    }
}
