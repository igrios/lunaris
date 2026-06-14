ALTER TABLE conversation_sessions
ADD COLUMN pickup_locality VARCHAR(100);

ALTER TABLE conversation_sessions
ADD COLUMN pickup_address VARCHAR(255);

ALTER TABLE conversation_sessions
ADD COLUMN destination VARCHAR(100);

ALTER TABLE conversation_sessions
ADD COLUMN travel_date DATE;