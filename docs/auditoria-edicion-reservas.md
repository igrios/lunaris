# Auditoría y propuesta: edición integral de reservas

Fecha: 2026-11-11. Alcance: análisis del árbol de trabajo actual, incluidos cambios locales preexistentes. No se modifica la implementación. El proyecto declara Java 21 y Spring Boot 3.5.14 en `pom.xml`.

## 1. Diagnóstico verificable

No existe un `UpdateReservationUseCase` ni un `UpdateReservationRequest` en el flujo auditado. La edición manual reutiliza la creación:

| Capa | Implementación actual | Observación |
| --- | --- | --- |
| API administrativa | `ManualTripApiController`, `POST /api/admin/trips` | Recibe `CreateReservationForm`; si llega `reservationId`, responde 200 y edita. |
| Formulario web/chat | `BotMonitorController`, `POST /monitor/cargar-reserva-web` y carga asistida | Construye una entidad y delega en el mismo caso de uso. |
| Mapeo | `ManualTripWebMapper` y `ManualReservationOptions` | Traslada `reservationId` al ID; infiere `TripType` únicamente desde `roundTrip` y `returnDate`. No transporta configuración flexible. |
| Caso de uso | `CreateManualReservationUseCase.execute`, líneas 21–39 | Transacción exterior; valida, guarda el pasajero y decide crear/editar por ID. |
| Restricción | `ReservationService.updateManualReservationFlow`, líneas 108–119 | Compara `group.size()` con 1 o 2 según `roundTrip` y lanza el mensaje informado. |
| Otra edición | `ReservationViewController.updateFromPanel`, línea 296 | Programa vueltas abiertas mediante `scheduleOpenReturn`; para otras reservas llama a `updateReservation`. |
| Persistencia | `ReservationRepository` y `CapacityLockRepository` | Bloqueos pesimistas de filas/grupo y claves de capacidad; conteos que excluyen IDs del grupo editado. |

La restricción es una regla explícita del servicio. Quitar solamente el `if` no resuelve la operación: el código posterior itera únicamente las filas existentes, divide los importes según su cantidad y considera regreso a cualquier fila distinta de la seleccionada.

### Riesgos encontrados

1. **Identidad de los tramos:** la fila indicada por `input.id` se trata como principal, aunque pudiera ser el regreso. Un grupo puede superar dos filas: `scheduleOpenReturn` divide regresos parciales y conserva `bookingGroupCode`. El tamaño del grupo no representa su topología ni sus tramos activos. Las consultas de grupo también incluyen cancelados.
2. **Flexibilidad perdida:** la creación llama a `validateFlexibleReturn` y distingue fecha estimada de efectiva. La edición manual no llama a esa validación, no copia los campos flexibles e infiere `ROUND_TRIP` ante cualquier fecha de regreso. Puede convertir una estimación en programación efectiva.
3. **Cupos:** la edición manual excluye los IDs propios y bloquea destinos nuevos, lo cual evita autocontarse. No bloquea explícitamente todos los recursos anteriores y nuevos en un orden global. No suma la demanda propuesta cuando varias filas comparten recurso. `updateReservation` sólo valida capacidad ante reactivación manual; un cambio ordinario de fecha o cantidad no tiene esa comprobación.
4. **Políticas divergentes:** `ReturnCapacityPolicy` retiene vueltas abiertas antes de las 11:00 en los bloques del día asociado; `findReturnCapacityCandidates` enlaza algunas vueltas con la ida mediante sufijos del código. No distingue explícitamente la modalidad flexible. Por ello no debe asumirse que una vuelta flexible nunca afecta cupos: la documentación y la consulta requieren reconciliación.
5. **Horarios:** existen strings con formatos distintos. El conteo de ida compara prefijos, mientras otras rutas normalizan horas. Un horario personalizado debe representar un recurso de transporte definido; no puede implicar capacidad independiente por el mero hecho de tener otro texto.
6. **Tarifa y pagos:** `validateManualReservation` calcula tarifa sólo si `amount == null`. Un monto reenviado puede conservar una tarifa anterior tras cambiar trayecto/cantidad. La edición propaga “pagado” si cualquier tramo estaba pagado, aun cuando el nuevo total aumente. También reinicia estados de viaje a `SCHEDULED`/`OPEN_RETURN`.
7. **Notificaciones:** el snapshot no incluye preferencia flexible, flag flexible ni todos los atributos editables. Un cambio exclusivo de esos campos puede no generar auditoría ni aviso. Existe un listener manual `AFTER_COMMIT` que conviene conservar.

## 2. Caso de uso propuesto

Introducir `UpdateReservationUseCase` con inyección por constructor y `@Transactional`, separado de la creación. Los controladores deben convertir transporte a comando y delegar. Centralizar también las modificaciones de fecha/cantidad de la agenda en la misma política de cupos.

