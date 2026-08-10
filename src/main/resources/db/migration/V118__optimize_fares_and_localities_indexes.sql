UPDATE localities
SET name = TRIM(name);

UPDATE fares
SET locality_name = TRIM(locality_name);

CREATE INDEX IF NOT EXISTS idx_fares_locality_amount
    ON fares (UPPER(locality_name), amount);
