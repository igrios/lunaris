package com.lunaris.ansenuza.service.interurban;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Sin @Repository: no se instancia si el módulo está apagado. */
public class JdbcCapacityRepository implements CapacityRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcCapacityRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Optional<Trip> findTrip(UUID id) { return trip(id, false); }
    @Override public Optional<Trip> lockTrip(UUID id) { return trip(id, true); }

    private Optional<Trip> trip(UUID id, boolean lock) {
        return jdbc.query("SELECT id, direction, status, closes_at FROM interurban.trips WHERE id = :id"
                        + (lock ? " FOR UPDATE" : ""), Map.of("id", id),
                (rs, row) -> new Trip(rs.getObject("id", UUID.class), rs.getInt("direction"),
                        rs.getString("status"), rs.getTimestamp("closes_at").toInstant())).stream().findFirst();
    }

    @Override public List<Leg> findLegs(UUID id, List<Integer> ordinals) { return legs(id, ordinals, false); }
    @Override public List<Leg> lockLegs(UUID id, List<Integer> ordinals) { return legs(id, ordinals, true); }

    private List<Leg> legs(UUID id, List<Integer> ordinals, boolean lock) {
        return jdbc.query("""
                SELECT tl.id, cl.ordinal FROM interurban.trip_legs tl
                JOIN interurban.corridor_legs cl ON cl.id = tl.corridor_leg_id
                WHERE tl.trip_id = :id AND cl.ordinal IN (:ordinals)
                ORDER BY tl.id
                """ + (lock ? " FOR UPDATE OF tl" : ""), Map.of("id", id, "ordinals", ordinals),
                (rs, row) -> new Leg(rs.getObject("id", UUID.class), rs.getInt("ordinal")));
    }

    @Override public List<Integer> occupiedSeats(UUID legId) {
        return jdbc.queryForList("SELECT seat FROM interurban.leg_seats WHERE trip_leg_id = :id ORDER BY seat",
                Map.of("id", legId), Integer.class);
    }

    @Override public void insertReservation(UUID id, UUID tripId, Route route, PassengerHold p, Instant expiresAt) {
        jdbc.update("""
                INSERT INTO interurban.reservations
                (id, booking_id, trip_id, origin_stop, destination_stop, passenger_name, phone,
                 pickup_address, dropoff_address, fare_id, fare, commission, status, hold_expires_at)
                VALUES (:id, :booking, :trip, :origin, :destination, :name, :phone,
                        :pickup, :dropoff, :fareId, :fare, :commission, 'HELD', :expires)
                """, new MapSqlParameterSource().addValue("id", id).addValue("booking", p.bookingId())
                .addValue("trip", tripId).addValue("origin", route.origin()).addValue("destination", route.destination())
                .addValue("name", p.name()).addValue("phone", p.phone()).addValue("pickup", p.pickupAddress())
                .addValue("dropoff", p.dropoffAddress()).addValue("fareId", p.fareId()).addValue("fare", p.fare())
                .addValue("commission", p.commission()).addValue("expires", Timestamp.from(expiresAt)));
    }

    @Override public void insertSeat(UUID tripId, UUID legId, int seat, UUID reservationId) {
        jdbc.update("""
                INSERT INTO interurban.leg_seats(trip_id, trip_leg_id, seat, reservation_id)
                VALUES (:trip, :leg, :seat, :reservation)
                """, Map.of("trip", tripId, "leg", legId, "seat", seat, "reservation", reservationId));
    }
}
