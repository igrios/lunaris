CREATE TABLE IF NOT EXISTS waiting_list_entries (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(30) NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    travel_date DATE NOT NULL,
    pickup_locality VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    passenger_count INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_waiting_list_date_status
    ON waiting_list_entries (travel_date, status);
