package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.exception.SameDayBookingClosedException;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SameDayBookingPolicy {

    public static final String CONFIGURATION_KEY = "SAME_DAY_CUTOFF_TIME";
    public static final String BUFFER_CONFIGURATION_KEY = "SAME_DAY_CUTOFF_BUFFER_MINUTES";
    public static final LocalTime DEFAULT_CUTOFF = LocalTime.of(8, 0);
    public static final List<LocalTime> DEFAULT_SHIFTS = List.of(
            LocalTime.of(3, 0), LocalTime.of(8, 0));

    private final SystemConfigurationService configurationService;

    public void validate(LocalDate travelDate) {
        if (isClosed(travelDate, ArgentinaTime.now())) {
            throw new SameDayBookingClosedException();
        }
    }

    public void validate(LocalDate travelDate, String selectedShift) {
        LocalDateTime now = ArgentinaTime.now();
        if (travelDate != null && travelDate.equals(now.toLocalDate())
                && isShiftClosed(selectedShift, now.toLocalTime())) {
            throw new SameDayBookingClosedException();
        }
    }

    public boolean isTodayClosed() {
        LocalDateTime now = ArgentinaTime.now();
        return isClosed(now.toLocalDate(), now);
    }

    boolean isClosed(LocalDate travelDate, LocalDateTime now) {
        return travelDate != null
                && travelDate.equals(now.toLocalDate())
                && DEFAULT_SHIFTS.stream().allMatch(shift -> isShiftClosed(shift, now.toLocalTime()));
    }

    public boolean isTodayClosed(String selectedShift) {
        return isShiftClosed(selectedShift, ArgentinaTime.currentTime());
    }

    boolean isShiftClosed(String selectedShift, LocalTime now) {
        LocalTime shift = parseShift(selectedShift);
        return shift == null
                ? DEFAULT_SHIFTS.stream().allMatch(candidate -> isShiftClosed(candidate, now))
                : isShiftClosed(shift, now);
    }

    boolean isShiftClosed(LocalTime shift, LocalTime now) {
        LocalTime cutoff = shift.minusMinutes(cutoffBufferMinutes());
        return !now.isBefore(cutoff);
    }

    private LocalTime parseShift(String selectedShift) {
        if (selectedShift == null || selectedShift.isBlank()) return null;
        String value = selectedShift.trim().toUpperCase(Locale.ROOT);
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("H:mm", Locale.ROOT),
                DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))) {
            try {
                return LocalTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Se prueba el siguiente formato soportado.
            }
        }
        return null;
    }

    public int cutoffBufferMinutes() {
        try {
            return Math.max(0, Integer.parseInt(configurationService.getValue(
                    BUFFER_CONFIGURATION_KEY, "0").trim()));
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    public LocalTime cutoffTime() {
        String configured = configurationService.getValue(
                CONFIGURATION_KEY, DEFAULT_CUTOFF.toString());
        try {
            return LocalTime.parse(configured.trim());
        } catch (RuntimeException exception) {
            return DEFAULT_CUTOFF;
        }
    }
}
