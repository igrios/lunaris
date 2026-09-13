-- Los escritores actuales guardan importes por tramo. No se infiere duplicación por igualdad:
-- dos mitades iguales también representan una reserva correcta.
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS amount_is_group_total BOOLEAN NOT NULL DEFAULT false;
