-- Algunas instalaciones externas incorporan return_schedule_id; el esquema base usa texto.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'reservations'
                 AND column_name = 'return_schedule_id') THEN
        ALTER TABLE reservations ALTER COLUMN return_schedule_id DROP NOT NULL;
    END IF;
END;
$$;

ALTER TABLE reservations
    ADD COLUMN is_flexible_return_time BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN return_time_preference VARCHAR(500);
