ALTER TABLE news_banners
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS event_type VARCHAR(100),
    ADD COLUMN IF NOT EXISTS has_waiting_list BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE news_banners
SET event_type = UPPER(REGEXP_REPLACE(TRIM(title), '[^A-Za-z0-9]+', '_', 'g'))
WHERE event_type IS NULL OR TRIM(event_type) = '';

ALTER TABLE news_banners
    ALTER COLUMN event_type SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_news_banners_event_type
    ON news_banners (event_type);
