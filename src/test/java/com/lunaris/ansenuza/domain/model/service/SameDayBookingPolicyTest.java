package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.exception.SameDayBookingClosedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SameDayBookingPolicyTest {

    @Test
    void closesTodayStrictlyAfterConfiguredCutoff() {
        SystemConfigurationService configurations = mock(SystemConfigurationService.class);
        when(configurations.getValue(SameDayBookingPolicy.CONFIGURATION_KEY, "08:00"))
                .thenReturn("08:00");
        SameDayBookingPolicy policy = new SameDayBookingPolicy(configurations);
        LocalDate today = LocalDate.of(2026, 8, 3);

        assertFalse(policy.isClosed(today, LocalDateTime.of(2026, 8, 3, 8, 0)));
        assertTrue(policy.isClosed(today, LocalDateTime.of(2026, 8, 3, 8, 1)));
        assertFalse(policy.isClosed(today.plusDays(1), LocalDateTime.of(2026, 8, 3, 20, 0)));
    }

    @Test
    void throwsSpecificExceptionForClosedSameDayBooking() {
        SystemConfigurationService configurations = mock(SystemConfigurationService.class);
        when(configurations.getValue(SameDayBookingPolicy.CONFIGURATION_KEY, "08:00"))
                .thenReturn("00:00");
        SameDayBookingPolicy policy = new SameDayBookingPolicy(configurations);

        assertThrows(SameDayBookingClosedException.class,
                () -> policy.validate(com.lunaris.ansenuza.shared.ArgentinaTime.today()));
    }
}
