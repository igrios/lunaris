ALTER TABLE drivers
    ADD COLUMN IF NOT EXISTS current_location_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS location_updated_at TIMESTAMP;
