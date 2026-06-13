CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE localities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE fares (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    locality_name VARCHAR(100) NOT NULL,
    amount NUMERIC(12,2) NOT NULL
);