Resolver el grupo por `bookingGroupCode` y conservar fallback legado. Identificar ida y regresos por identidad y dirección explícitas, sin interpretar la fila seleccionada como ida. Para grupos con splits, el comando debe admitir `targetLegId`: modificar el bloque indicado o crear uno nuevo, preservando los otros. La baja de todo el regreso debe nombrar explícitamente ese alcance y validar cada bloque; nunca borrar silenciosamente regresos ya realizados.

| Solicitud | Resultado esperado |
| --- | --- |
| Conservar regreso | No modificar su configuración; revalidar si cambia un dato compartido que lo afecta. |
| Agregar regreso | Crear fila vinculada, con UUID generado y fallback defensivo; mantener ID y código de la ida. |
| Modificar regreso | Mantener su ID; cambiar fecha, modalidad, horario o preferencia y recalcular recursos afectados. |
| Dar de baja regreso | Cancelación lógica auditada; conservar historial y referencias, liberar su ocupación y actualizar modalidad del grupo. |
| Repetir solicitud | No duplicar filas, créditos ni avisos; usar identificador de operación para altas/reintentos. |

Conservar las protecciones por viaje completado, facturación emitida y aviso en curso. Añadir validaciones explícitas de estado para cada transición y conservar estados operativos que no deban cambiar. La cancelación de un tramo dentro del grupo no debe volver a impedir todas las ediciones por el simple hecho de existir como historial.

## 3. Secuencia transaccional y concurrencia

1. Obtener un bloqueo estable del agregado/grupo, incluso cuando todavía tiene una sola fila. Bloquear las filas relevantes en orden determinista y verificar una revisión esperada para detectar formularios obsoletos. El bloqueo pesimista por sí solo no evita sobrescribir datos enviados desde una pantalla antigua.
2. Construir un plan inmutable con estado anterior y propuesto, sin mutar entidades JPA todavía. Validar itinerario, fechas, pasajeros, modalidades y estados.
3. Resolver los recursos de capacidad anteriores y nuevos. Canonicalizar fecha, sentido y servicio/bloque. Mantener el bloqueo diario de regreso mientras siga existiendo retención entre bloques.
4. Bloquear la unión de recursos en orden global. Todas las rutas de creación, edición, cancelación, reactivación y programación deben respetar una jerarquía común de bloqueos; coordinar también bloqueos de pasajeros cuando exista movimiento de saldo.
5. Recontar después de obtener los bloqueos: `ocupación ajena + demanda propuesta agregada <= capacidad`. Excluir únicamente las filas sustituidas por el plan. Los splits intactos deben seguir contándose exactamente una vez. Validar todos los bloques afectados por una vuelta abierta.
6. Calcular el precio autoritativo del estado final. Aplicar altas, cambios y cancelaciones; guardar eventos y revisión; ejecutar `flush` y confirmar. Cualquier fallo revierte reservas, cupos lógicos, importes y movimientos económicos.
7. Publicar la notificación después del commit; para entrega durable y reintentos, utilizar outbox transaccional.

Los cupos actuales se derivan de filas y estados: no existe aquí un contador que deba incrementarse/decrementarse manualmente. Se liberan al cambiar fecha/horario o cancelar una fila y se ocupan con el estado nuevo. La exclusión de filas propias y los bloqueos permiten comprobar ese reemplazo antes de persistirlo.

**Decisión propuesta para flexibilidad:** una fecha estimada o preferencia no garantiza una butaca. El regreso flexible conserva `travelDate = null`, `departureSchedule = null`, `travelStatus = OPEN_RETURN`, fecha estimada en `returnDate` y preferencia textual. Ajustar explícitamente la consulta/política para excluirlo de la retención preventiva; mantener la retención existente para vueltas abiertas tradicionales. Al fijar fecha y horario efectivos, validar capacidad en esa misma transacción. Si el negocio exige garantía para flexibles, debe asignarse un recurso explícito y contabilizarlo.

**Tarifa:** distinguir `RECALCULATE` de `AGREED_TOTAL`, con permiso y motivo para el total acordado. Reutilizar `PricingAndScheduleService` y las reglas de viaje especial/promociones; repartir centavos por diferencia para conservar el total. Mantener el contrato de facturación `SUM(amount + extraAmount)` de los tramos aplicables, sin duplicar extras ni descontar nuevamente lo ya aplicado. Definir el filtro de cancelados en consumidores de `BookingInvoiceAmount`, que no lo hace por sí mismo. Registrar diferencia de cobro/crédito y saldo aplicado; no marcar automáticamente como pagado un aumento. No reutilizar una cancelación que acredite saldo y además devolver la misma diferencia mediante la edición.

## 4. Contrato de actualización compatible

Agregar `PATCH /api/admin/reservations/{id}` con `UpdateReservationRequest`. Mantener temporalmente los POST existentes mediante adaptadores al nuevo caso de uso. En `SecurityConfig`, autorizar la edición para `ADMIN` y `OPERADOR` antes de reglas más generales; conservar permisos específicos de facturación/pagos.

Ejemplo de modificación a horario manual:

