UPDATE accounts
SET password_hash = '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a.',
    active = true
WHERE upper(username) = upper('ignacio');

INSERT INTO accounts (id, username, display_name, password_hash, active)
SELECT
    'a1530000-0000-4000-8000-000000000001',
    'ignacio',
    'Ignacio',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a.',
    true
WHERE NOT EXISTS (
    SELECT 1
    FROM accounts
    WHERE upper(username) = upper('ignacio')
);

INSERT INTO account_roles (account_id, role)
SELECT id, 'ADMIN'
FROM accounts
WHERE upper(username) = upper('ignacio')
ON CONFLICT (account_id, role) DO NOTHING;
