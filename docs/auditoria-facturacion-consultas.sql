-- Propuesta de diagnóstico. NO ejecutada durante la auditoría.
-- Ejecutar sólo en la base/esquema verificados, con usuario de lectura.
-- No incluye nombres, documentos, teléfonos, URLs ni descripción libre de eventos.
BEGIN TRANSACTION READ ONLY;
SET LOCAL statement_timeout = '10s';

-- Estado del grupo comunicado y facturas asociadas a TODOS sus tramos.
WITH legs AS (
    SELECT id, reservation_code, booking_group_code, amount, custom_price,
           extra_amount, discount_amount, used_balance, amount_is_group_total,
           passenger_count, status, payment_status, payment_verified,
           payment_confirmed_at, requires_invoice, invoice_issued
    FROM reservations
    WHERE booking_group_code = 'ESP-42AB5ACE1'
       OR reservation_code IN ('ESP-42AB5ACE1-IDA', 'ESP-42AB5ACE1-VUELTA')
)
SELECT l.*, i.id AS invoice_id, i.amount AS registered_amount,
       i.authorization_code IS NOT NULL AS has_authorization_reference,
       COALESCE(LENGTH(TRIM(i.pdf_url)), 0) > 0 AS has_pdf_reference,
       i.sent_via_whatsapp, i.sent_at, i.created_at AS invoice_created_at
FROM legs l LEFT JOIN invoices i ON i.reservation_id = l.id;

-- Eventos sin contenido libre ni identidad del operador.
SELECT e.reservation_id, e.event_type, COUNT(*) AS event_count
FROM reservation_events e
JOIN reservations r ON r.id = e.reservation_id
WHERE r.booking_group_code = 'ESP-42AB5ACE1'
   OR r.reservation_code IN ('ESP-42AB5ACE1-IDA', 'ESP-42AB5ACE1-VUELTA')
GROUP BY e.reservation_id, e.event_type;

-- Banderas y duplicados grupales: la asociación directa no basta para la vuelta.
WITH operations AS (
    SELECT id, invoice_issued,
           COALESCE(NULLIF(TRIM(booking_group_code), ''),
                    REGEXP_REPLACE(reservation_code, '-(IDA|VUELTA)$', ''),
                    'UUID:' || id::text) AS operation_key
    FROM reservations
), invoice_counts AS (
    SELECT o.operation_key, COUNT(i.id) AS invoice_count
    FROM operations o LEFT JOIN invoices i ON i.reservation_id = o.id
    GROUP BY o.operation_key
)
SELECT o.operation_key, COUNT(*) AS reservation_count, c.invoice_count,
       BOOL_OR(o.invoice_issued) AS any_marked_issued,
       BOOL_AND(o.invoice_issued) AS all_marked_issued
FROM operations o JOIN invoice_counts c USING (operation_key)
GROUP BY o.operation_key, c.invoice_count
HAVING (BOOL_OR(o.invoice_issued) AND c.invoice_count = 0)
    OR (NOT BOOL_AND(o.invoice_issued) AND c.invoice_count > 0)
    OR c.invoice_count > 1;

-- Número interno repetido y documentos ausentes/no enviados (sólo conteos).
SELECT invoice_number, COUNT(*) AS duplicates
FROM invoices WHERE invoice_number IS NOT NULL
GROUP BY invoice_number HAVING COUNT(*) > 1;
SELECT COUNT(*) FILTER (WHERE COALESCE(LENGTH(TRIM(pdf_url)), 0) = 0) AS without_pdf_reference,
       COUNT(*) FILTER (WHERE sent_via_whatsapp IS NOT TRUE) AS without_send_record,
       COUNT(*) FILTER (WHERE authorization_code IS NULL) AS without_authorization_reference
FROM invoices;

-- Restricción aplicada; no modifica índices ni ejecuta migraciones.
SELECT indexname, indexdef FROM pg_indexes
WHERE schemaname = CURRENT_SCHEMA() AND tablename = 'invoices';
SELECT version, description, success FROM flyway_schema_history
WHERE version IN ('120', '128', '133');

ROLLBACK;
