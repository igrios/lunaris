ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS booking_group_code VARCHAR(40);

UPDATE reservations
SET booking_group_code = REGEXP_REPLACE(reservation_code, '-(IDA|VUELTA)$', '')
WHERE booking_group_code IS NULL
  AND reservation_code ~ '-(IDA|VUELTA)$';

CREATE INDEX IF NOT EXISTS idx_reservations_booking_group_code
    ON reservations(booking_group_code);
