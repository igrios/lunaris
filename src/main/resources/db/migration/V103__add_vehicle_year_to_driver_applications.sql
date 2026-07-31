ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS vehicle_year INTEGER;

UPDATE driver_applications
SET vehicle_year = 0
WHERE vehicle_year IS NULL;

ALTER TABLE driver_applications
    ALTER COLUMN vehicle_year SET NOT NULL;
