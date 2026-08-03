package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.exception.SameDayBookingClosedException;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SameDayBookingPolicy {

    public static final String CONFIGURATION_KEY = "SAME_DAY_CUTOFF_TIME";
    public static final LocalTime DEFAULT_CUTOFF = LocalTime.of(8, 0);

    private final SystemConfigurationService configurationService;

    public void validate(LocalDate travelDate) {
        if (isClosed(travelDate, ArgentinaTime.now())) {
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
                && now.toLocalTime().isAfter(cutoffTime());
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
