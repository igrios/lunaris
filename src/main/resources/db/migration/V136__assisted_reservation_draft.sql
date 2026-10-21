CREATE TABLE assisted_reservation_drafts (
    id UUID PRIMARY KEY,
    phone VARCHAR(15) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
