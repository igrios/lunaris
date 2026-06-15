ALTER TABLE reservations
ADD COLUMN status VARCHAR(50);

UPDATE reservations
SET status = 'PENDING_PAYMENT'
WHERE status IS NULL;