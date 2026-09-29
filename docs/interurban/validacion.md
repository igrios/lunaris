# Validación de la propuesta

## Segunda etapa: pagos y QR (2026-09-20)

- `./mvnw test` con `INTERURBAN_TEST_JDBC_URL` hacia PostgreSQL temporal: BUILD SUCCESS, **579 tests, 0 fallos, 0 errores, 0 omitidos**.
- V130 ejecutada correctamente sobre PostgreSQL 16.14, después de V129; no se modificaron migraciones previas.
- Prueba unitaria completa: webhook firmado → inbox → consulta API simulada → PAID → PNG cifrado; PNG descifrado y leído con ZXing, token de 32 bytes y hash SHA-256 verificados.
- Concurrencia PostgreSQL: dos eventos del mismo pago generan un único pago y un pase por pasajero.
- Rollback PostgreSQL: fallo en el segundo QR revierte pago, estados, tokens y artefactos, conservando la notificación pendiente para reintento.
- Tests de firma HMAC, cliente HTTP, controlador, revocación e independencia de flags. Sin credenciales reales ni transacciones externas.
- Configuración, operación y límites: [etapa-2-pagos-qr.md](etapa-2-pagos-qr.md).

## Primera etapa de código (2026-09-20)

- `./mvnw test` con PostgreSQL temporal habilitado mediante `INTERURBAN_TEST_JDBC_URL`: BUILD SUCCESS, **552 tests, 0 fallos, 0 errores, 0 omitidos**.
- 17 casos unitarios de capacidad y rutas, 3 pruebas de configuración condicional y 2 pruebas de integración PostgreSQL, además de los 530 tests preexistentes.
- Concurrencia PostgreSQL: cinco compradores sobre tres tramos → cuatro aceptados, uno rechazado, cuatro claims por tramo.
- Atomicidad PostgreSQL: fallo de FK en la segunda reserva → rollback de la primera; ninguna reserva ni claim persistido para el viaje de prueba.
- Verificados beans ausentes con flag ausente/false y registro de servicio/repositorio con flag true. No se cambiaron archivos productivos existentes del flujo Córdoba.

## Auditoría inicial

- `./mvnw test`: BUILD SUCCESS, 530 tests, 0 fallos, 0 errores, 0 omitidos (2026-09-20).
- V129 ejecutada con `psql -v ON_ERROR_STOP=1` sobre PostgreSQL 16.14 temporal: esquema, 13 tablas, índices, función, trigger y tres segmentos creados correctamente.
- `validar-restricciones.sql`: prueba reproducible de rechazo de quinta plaza, plaza duplicada, capacidad distinta de cuatro, doble crédito y modificación del ledger; fixtures revertidas con ROLLBACK.

Reproducción contra una base **desechable** ya disponible:

```bash
psql "$INTERURBAN_TEST_DATABASE_URL" -v ON_ERROR_STOP=1 -f src/main/resources/db/migration/V129__add_interurban_corridor.sql
psql "$INTERURBAN_TEST_DATABASE_URL" -v ON_ERROR_STOP=1 -f docs/interurban/validar-restricciones.sql
./mvnw test
```

Esta prueba SQL directa valida DDL y restricciones, no el ciclo completo de Flyway ni la convivencia sobre una copia productiva. Los resultados anteriores corresponden a la auditoría inicial. La primera etapa de capacidad ya cuenta con código y tests: ver [etapa-1.md](etapa-1.md). Siguen pendientes integración real de proveedor, endpoints y regresión sobre copia productiva. No se modificaron clases, configuración ni dependencias existentes del flujo Córdoba.
