-- La agenda usa public.reservations. trip_type ya expresa ONE_WAY/ROUND_TRIP/OPEN_RETURN.
-- No modifica el catálogo special_trips ni el módulo interurban.
ALTER TABLE reservations
    ADD COLUMN trip_category VARCHAR(16) NOT NULL DEFAULT 'REGULAR',
    ADD COLUMN origin_custom VARCHAR(100),
    ADD COLUMN destination_custom VARCHAR(100),
    ADD COLUMN custom_price NUMERIC(10,2),
    ADD COLUMN payment_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN invoice_issued BOOLEAN NOT NULL DEFAULT FALSE;

-- passenger_count ya existe. Se conservan los pagos y facturas históricos.
UPDATE reservations SET payment_status = 'PAID' WHERE payment_verified = TRUE;
UPDATE reservations r SET invoice_issued = TRUE
WHERE EXISTS (
    SELECT 1 FROM invoices i JOIN reservations billed ON billed.id = i.reservation_id
    WHERE billed.id = r.id
       OR (r.booking_group_code IS NOT NULL AND billed.booking_group_code = r.booking_group_code)
);

ALTER TABLE reservations
    ADD CONSTRAINT reservation_trip_category_valid CHECK (trip_category IN ('REGULAR', 'SPECIAL')),
    ADD CONSTRAINT reservation_payment_status_valid CHECK (payment_status IN ('PENDING', 'PAID')),
    ADD CONSTRAINT reservation_special_details_valid CHECK (
        trip_category <> 'SPECIAL' OR (
            origin_custom IS NOT NULL AND LENGTH(TRIM(origin_custom)) > 0
            AND destination_custom IS NOT NULL AND LENGTH(TRIM(destination_custom)) > 0
            AND passenger_count IS NOT NULL AND passenger_count > 0
            AND custom_price IS NOT NULL AND custom_price > 0
        )
    ),
    ADD CONSTRAINT reservation_special_invoice_requires_payment CHECK (
        trip_category <> 'SPECIAL' OR NOT invoice_issued
        OR (payment_status = 'PAID' AND payment_verified = TRUE)
    );

CREATE INDEX reservations_special_agenda_date ON reservations (travel_date, departure_schedule)
    WHERE trip_category = 'SPECIAL';
