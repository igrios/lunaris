# Propuesta de clases Java 21 / Spring Boot

Primera etapa implementada en `src/main/java/com/lunaris/ansenuza/service/interurban`: `Route`, `PassengerHold`, `CapacityService`, `CapacityRepository`, `JdbcCapacityRepository`, excepciones de dominio y `InterurbanConfiguration`. Los fragmentos siguientes conservan el diseño de referencia de las etapas restantes; el código ejecutable es el de `src/main/java`. Ver [uso de la primera etapa](etapa-1.md).

Paquete recomendado: `com.lunaris.ansenuza.service.interurban` (equivalente funcional al `com.lunaris.service.interurban` solicitado, compatible con escaneo vigente).

La [etapa 2 de pagos y QR](etapa-2-pagos-qr.md) implementa además webhook autenticado, consulta de pago, inbox recuperable, confirmación transaccional, generación ZXing y almacenamiento cifrado de pases. Asignación y entrega siguen siendo propuestas.

La [etapa 4 de liquidación](etapa-4-liquidacion.md) implementa el corte de las 22:00, órdenes e items atómicos, outbox y revisión interna mediante un puerto de pago simulado. Las transferencias reales siguen pendientes de habilitación del proveedor.

| Clase | Responsabilidad / puertos |
|---|---|
| `CapacityService` | Resolver segmentos, consultar disponibilidad read-only, reservar/liberar atómicamente; `TripRepository`, `SeatRepository`, `Clock`. |
| `InterurbanBookingService` | Validar ruta, tarifa vigente más reciente anterior a fecha solicitada, grupo, hold y snapshots; coordinar capacidad. |
| `AutomaticDriverAssignmentService` | Cerrar viajes vencidos, agrupar intervalos y asignar local/posicionamiento/flota; `FleetAvailabilityPort`, `AssignmentRepository`, `OutboxPort`. |
| `MercadoPagoWebhookService` / `PaymentReconciliationService` | Autenticar evento e inbox; consultar `PaymentQueryPort`, verificar pago, transición idempotente y outbox. |
| `QrGeneratorService` | `SecureRandom`, hash, ZXing vía `QrRendererPort`, almacenamiento privado cifrado; emisión/revocación. |
| `CheckInService` | Bloquear reserva/QR, autorizar chofer, validar pago y ventana; consumo+crédito exactamente una vez en BD. |
| `DriverRouteSheetQuery` | `@Transactional(readOnly=true)`, proyección DTO del chofer autenticado, paradas cronológicas. |
| `DailyDriverPayoutJob` / `DriverSettlementService` | Disparador del corte; transacción de orden, items y outbox; no llamadas HTTP dentro del lock. |
| `PayoutWorker` | `DriverPayoutPort`, idempotencia, reintentos y conciliación de UNKNOWN. |
| `InterurbanExceptionHandler` | `@ControllerAdvice` acotado a controladores del módulo; `CapacityExceeded`, `BookingClosed`, `InvalidRoute`, `QrExpired`, `AssignmentUnavailable`. |

Esqueleto del núcleo de capacidad (los puertos indicados son contratos por implementar):

