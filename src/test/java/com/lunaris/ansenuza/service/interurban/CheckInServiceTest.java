package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

class CheckInServiceTest {
    final DriverOperationsRepository repository = mock(DriverOperationsRepository.class);
    final DriverIdentityPort identity = mock(DriverIdentityPort.class);
    final Authentication authentication = mock(Authentication.class);
    final UUID driver = UUID.randomUUID(), trip = UUID.randomUUID(), reservation = UUID.randomUUID();
    final Instant now = Instant.parse("2026-09-20T15:00:00Z");
    final String token = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[32]);
    CheckInService service;

    @BeforeEach void setup() {
        service = new CheckInService(repository, identity, Clock.fixed(now, ZoneOffset.UTC), Duration.ofHours(1), Duration.ofHours(2));
        when(identity.requireDriver(authentication)).thenReturn(driver);
        when(repository.lockTrip(trip)).thenReturn(Optional.of(new DriverOperationsRepository.Trip(trip, driver,
                "ASSIGNED", LocalDate.of(2026, 9, 20), now)));
        when(repository.lockTicket(CheckInService.tokenHash(token), trip)).thenReturn(Optional.of(ticket("PAID", null, now.plusSeconds(600), true)));
    }

    DriverOperationsRepository.Ticket ticket(String state, Instant consumed, Instant expiry, boolean paid) {
        return new DriverOperationsRepository.Ticket(reservation, trip, state, expiry, consumed, now,
                new BigDecimal("1234.50"), new BigDecimal("234.50"), paid);
    }

    @Test void successfulCheckInConsumesTokenChangesStateAndCreditsNetFareOnce() {
        var result = service.verify(token, trip, authentication);
        assertEquals("CHECKED_IN", result.status());
        assertEquals(reservation, result.reservationId());
        var ordered = inOrder(repository);
        ordered.verify(repository).lockTrip(trip);
        ordered.verify(repository).lockTicket(CheckInService.tokenHash(token), trip);
        ordered.verify(repository).consume(reservation, now);
        ordered.verify(repository).markCheckedIn(reservation, driver, now);
        ordered.verify(repository).earn(reservation, driver, new BigDecimal("1000.00"), now);
        verifyNoMoreInteractions(repository);
    }

    @Test void twoIdenticalScansProduceOneCredit() {
        when(repository.lockTicket(CheckInService.tokenHash(token), trip)).thenReturn(
                Optional.of(ticket("PAID", null, now.plusSeconds(600), true)),
                Optional.of(ticket("CHECKED_IN", now, now.plusSeconds(600), true)));
        service.verify(token, trip, authentication);
        assertThrows(QrAlreadyConsumedException.class, () -> service.verify(token, trip, authentication));
        verify(repository, times(1)).consume(reservation, now);
        verify(repository, times(1)).markCheckedIn(reservation, driver, now);
        verify(repository, times(1)).earn(reservation, driver, new BigDecimal("1000.00"), now);
    }

    @Test void expiredQrRejectsAtExactExpiry() {
        when(repository.lockTicket(anyString(), eq(trip))).thenReturn(Optional.of(ticket("PAID", null, now, true)));
        assertThrows(QrExpiredException.class, () -> service.verify(token, trip, authentication));
        verify(repository, never()).consume(any(), any());
        verify(repository, never()).earn(any(), any(), any(), any());
    }

    @Test void anotherDriverCannotInspectOrConsumeQr() {
        when(identity.requireDriver(authentication)).thenReturn(UUID.randomUUID());
        assertThrows(AccessDeniedException.class, () -> service.verify(token, trip, authentication));
        verify(repository, never()).lockTicket(any(), any());
    }

    @Test void tokenFromAnotherTripIsRejected() {
        when(repository.lockTicket(anyString(), eq(trip))).thenReturn(Optional.empty());
        assertThrows(InvalidTripException.class, () -> service.verify(token, trip, authentication));
        verify(repository, never()).consume(any(), any());
    }

    @Test void paymentMustStillBeApproved() {
        when(repository.lockTicket(anyString(), eq(trip))).thenReturn(Optional.of(ticket("PAID", null, now.plusSeconds(600), false)));
        assertThrows(InvalidTripException.class, () -> service.verify(token, trip, authentication));
        verify(repository, never()).earn(any(), any(), any(), any());
    }

    @Test void heldReservationCannotBoard() {
        when(repository.lockTicket(anyString(), eq(trip))).thenReturn(Optional.of(ticket("HELD", null, now.plusSeconds(600), true)));
        assertThrows(InvalidTripException.class, () -> service.verify(token, trip, authentication));
    }

    @Test void invalidAndNonCanonicalBase64NeverReachRepository() {
        for (String invalid : new String[]{"bad", token + "=", token.substring(0, 42) + "B"}) {
            assertThrows(InvalidQrException.class, () -> service.verify(invalid, trip, authentication));
        }
        verifyNoInteractions(repository);
    }

    @Test void dateUsesArgentinaRatherThanUtcDay() {
        service = new CheckInService(repository, identity, Clock.fixed(Instant.parse("2026-09-21T01:00:00Z"), ZoneOffset.UTC),
                Duration.ofHours(1), Duration.ofHours(12));
        when(repository.lockTicket(anyString(), eq(trip))).thenReturn(Optional.of(ticket("PAID", null, now.plusSeconds(50000), true)));
        assertEquals("CHECKED_IN", service.verify(token, trip, authentication).status());
    }

    @Test void pickupWindowPreventsEarlyBoarding() {
        service = new CheckInService(repository, identity, Clock.fixed(now.minusSeconds(3601), ZoneOffset.UTC), Duration.ofHours(1), Duration.ofHours(2));
        assertThrows(InvalidTripException.class, () -> service.verify(token, trip, authentication));
        verify(repository, never()).consume(any(), any());
    }
}
