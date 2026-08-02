CREATE TABLE news_banners (
    id UUID PRIMARY KEY,
    image_url VARCHAR(500) NOT NULL,
    title VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    valid_until DATE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_news_banners_active_valid_until
    ON news_banners (active, valid_until);
