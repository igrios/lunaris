CREATE TABLE special_trips (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    origin VARCHAR(100),
    destination VARCHAR(100),
    start_date DATE,
    end_date DATE,
    price DECIMAL(10,2),
    max_passengers INT,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_special_trips_active_start_date
    ON special_trips (active, start_date);
