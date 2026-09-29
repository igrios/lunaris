-- Aditiva: V129 y las tablas de Córdoba permanecen intactas.
-- El PNG permite reentrega del mismo pase sin guardar el token en claro.
CREATE TABLE interurban.qr_artifacts (
    reservation_id UUID PRIMARY KEY REFERENCES interurban.qr_tokens(reservation_id),
    encrypted_png BYTEA NOT NULL,
    key_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX interurban_pending_payment_inbox
    ON interurban.payment_inbox(received_at) WHERE processed_at IS NULL;
