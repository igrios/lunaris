CREATE TABLE passengers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    cuil VARCHAR(20) NOT NULL,
    phone VARCHAR(30),
    address VARCHAR(255),
    locality VARCHAR(100)
);