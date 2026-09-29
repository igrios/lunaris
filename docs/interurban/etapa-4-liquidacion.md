# Etapa 4: corte nocturno y liquidación contable

Implementación en `com.lunaris.ansenuza.service.interurban`.

## Activación y corte

Aplicar V131 después de V129 y V130. Habilitar explícitamente:

```properties
lunaris.interurban.enabled=true
lunaris.interurban.settlement.enabled=true
```

`DailyDriverPayoutJob` ejecuta `0 0 22 * * *`, zona `America/Argentina/Cordoba`.
Al recibir `ApplicationReadyEvent` recupera el último corte vencido: antes de las
22:00 usa el día anterior. Incluye créditos pendientes de días anteriores; no
reconstruye una orden por cada día que la aplicación estuvo detenida.

## Integridad contable

`DriverSettlementService.settle(fecha)` rechaza cortes futuros. Una transacción
PostgreSQL toma un advisory lock común a las réplicas y bloquea los asientos en
orden de chofer/UUID. La serialización global es deliberada para esta etapa;
puede limitar el rendimiento con grandes volúmenes.

Selecciona EARNED y REVERSAL con `created_at <= corte`, sin vínculo en
`payout_items`. El crédito EARNED ya contiene tarifa menos comisión del check-in.
Cada boleto aporta una vez, independientemente de sus segmentos. El neto suma
créditos y compensaciones con `BigDecimal`/`NUMERIC(14,2)`.

Para neto positivo se crean juntos:

- Una orden PENDING única por `(driver_id, settlement_date)` y clave estable
  `interurban:payout:<driver UUID>:<fecha>`.
- Sus items, con `ledger_id` único para impedir reutilizar un crédito.
- Un evento outbox `PAYOUT_READY` único.

La orden guarda tarifa bruta, comisión y ajustes por separado. V131 conserva
NULL para órdenes históricas sin desglose; no inventa snapshots anteriores.
Neto cero o negativo permanece sin reservar hasta contar con saldo positivo.
Una orden existente no recibe nuevos items: los asientos pendientes pasan a un
corte posterior. No se modifican asientos ni se genera PAYOUT en esta etapa.

## Puerto y revisión interna

`DriverPayoutPort` define envío de lotes y conciliación por clave idempotente.
`SimulatedDriverPayoutAdapter` únicamente registra que la orden está lista para
aprobación y devuelve READY_FOR_APPROVAL; nunca invoca APIs externas.

Después del commit, `DriverPayoutReviewService` consume los eventos pendientes.
Si falla, quedan disponibles para la siguiente ejecución o reinicio. Una caída
entre log y confirmación de outbox puede repetir el log con la misma clave. La
orden sigue PENDING; `delivered_at` significa revisión notificada, no dinero
transferido. No se almacenan datos bancarios en logs.

La integración real requiere un worker separado con reclamo, estado UNKNOWN,
conciliación antes de reenvío y confirmación PAID + asiento PAYOUT atómicos.
El despachador de revisión interna no implementa ese ciclo ni debe conectarse
directamente a un proveedor real.

## Verificación

Pruebas JUnit 5 de zona horaria y límite de corte, repetición del job y contrato
simulado. Las pruebas PostgreSQL crean boletos PAID, ejecutan el check-in real y
verifican desglose decimal, items, repetición, concurrencia, exclusión de asientos
posteriores al corte, compensaciones, saldo no positivo y rollback ante fallo de
outbox.

Para ejecutar integración, usar una base **desechable** con V129–V131 aplicadas:

```bash
INTERURBAN_TEST_JDBC_URL=jdbc:postgresql://localhost:5432/interurban_test \
INTERURBAN_TEST_DB_USER=ignacio INTERURBAN_TEST_DB_PASSWORD=... ./mvnw test
```

Sin esa variable, las pruebas PostgreSQL se omiten explícitamente.

Resultado verificado el 2026-09-29: V129–V131 aplicadas sobre PostgreSQL 16
temporal; `./mvnw test` con integración habilitada terminó en **BUILD SUCCESS:
613 tests, 0 fallos, 0 errores, 0 omitidos**. Incluye 3 pruebas unitarias/de
configuración y 5 pruebas PostgreSQL nuevas de liquidación.
