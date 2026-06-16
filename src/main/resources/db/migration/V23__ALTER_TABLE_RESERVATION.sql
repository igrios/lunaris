-- Agregamos la columna a la tabla de reservas si no existe
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS payment_receipt_url VARCHAR(500);