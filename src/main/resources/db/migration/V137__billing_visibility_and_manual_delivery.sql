ALTER TABLE reservations ADD COLUMN manual_confirmation_sent_at TIMESTAMP,
    ADD COLUMN manual_invoice_delivered_url TEXT;

UPDATE reservations SET payment_verified = TRUE, payment_status = 'PAID', requires_invoice = TRUE,
    payment_confirmed_at = COALESCE(payment_confirmed_at, CURRENT_TIMESTAMP), payment_expires_at = NULL
WHERE payment_verified = TRUE OR payment_status = 'PAID';

UPDATE reservations SET requires_invoice = TRUE
WHERE UPPER(COALESCE(status, '')) = 'CONFIRMED'
   OR (payment_receipt_url IS NOT NULL AND TRIM(payment_receipt_url) NOT IN ('', 'null'));

UPDATE reservations r SET manual_invoice_delivered_url = invoice_url
WHERE invoice_url IS NOT NULL AND EXISTS (
    SELECT 1 FROM invoices i JOIN reservations billed ON billed.id = i.reservation_id
    WHERE i.sent_via_whatsapp = TRUE
      AND (billed.id = r.id OR (r.booking_group_code IS NOT NULL AND billed.booking_group_code = r.booking_group_code))
);

-- Un PDF entregado por el flujo anterior acredita que su confirmación ya fue enviada.
UPDATE reservations SET manual_confirmation_sent_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP)
WHERE manual_invoice_delivered_url IS NOT NULL;
