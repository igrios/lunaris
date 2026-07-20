ALTER TABLE reservations ADD COLUMN IF NOT EXISTS driver_id UUID;

-- Add constraint to reference the drivers table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_reservations_driver'
    ) THEN
        ALTER TABLE reservations 
        ADD CONSTRAINT fk_reservations_driver 
        FOREIGN KEY (driver_id) 
        REFERENCES drivers(id);
    END IF;
END $$;
