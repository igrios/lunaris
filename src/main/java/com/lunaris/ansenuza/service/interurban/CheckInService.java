package com.lunaris.ansenuza.service.interurban;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public class CheckInService {
    public record Result(UUID reservationId, UUID tripId, String status, Instant checkedInAt) {}
    private static final ZoneId ZONE = ZoneId.of("America/Argentina/Cordoba");
    private final DriverOperationsRepository repository;
    private final DriverIdentityPort identity;
    private final Clock clock;
    private final Duration earlyWindow;
    private final Duration lateWindow;

    public CheckInService(DriverOperationsRepository repository, DriverIdentityPort identity, Clock clock,
            Duration earlyWindow, Duration lateWindow) {
        if (earlyWindow.isNegative() || lateWindow.isNegative()) throw new IllegalArgumentException("Ventana de abordaje inválida.");
        this.repository = repository;
        this.identity = identity;
        this.clock = clock;
        this.earlyWindow = earlyWindow;
        this.lateWindow = lateWindow;
    }

    @Transactional
    public Result verify(String token, UUID tripId, Authentication authentication) {
        UUID driverId = identity.requireDriver(authentication);
        if (tripId == null) throw new InvalidTripException();
        String hash = tokenHash(token);
        // Mismo orden que pagos/capacidad: viaje -> reserva/QR. Nunca tomar un lock de pago después.
        var trip = repository.lockTrip(tripId).orElseThrow(InvalidTripException::new);
        if (!driverId.equals(trip.driverId())) throw new AccessDeniedException("El viaje no pertenece al chofer autenticado.");
        if (!"ASSIGNED".equals(trip.status())) throw new InvalidTripException();
        var ticket = repository.lockTicket(hash, tripId).orElseThrow(InvalidTripException::new);
        if (!tripId.equals(ticket.tripId())) throw new InvalidTripException();
        if (ticket.consumedAt() != null) throw new QrAlreadyConsumedException();
        Instant now = clock.instant(); // Releer reloj después de esperar por los locks.
        if (!now.isBefore(ticket.expiresAt())) throw new QrExpiredException();
        if (!"PAID".equals(ticket.status()) || !ticket.approvedPayment()) throw new InvalidTripException();
        Instant pickup = ticket.pickupAt() == null ? trip.departureAt() : ticket.pickupAt();
        if (!trip.date().equals(LocalDate.ofInstant(now, ZONE))
                || now.isBefore(pickup.minus(earlyWindow)) || now.isAfter(pickup.plus(lateWindow))) {
            throw new InvalidTripException();
        }
        repository.consume(ticket.reservationId(), now);
        repository.markCheckedIn(ticket.reservationId(), driverId, now);
        repository.earn(ticket.reservationId(), driverId, ticket.fare().subtract(ticket.commission()), now);
        return new Result(ticket.reservationId(), tripId, "CHECKED_IN", now);
    }

    static String tokenHash(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw new InvalidQrException();
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(token);
            if (bytes.length != 32 || !Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).equals(token)) {
                throw new InvalidQrException();
            }
            // QR etapa 2: hash del texto canónico UTF-8, no de los bytes de entropía.
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalArgumentException e) { throw new InvalidQrException(); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 no disponible", e); }
    }
}
