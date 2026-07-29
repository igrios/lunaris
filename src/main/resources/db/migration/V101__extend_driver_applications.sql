ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS locality VARCHAR(120);

ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS wants_direct_contact BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS insurance_file_url VARCHAR(500);

ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS green_card_file_url VARCHAR(500);

ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS criminal_record_file_url VARCHAR(500);

UPDATE driver_applications
SET locality = 'Sin especificar'
WHERE locality IS NULL OR BTRIM(locality) = '';

ALTER TABLE driver_applications
    ALTER COLUMN locality SET NOT NULL;
