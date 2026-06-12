ALTER TABLE reservations
ADD COLUMN round_trip BOOLEAN DEFAULT FALSE;

ALTER TABLE reservations
ADD COLUMN return_date DATE;

ALTER TABLE reservations
ADD COLUMN extra_amount NUMERIC(12,2);