ALTER TABLE promotion_usages RENAME COLUMN used_at TO created_at;

WITH duplicated_usages AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY promotion_id, REGEXP_REPLACE(phone_number, '[^0-9]', '', 'g')
               ORDER BY created_at, id
           ) AS row_number
    FROM promotion_usages
)
DELETE FROM promotion_usages
WHERE id IN (SELECT id FROM duplicated_usages WHERE row_number > 1);

UPDATE promotion_usages
SET phone_number = REGEXP_REPLACE(phone_number, '[^0-9]', '', 'g');

ALTER TABLE promotion_usages
    ADD CONSTRAINT chk_promotion_usage_phone_digits
    CHECK (phone_number ~ '^[0-9]+$');
