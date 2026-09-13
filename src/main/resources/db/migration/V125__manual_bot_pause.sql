ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS manually_paused BOOLEAN NOT NULL DEFAULT false;
