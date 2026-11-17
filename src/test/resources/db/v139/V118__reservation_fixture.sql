-- Esquema mínimo anterior a V119 para probar los scripts reales sobre PostgreSQL.
CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    booking_group_code VARCHAR(40),
    payment_receipt_url TEXT,
    status VARCHAR(30),
    travel_status VARCHAR(30),
    payment_verified BOOLEAN,
    created_at TIMESTAMP
);
CREATE TABLE reservation_events (
    id UUID PRIMARY KEY, reservation_id UUID, event_type TEXT,
    description TEXT, created_at TIMESTAMP, triggered_by TEXT
);
CREATE INDEX idx_reservations_booking_group_code ON reservations(booking_group_code);
INSERT INTO reservations (id, booking_group_code, payment_receipt_url, status, payment_verified)
VALUES ('00000000-0000-0000-0000-000000000001', 'HISTORICO', 'comprobante-historico', 'CONFIRMED', TRUE);
