-- Sólo objetos nuevos, aislados de public y de los repositorios de Córdoba.
-- Una reserva representa UN pasajero; booking_id agrupa acompañantes.
CREATE SCHEMA interurban;

CREATE TABLE interurban.corridor_legs (
    id UUID PRIMARY KEY,
    code VARCHAR(16) NOT NULL UNIQUE,
    ordinal SMALLINT NOT NULL UNIQUE CHECK (ordinal BETWEEN 1 AND 3),
    origin VARCHAR(40) NOT NULL,
    destination VARCHAR(40) NOT NULL
);
INSERT INTO interurban.corridor_legs VALUES
 ('00000000-0000-0000-0000-000000000001', 'SG-SUA', 1, 'San Guillermo', 'Suardi'),
 ('00000000-0000-0000-0000-000000000002', 'SUA-MOR', 2, 'Suardi', 'Morteros'),
 ('00000000-0000-0000-0000-000000000003', 'MOR-BRI', 3, 'Morteros', 'Brinkmann');

-- Paradas 0=SG, 1=SUA, 2=MOR, 3=BRI. Tarifas direccionales versionadas.
CREATE TABLE interurban.interurban_fares (
    id UUID PRIMARY KEY,
    origin_stop SMALLINT NOT NULL CHECK (origin_stop BETWEEN 0 AND 3),
    destination_stop SMALLINT NOT NULL CHECK (destination_stop BETWEEN 0 AND 3),
    valid_from DATE NOT NULL,
    fare NUMERIC(12,2) NOT NULL CHECK (fare >= 0),
    commission NUMERIC(12,2) NOT NULL CHECK (commission >= 0 AND commission <= fare),
    currency CHAR(3) NOT NULL DEFAULT 'ARS' CHECK (currency = 'ARS'),
    CHECK (origin_stop <> destination_stop),
    UNIQUE (origin_stop, destination_stop, valid_from)
);

CREATE TABLE interurban.trips (
    id UUID PRIMARY KEY,
    service_date DATE NOT NULL,
    departure_at TIMESTAMPTZ NOT NULL,
    closes_at TIMESTAMPTZ NOT NULL CHECK (closes_at <= departure_at),
    direction SMALLINT NOT NULL CHECK (direction IN (-1, 1)),
    status VARCHAR(24) NOT NULL CHECK (status IN ('OPEN', 'CLOSED', 'ASSIGNED', 'COMPLETED', 'CANCELLED')),
    driver_id UUID,
    vehicle_id UUID,
    assignment_source VARCHAR(24) CHECK (assignment_source IN ('LOCAL', 'POSITIONING', 'LUNARIS')),
    CHECK (status NOT IN ('ASSIGNED', 'COMPLETED') OR
        (driver_id IS NOT NULL AND vehicle_id IS NOT NULL AND assignment_source IS NOT NULL))
);
CREATE INDEX interurban_trip_schedule ON interurban.trips(service_date, direction, departure_at);

CREATE TABLE interurban.trip_legs (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES interurban.trips(id),
    corridor_leg_id UUID NOT NULL REFERENCES interurban.corridor_legs(id),
    capacity SMALLINT NOT NULL DEFAULT 4 CHECK (capacity = 4),
    UNIQUE (trip_id, corridor_leg_id),
    UNIQUE (id, trip_id)
);

CREATE TABLE interurban.reservations (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL,
    trip_id UUID NOT NULL REFERENCES interurban.trips(id),
    origin_stop SMALLINT NOT NULL CHECK (origin_stop BETWEEN 0 AND 3),
    destination_stop SMALLINT NOT NULL CHECK (destination_stop BETWEEN 0 AND 3),
    passenger_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    dropoff_address VARCHAR(255) NOT NULL,
    pickup_at TIMESTAMPTZ,
    dropoff_at TIMESTAMPTZ,
    fare_id UUID NOT NULL REFERENCES interurban.interurban_fares(id),
    fare NUMERIC(12,2) NOT NULL CHECK (fare >= 0),
    commission NUMERIC(12,2) NOT NULL CHECK (commission >= 0 AND commission <= fare),
    status VARCHAR(24) NOT NULL CHECK (status IN ('HELD', 'PAID', 'CHECKED_IN', 'EXPIRED', 'CANCELLED', 'REFUNDED')),
    hold_expires_at TIMESTAMPTZ NOT NULL,
    checked_in_at TIMESTAMPTZ,
    checked_in_driver_id UUID,
    CHECK (origin_stop <> destination_stop),
    CHECK (status <> 'CHECKED_IN' OR (checked_in_at IS NOT NULL AND checked_in_driver_id IS NOT NULL)),
    UNIQUE (id, trip_id)
);
CREATE INDEX interurban_reservation_booking ON interurban.reservations(booking_id);
CREATE INDEX interurban_reservation_trip ON interurban.reservations(trip_id, pickup_at);
CREATE INDEX interurban_hold_expiry ON interurban.reservations(hold_expires_at) WHERE status = 'HELD';

