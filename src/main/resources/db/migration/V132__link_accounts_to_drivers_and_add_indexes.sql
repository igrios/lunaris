-- 1. Agregar columna account_id a public.drivers con restricción UNIQUE
ALTER TABLE public.drivers
    ADD COLUMN IF NOT EXISTS account_id UUID REFERENCES public.accounts(id);

ALTER TABLE public.drivers
    ADD CONSTRAINT uq_drivers_account UNIQUE (account_id);

-- 2. Índice para acelerar la resolución de chofer activo
CREATE INDEX IF NOT EXISTS idx_drivers_account_active
    ON public.drivers(account_id)
    WHERE active = TRUE;

-- 3. Índices de performance operativos para route-sheet
CREATE INDEX IF NOT EXISTS idx_interurban_trips_driver_date
    ON interurban.trips(driver_id, service_date)
    WHERE status IN ('ASSIGNED', 'COMPLETED');

CREATE INDEX IF NOT EXISTS idx_interurban_payments_approved_booking
    ON interurban.payments(booking_id)
    WHERE status = 'APPROVED';

-- 4. Unicidad de username insensible a mayúsculas
CREATE UNIQUE INDEX IF NOT EXISTS idx_accounts_username_upper_unique
    ON public.accounts(UPPER(username));
