ALTER TABLE conversation_sessions
ADD COLUMN requires_invoice BOOLEAN;

ALTER TABLE conversation_sessions
ADD COLUMN cuil VARCHAR(20);