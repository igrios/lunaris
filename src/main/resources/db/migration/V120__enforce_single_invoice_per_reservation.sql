WITH duplicated_invoices AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY reservation_id
               ORDER BY created_at DESC NULLS LAST, id
           ) AS row_number
    FROM invoices
)
DELETE FROM invoices
WHERE id IN (
    SELECT id FROM duplicated_invoices WHERE row_number > 1
);

DROP INDEX IF EXISTS idx_invoices_reservation_id;

CREATE UNIQUE INDEX uk_invoices_reservation_id
    ON invoices(reservation_id);
