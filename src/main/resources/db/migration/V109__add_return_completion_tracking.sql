ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS returned_passenger_count INTEGER NOT NULL DEFAULT 0;

UPDATE reservations
SET returned_passenger_count = 0
WHERE returned_passenger_count IS NULL;
