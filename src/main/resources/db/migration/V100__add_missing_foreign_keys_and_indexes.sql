-- V2 ya está ocupado por V2__drivers.sql y el historial llega hasta V99.
-- V100 evita versiones duplicadas y migraciones fuera de orden en Flyway.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'invoices'::regclass
          AND constraint_definition.confrelid = 'reservations'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'reservation_id'
    ) THEN
        ALTER TABLE invoices
            ADD CONSTRAINT fk_invoices_reservation
            FOREIGN KEY (reservation_id) REFERENCES reservations(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'reservation_events'::regclass
          AND constraint_definition.confrelid = 'reservations'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'reservation_id'
    ) THEN
        ALTER TABLE reservation_events
            ADD CONSTRAINT fk_reservation_events_reservation
            FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'promotion_usages'::regclass
          AND constraint_definition.confrelid = 'promotions'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'promotion_id'
    ) THEN
        ALTER TABLE promotion_usages
            ADD CONSTRAINT fk_promotion_usages_promotion
            FOREIGN KEY (promotion_id) REFERENCES promotions(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'reservations'::regclass
          AND constraint_definition.confrelid = 'passengers'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'passenger_id'
    ) THEN
        ALTER TABLE reservations
            ADD CONSTRAINT fk_reservations_passenger
            FOREIGN KEY (passenger_id) REFERENCES passengers(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'reservations'::regclass
          AND constraint_definition.confrelid = 'drivers'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'driver_id'
    ) THEN
        ALTER TABLE reservations
            ADD CONSTRAINT fk_reservations_driver
            FOREIGN KEY (driver_id) REFERENCES drivers(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'reservations'::regclass
          AND constraint_definition.confrelid = 'promotions'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'promotion_id'
    ) THEN
        ALTER TABLE reservations
            ADD CONSTRAINT fk_reservations_promotion
            FOREIGN KEY (promotion_id) REFERENCES promotions(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_conversation_sessions_phone_number
    ON conversation_sessions (phone_number);

CREATE INDEX IF NOT EXISTS idx_chat_messages_phone_number
    ON chat_messages (phone_number);

CREATE INDEX IF NOT EXISTS idx_passengers_phone
    ON passengers (phone);

CREATE INDEX IF NOT EXISTS idx_drivers_phone
    ON drivers (phone);

CREATE INDEX IF NOT EXISTS idx_reservations_travel_date_driver_status
    ON reservations (travel_date, driver_id, status);
