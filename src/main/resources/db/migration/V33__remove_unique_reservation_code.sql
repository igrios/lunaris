-- 🛠️ MIGRACIÓN FLYWAY: Removemos la restricción única del código de reserva
ALTER TABLE reservations DROP CONSTRAINT IF EXISTS reservations_reservation_code_key;

-- Opcional: Si querés que las búsquedas sigan volando, le creamos un índice común (no único)
CREATE INDEX IF NOT EXISTS idx_reservations_reservation_code ON reservations(reservation_code);