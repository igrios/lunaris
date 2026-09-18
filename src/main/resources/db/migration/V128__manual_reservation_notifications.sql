ALTER TABLE reservations ADD COLUMN invoice_url VARCHAR(2048);
ALTER TABLE reservations ADD COLUMN manual_notification_pending BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE reservations ADD COLUMN manual_notification_waiting_reply BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE reservations ADD COLUMN manual_notification_attempt_at TIMESTAMP;
ALTER TABLE invoices ADD COLUMN authorization_code VARCHAR(40);
CREATE INDEX idx_reservations_manual_notification_pending
    ON reservations (manual_notification_attempt_at, created_at)
    WHERE manual_notification_pending = TRUE;
