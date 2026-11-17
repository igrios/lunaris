ALTER TABLE reservations ADD COLUMN IF NOT EXISTS recognized_paid_amount NUMERIC(10,2);
-- PostgreSQL impide ALTER TYPE si un trigger UPDATE OF depende de la columna
-- (trg_unique_active_receipt fue creado en V119). Flyway ejecuta este script
-- en una transacción: el bloqueo DDL se conserva hasta recrear la protección.
DROP TRIGGER IF EXISTS trg_unique_active_receipt ON reservations;
ALTER TABLE reservations ALTER COLUMN booking_group_code TYPE VARCHAR(64);
CREATE TRIGGER trg_unique_active_receipt
BEFORE INSERT OR UPDATE OF payment_receipt_url, status, booking_group_code ON reservations
FOR EACH ROW EXECUTE FUNCTION prevent_duplicate_active_receipt();

CREATE TABLE IF NOT EXISTS reservation_update_states (
    group_code VARCHAR(64) PRIMARY KEY,
    primary_reservation_id UUID NOT NULL REFERENCES reservations(id),
    revision INTEGER NOT NULL DEFAULT 0 CHECK (revision >= 0),
    fingerprint VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS reservation_update_operations (
    operation_key VARCHAR(64) PRIMARY KEY,
    group_code VARCHAR(64) NOT NULL REFERENCES reservation_update_states(group_code),
    request_hash VARCHAR(64) NOT NULL,
    before_json TEXT NOT NULL,
    triggered_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    result_json TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_reservation_update_operations_group ON reservation_update_operations(group_code);
CREATE INDEX IF NOT EXISTS idx_reservations_booking_group_code ON reservations(booking_group_code);
