CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(30) NOT NULL,
    message_text TEXT NOT NULL,
    is_from_operator BOOLEAN NOT NULL DEFAULT false,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);