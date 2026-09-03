CREATE TABLE IF NOT EXISTS whatsapp_webhook_inbox (
    message_id  VARCHAR(255) PRIMARY KEY,
    received_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_whatsapp_webhook_inbox_received_at
    ON whatsapp_webhook_inbox(received_at);
