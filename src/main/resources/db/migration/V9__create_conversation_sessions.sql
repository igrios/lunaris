CREATE TABLE conversation_sessions (

    id BIGSERIAL PRIMARY KEY,

    phone_number VARCHAR(30) NOT NULL UNIQUE,

    current_step VARCHAR(50),

    last_interaction TIMESTAMP
);