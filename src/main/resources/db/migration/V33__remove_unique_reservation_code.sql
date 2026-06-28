-- 🛠️ MIGRACIÓN FLYWAY: Removemos la restricción única del código de reserva
ALTER TABLE reservations DROP CONSTRAINT IF EXISTS reservations_reservation_code_key;

-- Opcional: Si querés que las búsquedas sigan volando, le creamos un índice común (no único)
CREATE INDEX IF NOT EXISTS idx_reservations_reservation_code ON reservations(reservation_code);


ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS schedule_block VARCHAR(50);
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS reservation_code VARCHAR(20);