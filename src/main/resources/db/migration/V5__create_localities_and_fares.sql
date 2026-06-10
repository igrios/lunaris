CREATE TABLE localities (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE fares (
    id UUID PRIMARY KEY,
    locality_name VARCHAR(100) NOT NULL,
    amount NUMERIC(12,2) NOT NULL
);
