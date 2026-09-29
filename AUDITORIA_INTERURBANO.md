# Corredor Ansenuza: Auditoría Técnica y Propuesta de Integración Non-Breaking

**Fecha:** 2026-09-20  
**Alcance:** Diagnóstico de código existente, script de migración SQL aditivo (`V129`) y propuesta de servicios para el Corredor Interurbano (San Guillermo ↔ Suardi ↔ Morteros ↔ Brinkmann).  
**Garantía:** Cero modificaciones sobre el flujo actual de viajes a Córdoba (compatibilidad hacia atrás 100%).

---

## 1. Diagnóstico del Repositorio Actual
* **Stack Tecnológico:** Java 21, Spring Boot 3.5.14, JPA, PostgreSQL, Flyway.
* **Estructura Existente:** La lógica actual de reservas de larga distancia a Córdoba convive en la base de datos `public.reservations` y tablas de tarifas tradicionales (`fares`).
* **Principio de Aislamiento:** Para evitar conflictos con el servicio de Córdoba, el nuevo módulo se encapsula en el paquete `com.lunaris.ansenuza.service.interurban` y las nuevas tablas se crean bajo un esquema aislado o mediante entidades aditivas independientes.

---

## 2. Script de Migración SQL Flyway (`V129__add_interurban_corridor.sql`)
El script crea el esquema y tablas necesarias para el corredor regional sin alterar, modificar ni borrar ningún objeto existente en producción:

```sql
-- Crear esquema o tablas aditivas para el corredor Ansenuza
CREATE SCHEMA IF NOT EXISTS interurban;

-- 1. Tramos del Corredor (SG=0, SUA=1, MOR=2, BRI=3)
CREATE TABLE IF NOT EXISTS interurban.corridor_legs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    origin_ordinal INT NOT NULL,
    destination_ordinal INT NOT NULL,
    origin_name VARCHAR(100) NOT NULL,
    destination_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- 2. Matriz de Tarifas por Tramo
CREATE TABLE IF NOT EXISTS interurban.interurban_fares (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    origin_stop INT NOT NULL,
    destination_stop INT NOT NULL,
    valid_from DATE NOT NULL,
    fare_amount DECIMAL(10, 2) NOT NULL,
    platform_commission DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'ARS'
);

-- 3. Control de Viajes y Capacidad Estricta por Tramo (Máx. 4 pax)
CREATE TABLE IF NOT EXISTS interurban.trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_date DATE NOT NULL,
    departure_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closes_at TIMESTAMP WITH TIME ZONE NOT NULL, -- 20:00 hs del día anterior
    direction INT NOT NULL, -- 1: Norte-Sur (SG->BRI), -1: Sur-Norte (BRI->SG)
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    driver_id UUID,
    vehicle_id UUID
);

CREATE TABLE IF NOT EXISTS interurban.trip_legs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL REFERENCES interurban.trips(id),
    corridor_leg_id UUID NOT NULL,
    capacity INT DEFAULT 4 CHECK (capacity = 4),
    UNIQUE(trip_id, corridor_leg_id)
);

CREATE TABLE IF NOT EXISTS interurban.leg_seats (
    trip_id UUID NOT NULL,
    trip_leg_id UUID NOT NULL,
    seat_number INT NOT NULL CHECK (seat_number BETWEEN 1 AND 4),
    reservation_id UUID NOT NULL,
    PRIMARY KEY (trip_leg_id, seat_number)
);

-- 4. Reservas y Tokens QR (ZXing)
CREATE TABLE IF NOT EXISTS interurban.reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL,
    trip_id UUID NOT NULL REFERENCES interurban.trips(id),
    origin_stop INT NOT NULL,
    destination_stop INT NOT NULL,
    passenger_name VARCHAR(150) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    pickup_address TEXT NOT NULL,
    dropoff_address TEXT NOT NULL,
    fare DECIMAL(10, 2) NOT NULL,
    commission DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'HELD', -- HELD, PAID, CHECKED_IN, CANCELLED
    hold_expires_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS interurban.qr_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID NOT NULL UNIQUE REFERENCES interurban.reservations(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE
);

-- 5. Ledger Contable y Liquidación Nocturna (22:00 hs)
CREATE TABLE IF NOT EXISTS interurban.driver_ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    reservation_id UUID NOT NULL REFERENCES interurban.reservations(id),
    entry_type VARCHAR(20) NOT NULL, -- EARNED, PAYOUT_COMPENSATION
    amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    event_key VARCHAR(100) NOT NULL UNIQUE
);
