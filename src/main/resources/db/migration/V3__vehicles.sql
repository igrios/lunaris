CREATE TABLE vehicles (
    id UUID PRIMARY KEY,
    plate VARCHAR(20) NOT NULL,
    capacity INTEGER NOT NULL,
    active BOOLEAN NOT NULL
);