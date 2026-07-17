CREATE TABLE IF NOT EXISTS system_configurations (
    key VARCHAR PRIMARY KEY,
    value TEXT
);

INSERT INTO system_configurations (key, value) VALUES
    ('return.scheduler.time', '15:00'),
    ('return.message.header', 'Confirmación de vuelta'),
    ('return.message.body', 'Hola, ¿confirmás tu vuelta de hoy con Lunaris Ansenuza?
Elegí una opción para que podamos organizar las butacas.'),
    ('return.button.yes.title', 'SÍ, VOLVER ✅'),
    ('return.button.later.title', 'OTRO DÍA 📅'),
    ('return.button.no.title', 'NO, CANCELAR ❌'),
    ('session.inactivity.timeout.minutes', '30')
ON CONFLICT (key) DO NOTHING;