```java
public record Route(int origin, int destination) {
    public Route {
        if (origin < 0 || origin > 3 || destination < 0 || destination > 3
                || origin == destination) throw new InvalidRoute();
    }
    public List<Integer> ordinals() {
        return IntStream.rangeClosed(Math.min(origin, destination) + 1,
                Math.max(origin, destination)).boxed().toList();
    }
    public int direction() { return Integer.signum(destination - origin); }
}

// Bean registrado sólo por configuración condicional del módulo.
public class CapacityService {
    private final CapacityRepository repository;
    private final Clock clock;

    public CapacityService(CapacityRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public int available(UUID tripId, Route route) {
        var trip = repository.getTrip(tripId);
        trip.validateRoute(route); // sentido y segmentos completos
        var legs = repository.getLegs(tripId, route.ordinals());
        requireAllLegs(legs, route); // ausencia de fila no equivale a libre
        return legs.stream().mapToInt(l -> 4 - l.occupied()).min().orElseThrow();
    }

    @Transactional
    public void hold(UUID tripId, Route route, List<NewPassengerBooking> passengers) {
        if (passengers.isEmpty() || passengers.size() > 4) throw new InvalidPartySize();
        var trip = repository.lockTrip(tripId);
        trip.assertOpenAt(clock.instant());
        trip.validateRoute(route);
        var legs = repository.lockLegsAscending(tripId, route.ordinals());
        requireAllLegs(legs, route);
        if (legs.stream().anyMatch(l -> l.freeSeats().size() < passengers.size())) {
            throw new CapacityExceeded();
        }
        // Cada pasajero lleva tarifa/commission snapshot y UUID nuevo.
        // La transacción también incluye insert de reservas y todos los legs.
        for (var passenger : passengers) repository.insertReservation(tripId, route, passenger);
        for (var leg : legs) {
            var seats = leg.freeSeats().iterator();
            for (var passenger : passengers) {
                repository.insertSeat(tripId, leg.id(), seats.next(), passenger.id());
            }
        }
    }
}
```

La plaza es un cupo lógico por segmento, no una butaca física fija. La implementación bloquea el viaje y después los tramos por ID ascendente mediante `ORDER BY tl.id FOR UPDATE OF tl`, conforme al requerimiento de etapa 1; los ordinales sólo determinan qué tramos pertenecen a la ruta. Todas las futuras operaciones que modifican ocupación deben usar ese mismo orden de locks. Una violación de unicidad no se captura para continuar dentro de una transacción abortada.

Asignación propuesta:

```java
@Transactional
public AssignmentResult closeAndAssign(UUID tripId, Instant now) {
    var trip = trips.lock(tripId);
    if (trip.isAssigned()) return trip.assignment();
    trip.requireCutoffReached(now);
    // Expirar holds no pagados; cerrar en forma persistente.
    // Obtener candidatos ordenados LOCAL, POSITIONING, LUNARIS.
    // Reclamar recurso con exclusión atómica y verificar agenda/tiempos.
    // Si falta recurso: CLOSED + incidencia/outbox y resultado Pending.
    // Si hay recurso: ASSIGNED + driver/vehicle + hoja de ruta/outbox.
    return assignments.assignOrRecordPending(trip);
}
```

No lanzar excepción que revierta CLOSED al fallar el fallback: persistir pendiente y reintentar. El agrupamiento entre varios viajes debe bloquear todos los viajes en orden UUID y recalcular capacidad antes de mover reservas; las FK compuestas exigen liberar/reinsertar claims en la misma transacción.

Disparadores aislados, sin activar globalmente nuevos componentes:

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "lunaris.interurban", name = "enabled",
        havingValue = "true", matchIfMissing = false)
class InterurbanConfiguration {
    // @Bean con constructor injection para servicios, jobs y adapters.
    // Sin @ComponentScan global ni cambios a servicios de Córdoba.
}

public class DailyDriverPayoutJob {
    private final DriverSettlementService settlements;
    private final Clock clock;
    public DailyDriverPayoutJob(DriverSettlementService settlements, Clock clock) {
        this.settlements = settlements;
        this.clock = clock;
    }
    @Scheduled(cron = "0 0 22 * * *", zone = "America/Argentina/Cordoba")
    public void run() {
        settlements.createMissingOrdersThrough(clock.instant());
    }
}

public interface DriverPayoutPort {
    TransferResult submit(PayoutOrder order, String idempotencyKey);
    TransferResult reconcile(String idempotencyKey);
}
```

`createMissingOrdersThrough` calcula cortes pendientes hasta las 22:00 local y usa claves únicas por chofer/fecha; debe tener también un disparador de recuperación. El proveedor debe soportar reconciliación real: el puerto no implica que Mercado Pago ofrezca ese contrato. Los repositorios usan unicidad real para retornos Optional; Optional por sí solo NO evita resultados múltiples. Tarifas: consulta ordenada `valid_from DESC`, limitada a una fila. Colecciones para resultados legítimamente múltiples.
