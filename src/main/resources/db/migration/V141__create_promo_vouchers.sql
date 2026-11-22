CREATE SEQUENCE promo_voucher_number_seq;

CREATE TABLE promo_vouchers (
    id UUID PRIMARY KEY,
    number VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL CHECK (status IN ('DISPONIBLE', 'CANJEADA', 'ANULADA')),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    cuil VARCHAR(11) NOT NULL,
    paid_amount NUMERIC(10,2) NOT NULL CHECK (paid_amount > 0),
    receipt_url VARCHAR(500),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    redeemed_at TIMESTAMP,
    reservation_id UUID UNIQUE REFERENCES reservations(id),
    CHECK ((status = 'CANJEADA' AND redeemed_at IS NOT NULL AND reservation_id IS NOT NULL)
        OR (status IN ('DISPONIBLE', 'ANULADA') AND redeemed_at IS NULL AND reservation_id IS NULL))
);

CREATE INDEX idx_promo_vouchers_status_created_at ON promo_vouchers (status, created_at DESC);
