ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS payment_expires_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE reservations
SET payment_expires_at = created_at + INTERVAL '20 minutes'
WHERE payment_verified = FALSE
  AND UPPER(status) IN ('PENDING_PAYMENT', 'PENDING_VERIFICATION', 'PAYMENT_RECEIVED')
  AND payment_expires_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reservations_payment_expiration
    ON reservations(payment_expires_at)
    WHERE payment_verified = FALSE
      AND UPPER(status) IN ('PENDING_PAYMENT', 'PENDING_VERIFICATION', 'PAYMENT_RECEIVED');

-- Un comprobante puede compartirse entre los dos tramos del mismo grupo, pero no
-- reutilizarse como comprobante canónico de otra reserva activa.
CREATE OR REPLACE FUNCTION prevent_duplicate_active_receipt()
RETURNS trigger AS $$
BEGIN
    IF NEW.payment_receipt_url IS NOT NULL AND NEW.payment_receipt_url <> ''
       AND UPPER(COALESCE(NEW.status, '')) NOT IN ('CANCELLED', 'EXPIRED', 'REJECTED')
       AND EXISTS (
           SELECT 1 FROM reservations existing
           WHERE existing.id <> NEW.id
             AND existing.payment_receipt_url = NEW.payment_receipt_url
             AND UPPER(COALESCE(existing.status, '')) NOT IN ('CANCELLED', 'EXPIRED', 'REJECTED')
             AND (NEW.booking_group_code IS NULL
                  OR existing.booking_group_code IS NULL
                  OR existing.booking_group_code <> NEW.booking_group_code)
       ) THEN
        RAISE EXCEPTION 'El comprobante ya está vinculado a otra reserva activa';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_unique_active_receipt ON reservations;
CREATE TRIGGER trg_unique_active_receipt
BEFORE INSERT OR UPDATE OF payment_receipt_url, status, booking_group_code ON reservations
FOR EACH ROW EXECUTE FUNCTION prevent_duplicate_active_receipt();

CREATE OR REPLACE FUNCTION prevent_reservation_event_mutation()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'reservation_events es un registro inmutable';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_reservation_events_immutable ON reservation_events;
CREATE TRIGGER trg_reservation_events_immutable
BEFORE UPDATE OR DELETE ON reservation_events
FOR EACH ROW EXECUTE FUNCTION prevent_reservation_event_mutation();

CREATE OR REPLACE FUNCTION audit_reservation_mutation()
RETURNS trigger AS $$
BEGIN
    IF ROW(OLD.*) IS DISTINCT FROM ROW(NEW.*) THEN
        INSERT INTO reservation_events (
            id, reservation_id, event_type, description, created_at, triggered_by)
        VALUES (
            md5(random()::text || clock_timestamp()::text)::uuid,
            NEW.id,
            CASE
                WHEN OLD.status IS DISTINCT FROM NEW.status
                  OR OLD.travel_status IS DISTINCT FROM NEW.travel_status
                    THEN 'STATE_CHANGED'
                WHEN OLD.payment_verified IS DISTINCT FROM NEW.payment_verified
                    THEN 'PAYMENT_VERIFICATION_CHANGED'
                ELSE 'RESERVATION_MODIFIED'
            END,
            LEFT('Cambio BD: status=' || COALESCE(OLD.status, 'NULL') || '->'
                || COALESCE(NEW.status, 'NULL') || ', travel_status='
                || COALESCE(OLD.travel_status, 'NULL') || '->'
                || COALESCE(NEW.travel_status, 'NULL'), 500),
            NOW(),
            'DB_SYSTEM');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_reservations_audit ON reservations;
CREATE TRIGGER trg_reservations_audit
AFTER UPDATE ON reservations
FOR EACH ROW EXECUTE FUNCTION audit_reservation_mutation();
