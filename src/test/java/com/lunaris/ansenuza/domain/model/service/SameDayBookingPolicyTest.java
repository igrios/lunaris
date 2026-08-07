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
    void closesSelectedShiftAtExactDepartureTime() {
        SystemConfigurationService configurations = mock(SystemConfigurationService.class);
        when(configurations.getValue(SameDayBookingPolicy.BUFFER_CONFIGURATION_KEY, "0"))
                .thenReturn("0");
        SameDayBookingPolicy policy = new SameDayBookingPolicy(configurations);

        assertFalse(policy.isShiftClosed("08:00 AM", java.time.LocalTime.of(7, 59)));
        assertTrue(policy.isShiftClosed("08:00 AM", java.time.LocalTime.of(8, 0)));
        assertTrue(policy.isShiftClosed("03:00 AM", java.time.LocalTime.of(7, 59)));
    }

    @Test
    void throwsSpecificExceptionForClosedSameDayBooking() {
        SystemConfigurationService configurations = mock(SystemConfigurationService.class);
        when(configurations.getValue(SameDayBookingPolicy.BUFFER_CONFIGURATION_KEY, "0"))
                .thenReturn("0");
        SameDayBookingPolicy policy = new SameDayBookingPolicy(configurations);

        assertThrows(SameDayBookingClosedException.class,
                () -> policy.validate(com.lunaris.ansenuza.shared.ArgentinaTime.today(), "00:00"));
    }

    @Test
    void appliesConfiguredBufferBeforeShiftDeparture() {
        SystemConfigurationService configurations = mock(SystemConfigurationService.class);
        when(configurations.getValue(SameDayBookingPolicy.BUFFER_CONFIGURATION_KEY, "0"))
                .thenReturn("30");
        SameDayBookingPolicy policy = new SameDayBookingPolicy(configurations);

        assertFalse(policy.isShiftClosed("08:00", java.time.LocalTime.of(7, 29)));
        assertTrue(policy.isShiftClosed("08:00", java.time.LocalTime.of(7, 30)));
    }
}
