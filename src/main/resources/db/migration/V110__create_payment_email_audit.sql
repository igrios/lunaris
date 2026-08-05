CREATE TABLE processed_payment_transactions (
    id                       UUID PRIMARY KEY,
    source                   VARCHAR(40) NOT NULL,
    external_notification_id VARCHAR(255) NOT NULL,
    transaction_id           VARCHAR(120) NOT NULL,
    reservation_code         VARCHAR(40) NOT NULL,
    reservation_id           UUID,
    received_amount          NUMERIC(14, 2) NOT NULL,
    expected_amount          NUMERIC(14, 2),
    payer_name               VARCHAR(180) NOT NULL,
    status                   VARCHAR(40) NOT NULL,
    detail                   VARCHAR(500),
    received_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at             TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_processed_payment_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

CREATE UNIQUE INDEX uq_processed_payment_external
    ON processed_payment_transactions(source, external_notification_id);

CREATE UNIQUE INDEX uq_processed_payment_transaction
    ON processed_payment_transactions(source, transaction_id);

CREATE INDEX idx_processed_payment_reservation
    ON processed_payment_transactions(reservation_id);

CREATE TABLE payment_audit_outbox (
    id           UUID PRIMARY KEY,
    event_type   VARCHAR(60) NOT NULL,
    aggregate_id VARCHAR(120),
    payload      TEXT NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    published    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_payment_audit_outbox_pending
    ON payment_audit_outbox(published, created_at);
