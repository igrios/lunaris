-- 1. Agregamos los timestamps y el código (usando IF NOT EXISTS para la columna que ya tenés)
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS reservation_code VARCHAR(20) UNIQUE;
ALTER TABLE reservations ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();
ALTER TABLE reservations ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();

-- 2. Creamos la tabla inmutable para auditoría por eventos (Timeline)
CREATE TABLE reservation_events (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    triggered_by VARCHAR(100) DEFAULT 'SYSTEM',
    CONSTRAINT fk_reservation_event FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);