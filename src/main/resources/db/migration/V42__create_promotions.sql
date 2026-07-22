CREATE TABLE promotions (
    id UUID PRIMARY KEY,
    code VARCHAR(4) NOT NULL UNIQUE,
    discount_percentage INTEGER NOT NULL CHECK (discount_percentage BETWEEN 10 AND 100),
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_promotions_available_code ON promotions (code) WHERE used = FALSE;
