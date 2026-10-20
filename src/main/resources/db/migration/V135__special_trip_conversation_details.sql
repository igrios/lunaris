ALTER TABLE conversation_sessions
    ADD COLUMN trip_category VARCHAR(16),
    ADD COLUMN origin_custom VARCHAR(100),
    ADD COLUMN destination_custom VARCHAR(100),
    ADD COLUMN custom_price NUMERIC(10,2);

ALTER TABLE conversation_sessions
    ADD CONSTRAINT session_trip_category_valid CHECK (trip_category IN ('REGULAR', 'SPECIAL')),
    ADD CONSTRAINT session_custom_price_positive CHECK (custom_price IS NULL OR custom_price > 0);
