package com.lunaris.ansenuza.service.interurban;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public class DriverRouteSheetQuery {
    public enum StopType { PICKUP, DROPOFF }
    public record Stop(UUID tripId, UUID reservationId, StopType type, Instant scheduledAt,
            String passengerName, String address, String phone, String callUrl, String whatsappUrl, String status) {}
    public record RouteSheet(UUID driverId, LocalDate date, List<Stop> stops) {}
    private final DriverOperationsRepository repository;
    private final DriverIdentityPort identity;

    public DriverRouteSheetQuery(DriverOperationsRepository repository, DriverIdentityPort identity) {
        this.repository = repository;
        this.identity = identity;
    }

    @Transactional(readOnly = true)
    public RouteSheet find(LocalDate date, Authentication authentication) {
        UUID driverId = identity.requireDriver(authentication);
        if (date == null) throw new InvalidTripException();
        var stops = new ArrayList<Stop>();
        for (var passenger : repository.routeSheet(driverId, date)) {
            String phone = internationalPhone(passenger.phone());
            String tel = phone == null ? null : "tel:" + phone;
            String wa = phone == null ? null : "https://wa.me/" + phone.substring(1);
            stops.add(new Stop(passenger.tripId(), passenger.reservationId(), StopType.PICKUP, passenger.pickupAt(),
                    passenger.name(), passenger.pickupAddress(), phone, tel, wa, passenger.status()));
            stops.add(new Stop(passenger.tripId(), passenger.reservationId(), StopType.DROPOFF, passenger.dropoffAt(),
                    passenger.name(), passenger.dropoffAddress(), phone, tel, wa, passenger.status()));
        }
        stops.sort(Comparator.comparing(Stop::scheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Stop::type).thenComparing(Stop::tripId).thenComparing(Stop::reservationId));
        return new RouteSheet(driverId, date, List.copyOf(stops));
    }

    private String internationalPhone(String raw) {
        if (raw == null) return null;
        String clean = raw.replaceAll("[\\s().-]", "");
        // Conservar E.164 explícito (incluido 549); no convertir datos arbitrarios en URLs.
        if (clean.matches("\\+[1-9][0-9]{7,14}")) return clean;
        if (clean.matches("54[1-9][0-9]{9,10}")) return "+" + clean;
        try { return "+" + com.lunaris.ansenuza.shared.PhoneUtils.normalizeArgentinePhone(raw); }
        catch (RuntimeException e) { return null; }
    }
}
