package com.lunaris.ansenuza.application.conversation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Optional;

public class FechaParser {

    private static final DateTimeFormatter FORMATEADOR_FLEXIBLE = new DateTimeFormatterBuilder()
            .appendPattern("[d][dd]") // Día de 1 o 2 dígitos
            .appendPattern("[/][-]")  // Soporta barra o guion
            .appendPattern("[M][MM]") // Mes de 1 o 2 dígitos
            .appendPattern("[/][-]")  // Soporta barra o guion
            .appendValueReduced(ChronoField.YEAR, 2, 4, 2000) // Año de 2 cifras (26 -> 2026) o 4 cifras (2026)
            .toFormatter();

    public static Optional<LocalDate> parsear(String textoUsuario) {
        if (textoUsuario == null) return Optional.empty();
        try {
            String textoLimpio = textoUsuario.trim();
            return Optional.of(LocalDate.parse(textoLimpio, FORMATEADOR_FLEXIBLE));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}