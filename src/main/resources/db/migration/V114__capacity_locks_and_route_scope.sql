CREATE TABLE IF NOT EXISTS reservation_capacity_locks (
    lock_key VARCHAR(255) PRIMARY KEY
);

ALTER TABLE reservations ADD COLUMN IF NOT EXISTS route_direction VARCHAR(16);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS return_audit_sent_at TIMESTAMP;

UPDATE reservations
SET route_direction = CASE
    WHEN LOWER(REPLACE(pickup_locality, 'ó', 'o')) LIKE '%cordoba%'
         AND LOWER(REPLACE(destination, 'ó', 'o')) NOT LIKE '%cordoba%' THEN 'VUELTA'
    WHEN LOWER(REPLACE(destination, 'ó', 'o')) LIKE '%cordoba%'
         AND LOWER(REPLACE(pickup_locality, 'ó', 'o')) NOT LIKE '%cordoba%' THEN 'IDA'
    ELSE route_direction
END
WHERE route_direction IS NULL;

ALTER TABLE reservations DROP CONSTRAINT IF EXISTS uk_reservations_driver_date_route_sequence;
CREATE UNIQUE INDEX IF NOT EXISTS uk_reservations_driver_route_scope
    ON reservations(driver_id, travel_date, departure_schedule, route_direction, route_sequence)
    WHERE driver_id IS NOT NULL AND route_sequence IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_reservations_driver_route_scope
    ON reservations(driver_id, travel_date, departure_schedule, route_direction);
