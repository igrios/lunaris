ALTER TABLE reservations ADD COLUMN promotion_id UUID;
ALTER TABLE reservations ADD COLUMN promotion_discount_percentage INTEGER;

UPDATE reservations reservation
SET promotion_id = promotion.id,
    promotion_discount_percentage = promotion.discount_percentage
FROM promotions promotion
WHERE reservation.promotion_code = promotion.code;

ALTER TABLE promotion_usages DROP CONSTRAINT IF EXISTS uk_promotion_usage_phone;

UPDATE promotion_usages
SET phone_number = REGEXP_REPLACE(phone_number, '^549', '54');

WITH duplicated_usages AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY promotion_id, phone_number
               ORDER BY created_at, id
           ) AS row_number
    FROM promotion_usages
)
DELETE FROM promotion_usages
WHERE id IN (SELECT id FROM duplicated_usages WHERE row_number > 1);

ALTER TABLE promotion_usages
    ADD CONSTRAINT uk_promotion_usage_phone UNIQUE (promotion_id, phone_number);