```json
{
  "expectedRevision": 7,
  "operationId": "a9655a96-2b48-4788-97ce-296a8d064ee0",
  "returnChange": {
    "action": "UPDATE",
    "targetLegId": "3ca5ac07-b619-4971-b2ab-2c446d68cb7b",
    "mode": "SCHEDULED",
    "date": "2026-11-15",
    "customTime": "18:45",
    "timePreference": "Retiro por terminal"
  },
  "pricing": { "mode": "RECALCULATE" }
}
```

Diseñar como records Java 21: `UpdateReservationRequest`, `ReturnChangeRequest`, `PricingChangeRequest`, con enums `ReturnAction` (`KEEP`, `ADD`, `UPDATE`, `REMOVE`) y `ReturnMode` (`SCHEDULED`, `OPEN`, `FLEXIBLE`). `REMOVE` debe incluir ID o alcance explícito para grupos divididos. No exponer la entidad JPA como contrato.

Semántica:

- `returnChange` ausente o nulo equivale a `KEEP`; nunca cancelar por la ausencia de un booleano. Para cambios parciales de otros campos usar presencia explícita —un wrapper con estado ausente/nulo/valor— cuando se permita borrar su contenido.
- `ADD`/`UPDATE` describen la configuración completa del regreso afectado. `UPDATE` y `REMOVE` requieren una identidad válida del grupo; un `ADD` no puede duplicar accidentalmente un tramo existente.
- `SCHEDULED`: fecha efectiva y exactamente uno entre horario de catálogo y `customTime`; validar/normalizar `HH:mm` y su recurso de capacidad.
- `FLEXIBLE`: fecha estimada y/o preferencia obligatorias, sin horario fijo. `OPEN` representa la vuelta abierta tradicional. No inferir modalidad sólo por la existencia de una fecha.
- Fecha de regreso no anterior a la ida; preferencia de hasta 500 caracteres, sin controles; pasajeros entre 1 y 9 y límites de splits; reglas cruzadas en dominio además de Bean Validation.
- Traducir `returnDate`, `returnDepartureSchedule`, `returnCustomTime`, `isFlexibleReturnTime` y sus aliases desde contratos anteriores. Si llegan formas nuevas y antiguas contradictorias, rechazar; no elegir silenciosamente una. El POST legado sigue siendo formulario completo; su `roundTrip` debe mapearse de manera explícita a la transición correspondiente.
- Respuesta: grupo, tramos activos, nueva revisión, total y diferencia pendiente/crédito. Reutilizar `ApiExceptionHandler`: 400 para validación, 409 para cupos/conflicto de revisión y 404 para reserva inexistente; agregar excepciones específicas donde falten.

## 5. Repositorios, migraciones y verificación

Conservar consultas de grupo como listas y búsquedas por ID como `Optional`. Un retorno `Optional` no corrige por sí mismo una consulta potencialmente no única: usar unicidad real o `findFirstBy` donde corresponda. Separar historial de tramos activos y reemplazar inferencias por sufijos por vínculos explícitos cuando sea necesario; migrar/backfillear registros legados sin renombrar códigos públicos existentes.

Revisar índices por grupo y recursos de capacidad; incorporar revisión/bloqueo estable del agregado e idempotencia mediante migración si se adopta ese diseño. Los campos de flexibilidad ya existen: no requieren duplicarse. Mantener `@GeneratedValue(strategy = GenerationType.UUID)` y fallback anterior a `save` para las nuevas entidades. Reservar `@Transactional(readOnly = true)` para consultas sin bloqueos de escritura.

Pruebas necesarias para implementar: ida→ida/vuelta; baja de vuelta; fijo↔abierto↔flexible; fecha estimada sin programación; hora manual; edición desde el ID de regreso; grupos divididos e historial cancelado; cambio de cantidad; tarifa, extras, descuentos y centavos; diferencias de pago; rollback por falta de cupos en cualquiera de los tramos; repetición sin duplicación; avisos sólo por cambios reales; preservación de códigos y UUID; protección de facturados/completados y permisos.

Validar carreras reales contra PostgreSQL —altas simultáneas por el último cupo, ediciones cruzadas entre turnos, cancelación frente a reubicación y adición concurrente de vuelta— porque los mocks no demuestran la corrección de los bloqueos. La suite actual incluye edición conservando IDs/códigos y totales en `ManualReservationIntegrationTest`, pero eso no demuestra las nuevas transiciones.

Orden recomendado: normalizar identidad y política de capacidad; implementar plan y caso de uso transaccional; agregar DTO/adaptadores; ajustar consumidores de facturación y notificación; conectar los formularios; ejecutar pruebas funcionales y de concurrencia. Retirar el mensaje restrictivo al completar las transiciones y su verificación.

Validación de la auditoría: `./mvnw test` finalizó con `BUILD SUCCESS`: 854 pruebas reportadas, 0 fallos, 0 errores y 13 omitidas. Entre las omitidas hay pruebas PostgreSQL; este resultado no demuestra las garantías de concurrencia propuestas ni implementa las nuevas transiciones.
