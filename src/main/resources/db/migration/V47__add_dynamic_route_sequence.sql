ALTER TABLE reservations ADD COLUMN route_sequence INTEGER;

WITH numbered_route AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY driver_id, travel_date
               ORDER BY departure_schedule NULLS LAST, created_at, id
           ) AS sequence
    FROM reservations
    WHERE driver_id IS NOT NULL
      AND status <> 'CANCELLED'
)
UPDATE reservations reservation
SET route_sequence = numbered_route.sequence
FROM numbered_route
WHERE reservation.id = numbered_route.id;

CREATE INDEX idx_reservations_driver_date_sequence
    ON reservations (driver_id, travel_date, route_sequence);
