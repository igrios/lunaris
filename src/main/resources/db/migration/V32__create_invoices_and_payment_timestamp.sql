-- 🧾 Módulo de Facturación + registro de ingreso de dinero
-- 1. Marca temporal del momento exacto en que se confirma el pago (cuándo ingresó la plata)
ALTER TABLE reservations ADD COLUMN payment_confirmed_at TIMESTAMP;

-- 2. Registro de facturas emitidas (la factura la arma la operadora aparte y se sube en PDF)
CREATE TABLE invoices (
    id                  UUID PRIMARY KEY,
    reservation_id      UUID NOT NULL REFERENCES reservations(id),
    invoice_number      VARCHAR(40),
    passenger_name      VARCHAR(200),
    passenger_cuil      VARCHAR(20),
    amount              NUMERIC(12,2),
    pdf_url             VARCHAR(300),
    sent_via_whatsapp   BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at             TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_reservation_id ON invoices(reservation_id);
