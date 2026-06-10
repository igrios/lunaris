CREATE TABLE reservations (

    id UUID PRIMARY KEY,

    passenger_id UUID NOT NULL,

    travel_date DATE NOT NULL,

    pickup_locality VARCHAR(100) NOT NULL,

    pickup_address VARCHAR(255),

    destination VARCHAR(100) NOT NULL,

    payment_verified BOOLEAN NOT NULL,

    notes VARCHAR(500),

    CONSTRAINT fk_reservation_passenger
        FOREIGN KEY (passenger_id)
        REFERENCES passengers(id)
);