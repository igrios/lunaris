DELETE FROM account_roles
WHERE account_id IN (
    SELECT id
    FROM accounts
    WHERE lower(username) = 'ignacio'
);

DELETE FROM accounts
WHERE lower(username) = 'ignacio';

INSERT INTO accounts (id, username, password_hash, active, display_name)
VALUES (
    'a1990000-0000-4000-8000-000000000001',
    'ignacio',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a.',
    true,
    'Ignacio Admin'
);

INSERT INTO account_roles (account_id, role)
SELECT id, 'ADMIN'
FROM accounts
WHERE lower(username) = 'ignacio';
