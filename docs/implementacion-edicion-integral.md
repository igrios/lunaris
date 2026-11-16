# Caso de uso de edición integral

`UpdateReservationUseCase` implementa la capa de aplicación. Los controladores actuales todavía no delegan en él: su conexión corresponde a la siguiente capa de la entrega.

## Uso

1. Consultar `getState(reservationId)` para obtener el grupo, sus tramos y la revisión vigente.
2. Enviar esa revisión en `UpdateReservationRequest.expectedRevision`, con un `operationId` único por intención de edición.
3. Invocar `execute(reservationId, request, operator)`. Existe una sobrecarga con operador `ADMIN_PANEL` para adaptadores internos.

Se puede invocar usando el ID de la ida o de un regreso. `ReturnChangeRequest.targetLegId` identifica el bloque concreto al modificar o cancelar un regreso dividido. `ADD` exige que no haya regresos activos; un historial cancelado no impide un alta nueva. `KEEP` o un `returnChange` ausente conservan la configuración del itinerario.

`SCHEDULED` exige fecha efectiva y `customTime` en `HH:mm`. Un turno de catálogo debe resolverse a esa hora antes de construir el comando; estos DTOs no contienen un ID de catálogo. `OPEN` mantiene fecha y horario pendientes. `FLEXIBLE` admite fecha estimada y/o preferencia, conserva `travelDate` y `departureSchedule` nulos y no garantiza cupos. Los viajes facturados, utilizados, asignados a chofer o con un aviso en curso se protegen contra la edición.

## Revisión e idempotencia

La migración `V139__reservation_integral_update.sql` agrega:

- `reservation_update_states`: identidad estable de la ida, revisión y huella de los datos de negocio.
- `reservation_update_operations`: solicitud identificada por hash, operador, fecha y snapshots anterior/resultante.
- `reservations.recognized_paid_amount`: dinero externo reconocido cuando queda un saldo pendiente.
- Ampliación de `booking_group_code` a 64 caracteres para grupos legados identificados por UUID.

El orden de bloqueo del nuevo caso de uso es grupo → filas ordenadas → unión ordenada de recursos anteriores/nuevos → pasajero si corresponde acreditar saldo. El mutex usa la tabla existente `reservation_capacity_locks` con prefijo `GROUP|`. Los bloqueos de cupos conservan las mismas claves que la creación: fecha/horario para ida y fecha/día para regreso.

La revisión inicial es 0. Una operación que cambia el estado incrementa la revisión una vez. Los cambios de negocio hechos por escritores legados se detectan mediante la huella al consultar/editar el grupo. Se normaliza la escala de `BigDecimal` para evitar conflictos falsos. Las modificaciones técnicas de notificación no incrementan esa revisión. `getState` tiene una transacción de escritura porque inicializa o sincroniza el registro de revisión; no es una consulta `readOnly`.

La primera identificación de una ida legada utiliza su código sin sufijo, o su fecha de creación anterior a los regresos. Si las fechas son ambiguas, rechaza la edición para evitar invertir los tramos. Luego conserva el UUID principal explícitamente.

Repetir el mismo `operationId` y contenido devuelve el resultado original, incluso si ya existe una revisión posterior. Reutilizarlo con otro contenido o presentar una revisión obsoleta produce `ReservationUpdateConflictException` (409). La reserva inexistente produce 404. El adaptador debe recargar el grupo después de un conflicto.

## Cupos

El plan trabaja con copias y revalida antes de fusionar entidades. Excluye las filas propias del conteo y suma la demanda propuesta por recurso. Las vueltas abiertas tradicionales conservan la política preventiva de bloques; las flexibles quedan fuera de esa retención. La consulta compartida de regresos reconoce el vínculo explícito al principal para los grupos editados, además del vínculo legado por sufijos.

La baja es lógica (`CANCELLED`/`CANCELED`) y conserva el registro histórico. La liberación de cupos resulta de los estados/fechas persistidos; no se modifica un contador manual de asientos.

## Precios y pagos

- `pricing` ausente conserva importes en `KEEP` y `UPDATE`; en `ADD` y `REMOVE` recalcula automáticamente.
- `RECALCULATE` usa la tarifa vigente, promociones existentes, extras del grupo y precio personalizado en viajes especiales. Contempla una parte de pasajeros con regreso y otra con sólo ida.
- `AGREED_TOTAL.agreedAmount` es el total del grupo, incluidos extras y saldo previamente aplicado. No puede ser menor a los extras que se conservan. Reemplaza los descuentos promocionales anteriores.
- El reparto por asientos usa redondeo hacia abajo y asigna el remanente al último tramo, conservando los centavos del total.
- Se conserva el saldo ya aplicado, sin volver a debitar la billetera. El excedente efectivamente pagado se acredita una sola vez al pasajero. La parte pendiente permanece como deuda y se informa en la respuesta.
- `UpdateReservationResult.total` incluye el saldo aplicado; `invoiceTotal` corresponde al contrato existente de suma de `amount + extraAmount` de tramos activos. `priceDifference`, `pendingAmount` y `creditedAmount` explicitan la reliquidación.
- Una reducción no usa la cancelación general en cascada: evita acreditar dos veces. Las cancelaciones posteriores reconocen también pagos parciales guardados por una edición.

Los importes legados que repiten el total del grupo en cada tramo requieren normalización previa antes de reliquidar; se rechazan para evitar duplicar pagos. Los registros históricos cancelados se conservan y deben excluirse de cualquier cálculo de facturación del estado activo.

Los servicios actuales de registro de pago y emisión fiscal filtran ese historial cancelado. La persistencia de facturas revalida el importe vigente bajo bloqueo para impedir emitir con una tarifa leída antes de una edición concurrente.

## Auditoría, eventos y límites de integración

Cada operación confirmada queda registrada con snapshots y resultado. Los cambios generan `RESERVATION_UPDATED` y un snapshot `OperatorNotification`, que el listener existente procesa con `AFTER_COMMIT`. Un rollback no emite la notificación ni conserva créditos, cambios u operaciones. Ese despachador no constituye un outbox durable: mantiene las garantías actuales de envío del proyecto.

Los escritores legados conservan su jerarquía anterior de bloqueos; la huella detecta sus cambios, pero la eliminación global de posibles deadlocks requiere migrarlos al nuevo caso de uso. PostgreSQL puede abortar una transacción en conflicto; ningún reintento debe cambiar el `operationId` de la misma intención.

## Verificación

`UpdateReservationUseCaseTest` cubre las transiciones, historial cancelado, splits, revisión obsoleta, pagos reconocidos, centavos, rollback y notificaciones posteriores al commit.

`UpdateReservationPostgresTest` usa bloqueos reales y verifica altas simultáneas al mismo grupo, repetición simultánea de una operación, competencia por el último asiento, movimientos cruzados entre fechas y acreditaciones concurrentes al mismo pasajero con saldo precargado. Se habilita con `RESERVATION_UPDATE_TEST_JDBC_URL` y opcionalmente `RESERVATION_UPDATE_TEST_DB_USER` / `RESERVATION_UPDATE_TEST_DB_PASSWORD`. Requiere una base **desechable**: Hibernate crea y elimina el esquema de entidades para la prueba.

Verificación realizada: V139 aplicada correctamente sobre PostgreSQL 16 temporal; `./mvnw test` con PostgreSQL habilitado finalizó con `BUILD SUCCESS`: 874 pruebas reportadas, 0 fallos, 0 errores y 13 omitidas de la suite existente. Pasaron las 15 pruebas funcionales nuevas y las cinco de concurrencia PostgreSQL. `git diff --check` no reportó problemas.
