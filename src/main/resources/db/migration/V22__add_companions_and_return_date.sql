-- Agregamos las columnas solo si no existen previamente
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS passenger_count INT;
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS companion_names VARCHAR(500);
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS current_companion_index INT;
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS total_companions INT;

-- La fecha de regreso (por si no se creó antes)
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS return_date DATE;