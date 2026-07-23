ALTER TABLE promotion_usages
    DROP CONSTRAINT IF EXISTS uk_promotion_usage_phone;

UPDATE promotion_usages
SET phone_number = '54' || phone_number
WHERE LENGTH(phone_number) = 10
  AND phone_number NOT LIKE '54%';

WITH duplicated_usages AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY promotion_id, phone_number
               ORDER BY created_at, id
           ) AS row_number
    FROM promotion_usages
)
DELETE FROM promotion_usages
WHERE id IN (SELECT id FROM duplicated_usages WHERE row_number > 1);

ALTER TABLE promotion_usages
    ADD CONSTRAINT uk_promotion_usage_phone UNIQUE (promotion_id, phone_number);

UPDATE reservations
SET route_sequence = NULL
WHERE status = 'CANCELLED';

WITH numbered_route AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY driver_id, travel_date
               ORDER BY route_sequence NULLS LAST, departure_schedule NULLS LAST, created_at, id
           ) AS sequence
    FROM reservations
    WHERE driver_id IS NOT NULL
      AND status IS DISTINCT FROM 'CANCELLED'
)
UPDATE reservations reservation
SET route_sequence = numbered_route.sequence
FROM numbered_route
WHERE reservation.id = numbered_route.id;

ALTER TABLE reservations
    ADD CONSTRAINT uk_reservations_driver_date_route_sequence
    UNIQUE (driver_id, travel_date, route_sequence)
    DEFERRABLE INITIALLY DEFERRED;
