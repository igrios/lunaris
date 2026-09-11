CREATE TABLE inquiries (
    id UUID PRIMARY KEY,
    passenger_id UUID REFERENCES passengers(id),
    phone VARCHAR(255) NOT NULL,
    passenger_name VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'RESOLVED', 'ARCHIVED')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_inquiries_created_at ON inquiries(created_at DESC);
CREATE INDEX idx_inquiries_status ON inquiries(status);
CREATE INDEX idx_inquiries_passenger_id ON inquiries(passenger_id);
