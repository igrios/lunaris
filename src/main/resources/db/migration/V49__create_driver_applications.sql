CREATE TABLE driver_applications (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    vehicle_model VARCHAR(120) NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_driver_applications_status
    ON driver_applications (status);
