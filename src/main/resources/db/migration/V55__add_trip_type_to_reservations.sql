ALTER TABLE reservations ADD COLUMN IF NOT EXISTS trip_type VARCHAR(20);

UPDATE reservations
SET trip_type = CASE
    WHEN COALESCE(round_trip, FALSE) = FALSE THEN 'ONE_WAY'
    WHEN travel_date IS NULL OR travel_status = 'OPEN_RETURN' THEN 'OPEN_RETURN'
    ELSE 'ROUND_TRIP'
END
WHERE trip_type IS NULL;
