ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS used_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00;

DROP INDEX IF EXISTS idx_reservations_payment_expiration;
CREATE INDEX IF NOT EXISTS idx_reservations_payment_expiration
    ON reservations(payment_expires_at)
    WHERE payment_verified = FALSE
      AND UPPER(status) IN ('PENDING_PAYMENT', 'PENDING_VERIFICATION');
