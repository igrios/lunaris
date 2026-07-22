ALTER TABLE conversation_sessions ADD COLUMN promotion_code VARCHAR(4);
ALTER TABLE conversation_sessions ADD COLUMN promotion_discount_percentage INTEGER;

ALTER TABLE reservations ADD COLUMN promotion_code VARCHAR(4);
ALTER TABLE reservations ADD COLUMN discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
