CREATE TABLE drivers (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    active BOOLEAN NOT NULL,
    ranking INTEGER
);