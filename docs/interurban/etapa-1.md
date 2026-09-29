# Capacidad interurbana: primera etapa

Activación explícita: `lunaris.interurban.enabled=true`. Con propiedad ausente o false no se registran `CapacityService` ni `JdbcCapacityRepository`. No hay cambios en servicios, entidades, rutas HTTP, seguridad, jobs, dependencias ni propiedades de Córdoba. Flyway aplica V129 independientemente de este flag.

V129 conserva el esquema aditivo de la auditoría, incluyendo tablas auxiliares para etapas posteriores. Capacidad exactamente 4 en `trip_legs`, rango 1..4 y unicidad de plaza/reserva en `leg_seats`, ledger protegido contra UPDATE/DELETE por trigger. No se modificó la migración previamente entregada. El rol de aplicación no debe tener permisos de DDL/TRUNCATE sobre el ledger.

El puerto `CapacityRepository` tiene implementación JDBC real con SQL calificado `interurban.*`; no se introducen entidades JPA ni se amplía su escaneo. Los UUID de nuevas reservas se generan explícitamente antes del insert. Transacciones Spring con el datasource existente; consumir siempre el bean administrado para que se apliquen los proxies transaccionales.

```java
Route route = new Route(0, 3); // SG -> Brinkmann, ordinales 1,2,3
int available = capacityService.available(tripId, route);

PassengerHold passenger = new PassengerHold(
    bookingId, fareId, "Ana Pérez", "+5493562000000",
    "Dirección de origen", "Dirección de destino",
    new BigDecimal("1000.00"), new BigDecimal("100.00"));

List<UUID> reservationIds = capacityService.hold(
    tripId, route, List.of(passenger), Instant.now().plusSeconds(600));
```

`available` es orientativo, read-only, y devuelve el mínimo de cupos libres. Viaje cerrado o con cutoff alcanzado devuelve 0; recorrido inválido, incompleto o de otro sentido lanza `InvalidRouteException`.

`hold` valida grupo de 1..4 pasajeros, mismo bookingId y vencimiento futuro no posterior al cutoff. Bloquea viaje y todos los segmentos por ID antes de leer ocupación; crea reservas individuales HELD y claims de todos los segmentos sólo después de validar todos los cupos. Falta de lugar: `CapacityExceededException`; cierre: `BookingClosedException`; datos/vencimiento inválidos: `InvalidHoldException`. Cualquier fallo de persistencia revierte toda la transacción.

El llamador es un futuro caso de uso de booking interno: debe resolver tarifa vigente y verificar correspondencia de fareId, ruta e importes; los snapshots NO deben recibirse directamente de un cliente público. Preparación de viajes/tramos, selección de tarifas, liberación/expiración de holds, pagos, asignación y controladores quedan para etapas posteriores. Hasta implementar liberación, los holds vencidos siguen ocupando cupo de forma conservadora.

Cada llamada exitosa a `hold` crea reservas nuevas: no reintentar ciegamente tras un timeout de cliente. La idempotencia de comandos de booking debe añadirse antes de exponer ventas; bookingId agrupa pasajeros y no constituye una clave idempotente implementada. No hay endpoints públicos nuevos ni un ControllerAdvice global que altere manejo de errores de Córdoba.

Validación:

```bash
./mvnw test
```

`CapacityServiceTest` verifica mínimo, rutas parciales/vuelta, recorrido incompleto, orden de operaciones, cupo total, cutoff y datos de grupo. `InterurbanConfigurationTest` verifica flag ausente, false y true.

Pruebas adicionales con PostgreSQL real, exclusivamente contra base desechable con V129 ya aplicada:

```bash
INTERURBAN_TEST_JDBC_URL=jdbc:postgresql://localhost:55439/postgres \
INTERURBAN_TEST_DB_USER=usuario \
./mvnw test
```

Opcional: `INTERURBAN_TEST_DB_PASSWORD`. `CapacityPostgresTest` usa proxies Spring y verifica cinco compradores concurrentes (cuatro aceptados, uno rechazado) y rollback al fallar la segunda reserva. Sin la variable JDBC estas dos pruebas se omiten; el resto de la suite se ejecuta normalmente. No usar una base compartida o productiva: los fixtures de integración se conservan en la base desechable.
