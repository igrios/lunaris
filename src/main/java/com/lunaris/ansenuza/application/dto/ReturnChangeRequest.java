package com.lunaris.ansenuza.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** La obligatoriedad de los campos del regreso depende de la acción y modalidad. */
public record ReturnChangeRequest(
        @NotNull ReturnAction action,
        String targetLegId,
        ReturnMode mode,
        LocalDate date,
        @Pattern(regexp = "(?:[01][0-9]|2[0-3]):[0-5][0-9]",
                message = "El horario manual debe tener formato HH:mm.")
        String customTime,
        @Size(max = 500)
        @Pattern(regexp = "[^\\p{Cntrl}]*",
                message = "La preferencia horaria no admite caracteres de control.")
        String timePreference) {}
