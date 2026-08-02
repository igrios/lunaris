ALTER TABLE driver_applications
    ALTER COLUMN vehicle_year DROP NOT NULL;

ALTER TABLE driver_applications
    ALTER COLUMN license_plate DROP NOT NULL;
