package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class DriverRouteSheetQueryTest {
    @Test void returnsChronologicalStopsAndSafeContactLinksOnlyForAuthenticatedDriver() {
        var repository = mock(DriverOperationsRepository.class);
        var identity = mock(DriverIdentityPort.class);
        var auth = mock(Authentication.class);
        UUID driver = UUID.randomUUID(), trip = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 9, 20);
        when(identity.requireDriver(auth)).thenReturn(driver);
        when(repository.routeSheet(driver, date)).thenReturn(List.of(
                new DriverOperationsRepository.RoutePassenger(trip, UUID.randomUUID(), "Ana", "+54 9 3562 123456", "PAID",
                        "A", "B", Instant.parse("2026-09-20T12:00:00Z"), Instant.parse("2026-09-20T13:00:00Z")),
                new DriverOperationsRepository.RoutePassenger(trip, UUID.randomUUID(), "Juan", "sin teléfono", "CHECKED_IN",
                        "C", "D", Instant.parse("2026-09-20T11:00:00Z"), null)));
        var sheet = new DriverRouteSheetQuery(repository, identity).find(date, auth);
        assertEquals(driver, sheet.driverId());
        assertEquals(4, sheet.stops().size());
        assertEquals("Juan", sheet.stops().getFirst().passengerName());
        assertNull(sheet.stops().getFirst().callUrl());
        assertEquals("tel:+5493562123456", sheet.stops().get(1).callUrl());
        assertEquals("https://wa.me/5493562123456", sheet.stops().get(1).whatsappUrl());
        assertEquals(DriverRouteSheetQuery.StopType.DROPOFF, sheet.stops().get(2).type());
        assertNull(sheet.stops().getLast().scheduledAt());
        verify(repository).routeSheet(driver, date);
    }
}
