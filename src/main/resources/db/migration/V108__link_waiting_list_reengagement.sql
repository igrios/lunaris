ALTER TABLE conversation_sessions
    ADD COLUMN IF NOT EXISTS waiting_list_entry_id BIGINT;

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS waiting_list_entry_id BIGINT;

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_waiting_list_entry
    FOREIGN KEY (waiting_list_entry_id) REFERENCES waiting_list_entries(id);

CREATE INDEX IF NOT EXISTS idx_reservations_waiting_list_entry
    ON reservations (waiting_list_entry_id);
