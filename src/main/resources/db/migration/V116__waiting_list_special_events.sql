ALTER TABLE waiting_list_entries
    ALTER COLUMN travel_date DROP NOT NULL;

ALTER TABLE waiting_list_entries
    ADD COLUMN IF NOT EXISTS notes VARCHAR(500);

ALTER TABLE waiting_list_entries
    ADD COLUMN IF NOT EXISTS event_type VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_waiting_list_active_created
    ON waiting_list_entries(status, created_at DESC);
