CREATE TABLE operator_notification_phones (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO operator_notification_phones (id, name, phone, active)
VALUES ('b653034c-f32f-4cf9-a6e1-4c53ea1c0c8d', 'Ignacio', '5493512282251', TRUE);
