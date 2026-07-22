ALTER TABLE promotions ADD COLUMN is_massive BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE promotions ADD COLUMN expires_at TIMESTAMP NULL;

CREATE TABLE promotion_usages (
    id UUID PRIMARY KEY,
    promotion_id UUID NOT NULL REFERENCES promotions(id),
    phone_number VARCHAR(30) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_promotion_usage_phone UNIQUE (promotion_id, phone_number)
);

CREATE INDEX idx_promotion_usages_promotion ON promotion_usages (promotion_id);