-- La PK y el CHECK impiden un quinto pasajero incluso con escritores concurrentes.
-- No hay contador duplicado: la ocupación se deriva de estas filas.
CREATE TABLE interurban.leg_seats (
    trip_id UUID NOT NULL,
    trip_leg_id UUID NOT NULL,
    seat SMALLINT NOT NULL CHECK (seat BETWEEN 1 AND 4),
    reservation_id UUID NOT NULL,
    PRIMARY KEY (trip_leg_id, seat),
    UNIQUE (trip_leg_id, reservation_id),
    FOREIGN KEY (trip_leg_id, trip_id) REFERENCES interurban.trip_legs(id, trip_id),
    FOREIGN KEY (reservation_id, trip_id) REFERENCES interurban.reservations(id, trip_id)
);
CREATE INDEX interurban_seat_reservation ON interurban.leg_seats(reservation_id);

CREATE TABLE interurban.payment_inbox (
    id UUID PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL UNIQUE,
    payment_id VARCHAR(128) NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    last_error VARCHAR(500)
);
CREATE TABLE interurban.payments (
    id UUID PRIMARY KEY,
    payment_id VARCHAR(128) NOT NULL UNIQUE,
    booking_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    currency CHAR(3) NOT NULL CHECK (currency = 'ARS'),
    status VARCHAR(32) NOT NULL,
    verified_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE interurban.qr_tokens (
    reservation_id UUID PRIMARY KEY REFERENCES interurban.reservations(id),
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ
);

CREATE TABLE interurban.payout_orders (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL,
    settlement_date DATE NOT NULL,
    amount NUMERIC(14,2) NOT NULL CHECK (amount > 0),
    status VARCHAR(24) NOT NULL CHECK (status IN ('PENDING', 'SENDING', 'UNKNOWN', 'PAID', 'FAILED')),
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    provider_reference VARCHAR(128) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (driver_id, settlement_date)
);
CREATE TABLE interurban.driver_ledger (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL,
    reservation_id UUID REFERENCES interurban.reservations(id),
    payout_order_id UUID REFERENCES interurban.payout_orders(id),
    entry_type VARCHAR(24) NOT NULL CHECK (entry_type IN ('EARNED', 'REVERSAL', 'PAYOUT')),
    amount NUMERIC(14,2) NOT NULL,
    event_key VARCHAR(160) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((entry_type = 'EARNED' AND amount >= 0 AND reservation_id IS NOT NULL AND payout_order_id IS NULL)
        OR (entry_type = 'REVERSAL' AND amount <= 0 AND reservation_id IS NOT NULL AND payout_order_id IS NULL)
        OR (entry_type = 'PAYOUT' AND amount < 0 AND payout_order_id IS NOT NULL AND reservation_id IS NULL))
);
CREATE UNIQUE INDEX interurban_one_earning ON interurban.driver_ledger(reservation_id) WHERE entry_type = 'EARNED';
CREATE UNIQUE INDEX interurban_one_payout ON interurban.driver_ledger(payout_order_id) WHERE entry_type = 'PAYOUT';
CREATE INDEX interurban_driver_balance ON interurban.driver_ledger(driver_id, created_at);
CREATE TABLE interurban.payout_items (
    ledger_id UUID PRIMARY KEY REFERENCES interurban.driver_ledger(id),
    payout_order_id UUID NOT NULL REFERENCES interurban.payout_orders(id)
);
CREATE FUNCTION interurban.reject_ledger_mutation() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'driver_ledger es inmutable; registrar un asiento compensatorio';
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER interurban_immutable_ledger BEFORE UPDATE OR DELETE ON interurban.driver_ledger
    FOR EACH ROW EXECUTE FUNCTION interurban.reject_ledger_mutation();

CREATE TABLE interurban.outbox (
    id UUID PRIMARY KEY,
    event_key VARCHAR(160) NOT NULL UNIQUE,
    event_type VARCHAR(48) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0)
);
CREATE INDEX interurban_pending_outbox ON interurban.outbox(created_at) WHERE delivered_at IS NULL;
