INSERT INTO localities (name)
VALUES ('Aeropuerto Córdoba')
ON CONFLICT (name) DO NOTHING;