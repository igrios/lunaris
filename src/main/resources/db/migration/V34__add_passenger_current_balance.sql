-- 💰 Agregamos la columna de saldo a favor en la tabla de pasajeros con valor inicial 0
ALTER TABLE passengers ADD COLUMN IF NOT EXISTS current_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00;