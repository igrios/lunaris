UPDATE accounts
SET password_hash = '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a.',
    active = true
WHERE upper(username) = upper('ignacio');
