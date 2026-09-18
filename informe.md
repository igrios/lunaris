# Informe: adopción del chatbot de Lunaris Ansenuza

Fecha: 17 de septiembre de 2026.

**Estado: auditoría y diseño propuesto, pendientes de aprobación.** No se implementó telemetría ni se ejecutaron las migraciones o consultas de este informe.

## Resumen

Ya existen datos para aproximar contactos y medir reservas por canal, pero no para reconstruir un funnel histórico confiable.

La propuesta mínima es agregar dos tablas: sesiones analíticas y eventos de interacción. La conversación operativa sigue usando `ConversationSession`; las reglas de negocio y el flujo funcional del bot se conservan.

El objetivo es identificar dónde se interrumpe el recorrido y distinguir desinterés, consulta informativa, falta de disponibilidad, derivación humana y problemas técnicos.

## Alcance y evidencia

- Se inspeccionaron código, entidades, repositorios y migraciones del repositorio.
- No se consultó PostgreSQL de producción. La existencia efectiva de tablas, su volumen, retención y calidad histórica deben verificarse allí.
- Se ejecutó `./mvnw test` durante la auditoría: **492 pruebas, cero fallos, cero errores, BUILD SUCCESS**.
- Los ejemplos SQL son propuestas para revisión, no consultas ejecutadas ni resultados reales.

## 1. Auditoría del flujo actual

### Arquitectura

El proyecto utiliza Java 21, Spring Boot 3.5.14, Spring Data JPA, PostgreSQL y Flyway. El chatbot se organiza en orquestador, handlers por paso, puertos de mensajería y adaptadores de infraestructura.

La arquitectura existente combina separación por capas con entidades JPA dentro de `domain/model` y repositorios Spring Data. La telemetría debe respetar esa estructura sin introducir un framework adicional.

Existe además un módulo `reservation/` con API v2 y otro modelo de reserva. El chatbot utiliza `domain/model/Reservation` y `domain/model/service/ReservationService`, no el servicio de creación del módulo v2.

### Entrada de WhatsApp

Archivos principales:

- [WhatsAppWebhookController](src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppWebhookController.java)
- [WhatsAppWebhookParser](src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppWebhookParser.java)
- [WhatsAppWebhookInboxService](src/main/java/com/lunaris/ansenuza/application/usecase/WhatsAppWebhookInboxService.java)
- [WhatsAppMessageDispatcher](src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppMessageDispatcher.java)

Recorrido:

1. `POST /whatsapp/webhook` verifica la firma del payload.
2. El parser construye un `IncomingMessage` con teléfono normalizado, identificador y tipo de mensaje.
3. `WhatsAppWebhookInboxService.claim()` deduplica por `message_id` mediante una transacción independiente.
4. `WhatsAppMessageDispatcher` procesa asíncronamente, ordenando por teléfono dentro de la instancia.
5. Las imágenes con comprobante se derivan a `ProcessPaymentReceiptUseCase`; los mensajes con cuerpo, a `ConversationOrchestrator`.

Limitaciones observadas:

- El parser toma el primer mensaje de la estructura recibida, no recorre todos los mensajes de un payload múltiple.
- Los estados de entrega y lectura no se procesan como historial analítico.
- Reclamar un mensaje en el inbox no demuestra que su procesamiento terminó correctamente.
- Una caída después del claim puede dejar el mensaje reclamado sin procesar; un reenvío con el mismo ID será deduplicado.

### Estado de conversación

Archivos principales:

- [ConversationOrchestrator](src/main/java/com/lunaris/ansenuza/application/conversation/ConversationOrchestrator.java)
- [ConversationSession](src/main/java/com/lunaris/ansenuza/domain/model/ConversationSession.java)
- [ConversationSessionCleanupScheduler](src/main/java/com/lunaris/ansenuza/application/conversation/ConversationSessionCleanupScheduler.java)

`conversation_sessions` contiene una fila por teléfono, con `currentStep`, `lastInteraction`, datos parciales del viaje, pausa del bot y operador asignado. El orquestador distingue choferes, comandos especiales, atención humana y recorrido de pasajeros.

**Es estado operativo mutable, no un historial de sesiones.** Las filas se eliminan al completar una reserva, al rechazar la cotización y por inactividad.

La limpieza usa `session.inactivity.timeout.minutes`, con 30 minutos como valor predeterminado, y corre cada 10 minutos por defecto. Excluye sesiones con `botPaused=true`. No conserva etapa ni motivo del cierre.

### Recorrido principal

```text
Menú
 ├─ Reservar ─────┐
 └─ Ver precios ─┴─ Localidad → Cotización → Desea reservar
                    → Horario → Datos del pasajero/domicilio
                    → Destino → Modalidad → Fechas
                    → DNI/CUIT → Promoción → Resumen → Confirmación
```

El orden varía: desde Córdoba se elige destino antes de cotizar; pasajeros existentes reutilizan información; aeropuerto y lista de espera tienen tratamientos particulares. No corresponde imponer un único funnel estrictamente lineal a todas las ramas.

### Creación de reservas

Archivos principales:

- [ConfirmationHandler](src/main/java/com/lunaris/ansenuza/application/conversation/steps/ConfirmationHandler.java)
- [ReservationService](src/main/java/com/lunaris/ansenuza/domain/model/service/ReservationService.java)
- [CreateReservationUseCase](src/main/java/com/lunaris/ansenuza/application/usecase/CreateReservationUseCase.java)
- [ConfirmWaitingListBookingHandler](src/main/java/com/lunaris/ansenuza/application/conversation/steps/ConfirmWaitingListBookingHandler.java)
- [WaitingListConversionService](src/main/java/com/lunaris/ansenuza/application/usecase/WaitingListConversionService.java)

`ConfirmationHandler` procesa `confirm_ok`, construye una reserva con `source=WHATSAPP` y llama a `ReservationService.saveReservationFlow()` dentro de una transacción.

El servicio persiste los tramos y asigna el mismo `booking_group_code` a ida y vuelta. También crea eventos `RESERVATION_CREATED` por tramo. Por lo tanto, **dos filas o dos eventos de creación pueden representar una sola conversión**.

Aceptar el resumen normalmente genera `PENDING_PAYMENT`, no `CONFIRMED`. Los viajes de aeropuerto generan `PENDING`; promociones o saldo disponible pueden producir confirmación inmediata.

El panel usa `saveManualReservationFlow()`, que asigna `MANUAL`. El caso de uso de creación general usa el origen recibido o `WEB` por defecto.

**Excepción de atribución:** la aceptación de una lista de espera desde WhatsApp llama a `WaitingListConversionService.beginPayment()`, pero ese servicio crea la reserva con `source=MANUAL`. No se puede interpretar todo `MANUAL` como ausencia de intervención del bot.

### Historial y pruebas internas

[WebSocketLiveChatAdapter](src/main/java/com/lunaris/ansenuza/infrastructure/chat/WebSocketLiveChatAdapter.java) persiste mensajes entrantes en `chat_messages` con teléfono, texto y fecha. Los mensajes del operador se distinguen mediante `is_from_operator`.

No es un registro completo de respuestas del bot. Además, algunos payloads se transforman en textos legibles, perdiendo parte de su contexto. Por ejemplo, `no_cancel` recibe una descripción que no corresponde a todos sus usos.

[WhatsAppSimulatorDevController](src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppSimulatorDevController.java) usa el mismo orquestador. El valor `WHATSAPP` no distingue por sí solo tráfico real de pruebas.

[WhatsAppMessagingAdapter](src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppMessagingAdapter.java) descarta el resultado booleano del envío. [WhatsAppService](src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppService.java) puede diferir envíos hasta después del commit y devolver antes de la llamada efectiva a Meta. Cambiar de paso no demuestra que la respuesta llegó al usuario.

## 2. Qué podemos medir actualmente

| Métrica | Disponibilidad y límite |
|---|---|
| Teléfonos con mensajes entrantes | Sí, en `chat_messages`; incluye otros usos del canal y atención humana. |
| Actividad diaria y recurrencia | Sí, sobre los mensajes conservados. |
| Sesiones aproximadas | Agrupando por teléfono y pausas de 30 minutos; no reproduce exactamente la sesión real. |
| Reservas por canal | Sí, con limitaciones históricas de `source`. |
| Operaciones sin duplicar ida/vuelta | Mediante `booking_group_code`; validar cobertura de registros antiguos. |
| Paso actual de conversaciones | Solo para sesiones que todavía existen. |
| Consultas y demanda sin cupo | Parcialmente mediante `inquiries` y `waiting_list_entries`. |
| Precio o resumen efectivamente enviado | No hay historial analítico confiable. |
| Abandono histórico por etapa | No; las sesiones se eliminan. |
| Duración exacta hasta reservar | Falta el vínculo entre inicio conversacional y reserva. |

### Contactos registrados

Los parámetros deben expresarse en la hora local usada por `chat_messages.timestamp`.

```sql
SELECT COUNT(DISTINCT phone_number) AS telefonos_con_mensajes
FROM chat_messages
WHERE is_from_operator = false
  AND timestamp >= :desde
  AND timestamp < :hasta;
```

Es una aproximación a usuarios: números históricos con formatos diferentes pueden duplicarse y un teléfono puede representar varias personas. Tampoco permite excluir retrospectivamente todas las pruebas internas con certeza.

### Reservas agrupadas por canal

```sql
WITH operaciones AS (
    SELECT
        source,
        COALESCE(
            'grupo:' || NULLIF(booking_group_code, ''),
            'fila:' || id::text
        ) AS operacion,
        MIN(created_at) AS creada_en,
        BOOL_AND(status = 'CONFIRMED') AS confirmada_actualmente
    FROM reservations
    GROUP BY 1, 2
)
SELECT
    source,
    COUNT(*) AS operaciones_creadas,
    COUNT(*) FILTER (
        WHERE confirmada_actualmente
    ) AS operaciones_confirmadas_actualmente
FROM operaciones
WHERE creada_en >= :desde
  AND creada_en < :hasta
GROUP BY source;
```

El fallback por fila puede sobrecontar ida/vuelta antiguas sin grupo. La [migración V113](src/main/resources/db/migration/V113__link_reservation_booking_groups.sql) reconstruye grupos para códigos terminados en `-IDA` o `-VUELTA`; falta comprobar cobertura y consistencia en producción. La consulta expresa estado actual, no si alguna vez estuvieron confirmadas.

Otras restricciones:

- `whatsapp_webhook_inbox` no contiene identidad para contar contactos únicos.
- `reservation_events` empieza después de crear la reserva.
- Los defaults históricos de `source` no prueban el origen real de reservas antiguas.
- Encontrar `confirm_ok` en mensajes acredita un intento, no un commit exitoso.
- Una reconstrucción por textos debe etiquetarse como estimación y mantenerse separada de la telemetría nueva.

## 3. Información faltante

Se necesita conservar:

- Identidad seudónima estable del contacto e identificador de sesión analítica.
- Inicio, última interacción, vencimiento y cierre.
- Intención de reservar, distinta de consultar precio.
- Hitos alcanzados, timestamps y paso que esperaba respuesta.
- Resultado: conversión, rechazo, inactividad, derivación humana o lista de espera.
- Grupo de reserva asociado a la conversión.
- Ambiente, canal y marca de prueba.
- Motivos acotados de bloqueo: falta de tarifa/cupo, fecha rechazada, entrada inválida o fallo técnico.

La telemetría puede mostrar dónde se detiene la gente y qué obstáculos coincidieron con la salida. No demuestra por sí sola motivos subjetivos, como considerar caro el servicio.

## 4. Diseño mínimo propuesto

### Dos tablas independientes del estado operativo

- `chatbot_analytics_sessions`: una fila por episodio de interacción.
- `chatbot_analytics_events`: hechos registrados dentro de ese episodio.

`ConversationSession` sigue controlando el bot. La sesión analítica conserva historia aunque la operativa se elimine; no duplica nombres, DNI, domicilio ni borradores de reserva.

No se propone Kafka, un dashboard, una plataforma externa ni un motor configurable de funnels.

### Definiciones de métricas

| Concepto | Definición |
|---|---|
| Usuario único | Teléfono normalizado representado por HMAC. |
| Sesión iniciada | Primer mensaje válido y deduplicado de un episodio. |
| Reserva iniciada | Opción «Reservar» o aceptación de reservar después de cotizar; primera fecha por sesión. |
| Conversión del bot | Grupo de reserva persistido por aceptación del usuario. |
| Confirmación comercial | Estado/pago confirmado, medido aparte con reservas y sus eventos. |
| Abandono de reserva | Existía intención de reservar, no hubo conversión ni salida alternativa, y venció la sesión. |
| Consulta expirada | Episodio sin intención de reservar que terminó por inactividad. |
| Rechazo explícito | El usuario eligió no continuar; separado de inactividad. |

El ingreso debe registrar también los mensajes de tipos no soportados para no ocultar contactos que intentaron usar el bot. La clasificación del recorrido distinguirá choferes, pasajeros y casos todavía desconocidos, reutilizando la identificación operativa existente. Los informes deben mostrar la proporción de desconocidos y separar soporte/cancelaciones del recorrido de adquisición.

Una sesión nueva comienza tras vencimiento o cierre de la anterior. Escribir «menú» durante un episodio registra reinicio sin incrementar artificialmente sesiones. La telemetría no debe alterar cómo el bot reinicia o conserva sus datos operativos.

Se propone empezar con el timeout operativo de 30 minutos y almacenar `expires_at`. El vencimiento se revisa mediante tarea analítica y al llegar un mensaje nuevo. La derivación humana y lista de espera se registran como salidas alternativas, no como abandono.

### Eventos

```text
SESSION_STARTED
BOOKING_STARTED
PRICE_REQUESTED
PRICE_SENT
ROUTE_SELECTED
DATE_SELECTED
PASSENGER_DATA_COMPLETED
SUMMARY_SENT
BOOKING_CREATED
BOOKING_DECLINED
SESSION_EXPIRED
HUMAN_HANDOFF
WAITING_LIST_JOINED
FLOW_BLOCKED
INPUT_REJECTED
SEND_FAILED
```

Los eventos describen hechos, no el mero ingreso a un handler:

- `PRICE_REQUESTED`: opción explícita de consulta de precio.
- `PRICE_SENT` y `SUMMARY_SENT`: Meta aceptó el envío efectivo; no implica entrega o lectura.
- `ROUTE_SELECTED`: origen y destino están determinados y validados, según la rama.
- `PASSENGER_DATA_COMPLETED`: los datos requeridos por el recorrido ya están disponibles, incluidos los reutilizados.
- `BOOKING_CREATED`: una operación persistida después del commit, con su grupo y una reserva representativa; nunca un evento por tramo.
- `SESSION_EXPIRED`: cierre por inactividad; el estado distingue abandono con intención de consulta sin intención.
- `FLOW_BLOCKED` e `INPUT_REJECTED`: motivos enumerados como `NO_FARE`, `NO_CAPACITY`, `DATE_CUTOFF` o `INVALID_INPUT`.

Los hits repetidos pueden conservarse como eventos de distintas interacciones. El funnel contará sesiones o usuarios distintos por hito. La clave de deduplicación evita repetir un mismo hecho por reintentos, sin borrar repeticiones legítimas.

### Integración y consistencia

Un puerto pequeño, por ejemplo `ChatbotTelemetryPort`, recibe hechos y contexto inmutable. Los handlers no ejecutan SQL ni calculan analytics. Un adaptador persiste el evento y actualiza la sesión analítica en una transacción propia, atómica e idempotente.

Los hechos ligados a transacciones de negocio se procesan después del commit. Para hechos fuera de transacción debe existir un camino explícito de persistencia inmediata: no depender de un listener transaccional que los ignore.

El proyecto ya utiliza listeners posteriores al commit en [OperatorNotificationService](src/main/java/com/lunaris/ansenuza/application/usecase/OperatorNotificationService.java). Spring permite asociar listeners a fases transaccionales: [documentación oficial](https://docs.spring.io/spring-framework/reference/7.1/data-access/transaction/event.html).

La correlación de envíos debe viajar explícitamente hasta el callback del envío efectivo. No depender de que el contexto de un hilo siga disponible en tareas asíncronas ni de consultar una sesión operativa que pudo eliminarse.

Una falla de analytics no debe revertir la reserva. Se propone persistencia de mejor esfuerzo, tiempos acotados y contador de fallos de telemetría. Existe una ventana de pérdida ante caída del proceso entre commit y persistencia analítica. No se promete entrega garantizada; un mecanismo durable adicional quedaría fuera de esta primera versión.

### Privacidad, canal y pruebas

- `subject_key = HMAC-SHA-256(secreto, teléfono_normalizado)` con versión de clave.
- Secreto fuera de PostgreSQL; no usar SHA simple del teléfono.
- Esto es seudonimización, no anonimización irreversible.
- No copiar mensajes, nombres, documentos, ubicaciones, comprobantes ni payloads a analytics.
- `is_test` lo determina el servidor por ambiente/simulador y una lista explícita de teléfonos internos.
- No deducir pruebas por nombre, importe cero o promoción.
- Conservar `reservations.source` y usar la correlación analítica para identificar recorridos asistidos, incluida la excepción de lista de espera. No corregir retrospectivamente atribuciones sin evidencia.
- Retención inicial propuesta: 90 días de detalle, con borrado programado y acceso restringido. Las referencias comerciales también permiten vinculación y deben tratarse como datos restringidos.
- La rotación de la clave HMAC necesita una estrategia de continuidad o separar los períodos; de lo contrario un contacto puede contarse dos veces.

## 5. Archivos y clases afectados por una implementación futura

| Clases existentes | Cambio propuesto |
|---|---|
| `WhatsAppWebhookController`, `IncomingMessage`, `WhatsAppSimulatorDevController` | Contexto, correlación, ambiente y pruebas; registro posterior a deduplicación. |
| `ConversationOrchestrator` | Actividad, clasificación, paso actual, reinicios y errores de procesamiento. |
| `MainMenuHandler`, `MarketingConfirmationHandler` | Intención, consulta de precio, rechazo y derivación. |
| `AskLocalityHandler`, `AskTownDestinationHandler`, `AskDestinationHandler` | Cotización, ruta y bloqueos. |
| Handlers de fechas y datos, `AskPromotionCodeHandler` | Hitos validados y motivos de rechazo. |
| `ConversationPresenter`, `WhatsAppMessagingAdapter`, `WhatsAppService` | Correlacionar precio/resumen con resultado efectivo de envío. |
| `ConfirmationHandler`, `ConfirmWaitingListBookingHandler` | Conversión posterior al commit, vinculada al resultado persistido. |
| `WaitingListCapacityGuard`, handlers de espera, `TakeOverConversationUseCase` | Salidas alternativas por cupo, lista de espera y atención humana. |

Clases nuevas previstas:

- Puerto de telemetría y contexto inmutable.
- Servicio/adaptador de persistencia.
- Dos entidades JPA y sus repositorios.
- Tarea de vencimiento y retención analítica.
- Configuración de habilitación, ambiente, HMAC y pruebas internas.

Las entidades nuevas usarían UUID con `@GeneratedValue(strategy = GenerationType.UUID)` y fallback defensivo según `AGENTS.md`. Las consultas de lectura usarían `@Transactional(readOnly = true)` donde corresponda.

No se propone transformar `reservation_events` en historial conversacional ni cambiar las reglas de creación, pago o capacidad.

## 6. Migración SQL y consultas propuestas

### Migración candidata

Nombre: `V127__chatbot_analytics.sql`, sujeto a que esa versión siga libre al implementar.

**SQL para revisión; no ejecutado ni validado todavía contra PostgreSQL.** Los UUID se generan desde la aplicación.

```sql
CREATE TABLE chatbot_analytics_sessions (
    id UUID PRIMARY KEY,
    subject_key VARCHAR(64) NOT NULL,
    subject_key_version SMALLINT NOT NULL,
    environment VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL,
    is_test BOOLEAN NOT NULL DEFAULT FALSE,
    audience VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',

    started_at TIMESTAMPTZ NOT NULL,
    last_interaction_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    booking_started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,

    current_step VARCHAR(64),
    last_milestone VARCHAR(64),
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN (
            'ACTIVE', 'COMPLETED', 'ABANDONED', 'EXPIRED',
            'DECLINED', 'HANDED_OFF', 'WAITLISTED'
        )),
    end_reason VARCHAR(64),
    booking_group_code VARCHAR(40),
    reservation_id UUID,

    CHECK (last_interaction_at >= started_at),
    CHECK (expires_at >= last_interaction_at),
    CHECK ((status = 'ACTIVE') = (ended_at IS NULL)),
    CHECK ((status = 'COMPLETED') = (completed_at IS NOT NULL))
);

CREATE UNIQUE INDEX uq_chatbot_active_subject
    ON chatbot_analytics_sessions (
        environment, source, subject_key_version, subject_key
    )
    WHERE status = 'ACTIVE';

CREATE INDEX idx_chatbot_sessions_started
    ON chatbot_analytics_sessions(started_at);

CREATE INDEX idx_chatbot_sessions_expiry
    ON chatbot_analytics_sessions(expires_at)
    WHERE status = 'ACTIVE';

CREATE TABLE chatbot_analytics_events (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL
        REFERENCES chatbot_analytics_sessions(id) ON DELETE CASCADE,
    event_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    step VARCHAR(64),
    reason_code VARCHAR(64),
    dedupe_key VARCHAR(128) NOT NULL,
    UNIQUE (session_id, dedupe_key)
);

CREATE INDEX idx_chatbot_events_session_time
    ON chatbot_analytics_events(session_id, occurred_at);
```

El índice único parcial limita a una sesión activa por identidad, ambiente y canal. Su uso está soportado por PostgreSQL: [documentación oficial](https://www.postgresql.org/docs/17/sql-createindex.html).

`dedupe_key` identifica el hecho lógico y es estable ante reintentos. Debe incorporar tipo de evento y una referencia de interacción o cierre; no incluir texto ni teléfono. El identificador UUID del evento no reemplaza esta clave.

No hay FK hacia `conversation_sessions`, porque sus filas se eliminan. `reservation_id` y `booking_group_code` son referencias de correlación sin cascadas hacia reservas. Los invariantes de conversión y sus referencias se validarán también en el servicio analítico.

### Usuarios, sesiones, operaciones y conversión

Los parámetros de estas consultas nuevas representan instantes con zona horaria. La cohorte se define por el inicio de la sesión, no por la fecha del viaje.

```sql
WITH cohorte AS (
    SELECT *
    FROM chatbot_analytics_sessions
    WHERE started_at >= :desde
      AND started_at < :hasta
      AND environment = 'prod'
      AND source = 'WHATSAPP'
      AND is_test = FALSE
      AND audience <> 'DRIVER'
)
SELECT
    COUNT(DISTINCT subject_key) AS usuarios_unicos,
    COUNT(*) AS sesiones_iniciadas,
    COUNT(*) FILTER (
        WHERE booking_started_at IS NOT NULL
    ) AS sesiones_con_intencion_de_reservar,
    COUNT(DISTINCT booking_group_code) FILTER (
        WHERE completed_at IS NOT NULL
    ) AS operaciones_creadas,
    ROUND(
        100.0 * COUNT(DISTINCT subject_key) FILTER (
            WHERE completed_at IS NOT NULL
        ) / NULLIF(COUNT(DISTINCT subject_key), 0),
        2
    ) AS conversion_usuarios_pct,
    AVG(completed_at - booking_started_at) FILTER (
        WHERE completed_at IS NOT NULL
          AND booking_started_at IS NOT NULL
    ) AS tiempo_promedio_para_reservar
FROM cohorte;
```

La conversión es usuarios que concretaron la reserva / usuarios que iniciaron sesión, sin duplicar ida/vuelta. La confirmación comercial o del pago se consulta aparte. También conviene mostrar conversión entre usuarios que expresaron intención de reservar.

La duración representa tiempo transcurrido desde la intención hasta crear la reserva. Para medir desde el primer contacto, sustituir `booking_started_at` por `started_at`. Agregar mediana y percentil 90 si el promedio resulta poco representativo.

Estas consultas asumen identidad estable durante el período. No resuelven automáticamente cambios de clave HMAC ni conversiones asistidas posteriores a una sesión ya cerrada. Una reactivación puede abrir una nueva sesión; la atribución retrospectiva multisesión queda fuera del mínimo inicial.

### Alcance de hitos del funnel

```sql
SELECT
    e.event_type,
    COUNT(DISTINCT e.session_id) AS sesiones,
    COUNT(DISTINCT s.subject_key) AS usuarios
FROM chatbot_analytics_events e
JOIN chatbot_analytics_sessions s ON s.id = e.session_id
WHERE s.started_at >= :desde
  AND s.started_at < :hasta
  AND s.environment = 'prod'
  AND s.source = 'WHATSAPP'
  AND NOT s.is_test
  AND s.audience <> 'DRIVER'
  AND e.event_type IN (
      'SESSION_STARTED', 'BOOKING_STARTED', 'PRICE_REQUESTED',
      'PRICE_SENT', 'ROUTE_SELECTED', 'DATE_SELECTED',
      'PASSENGER_DATA_COMPLETED', 'SUMMARY_SENT', 'BOOKING_CREATED'
  )
GROUP BY e.event_type;
```

Esta consulta cuenta alcance de hitos; no impone orden temporal. No calcular abandono restando filas consecutivas: las ramas tienen distinto orden y algunos hitos son opcionales.

### Abandono por etapa

```sql
SELECT
    last_milestone,
    current_step AS paso_pendiente,
    end_reason,
    COUNT(*) AS sesiones
FROM chatbot_analytics_sessions
WHERE started_at >= :desde
  AND started_at < :hasta
  AND environment = 'prod'
  AND source = 'WHATSAPP'
  AND NOT is_test
  AND audience <> 'DRIVER'
  AND status = 'ABANDONED'
GROUP BY last_milestone, current_step, end_reason
ORDER BY sesiones DESC;
```

Reportar por separado `DECLINED`, `EXPIRED`, `HANDED_OFF` y `WAITLISTED`. `last_milestone` expresa lo alcanzado; `current_step`, qué estaba esperando el bot.

Las cohortes recientes son provisionales mientras tengan sesiones activas. Para comparaciones estables, usar cohortes maduras y comunicar el instante de corte. Una expiración describe inactividad observada, no una decisión definitiva del usuario.

## 7. Riesgos, efectos secundarios y validación futura

| Riesgo | Tratamiento propuesto |
|---|---|
| Confundir consulta informativa con abandono | Separar intención, consulta expirada y rechazo. |
| Duplicar ida/vuelta o reintentos | Conversión por grupo, eventos idempotentes y conteo distinto de sesiones/usuarios. |
| Cerrar una sesión mientras llega un mensaje | Bloquear/revalidar la fila en PostgreSQL al actualizar actividad y expirar. El orden local por teléfono no alcanza con varias instancias. |
| Marcar un envío antes de que ocurra | Instrumentar la llamada efectiva a Meta y transportar correlación en callbacks. |
| Perder eventos tras commit | Reconocer la ventana de pérdida, medir fallos y no prometer entrega garantizada. |
| Afectar disponibilidad del bot | Persistencia aislada, errores contenidos, tiempos acotados y posibilidad de deshabilitar telemetría. |
| Interpretar defaults como origen histórico | No backfill de origen ni eventos sin evidencia. |
| Crecimiento o exposición de datos | Retención limitada, HMAC, ausencia de payloads y acceso restringido. |
| Contar soporte como intención de compra | Separar tipos de recorrido y mostrar métricas globales y de reserva. |

Pruebas necesarias al implementar:

- Ida/vuelta produce una sola conversión.
- Rollback no produce `BOOKING_CREATED`.
- Reintentos no duplican eventos ni sesiones.
- Timeout concurrente con mensaje nuevo no cierra incorrectamente la sesión activa.
- Derivación humana/lista de espera no se cuenta como abandono.
- Simulador y números internos quedan fuera de métricas de producción.
- Error de telemetría no revierte la reserva.
- Fallo del envío efectivo no produce `PRICE_SENT` o `SUMMARY_SENT`.
- Migración, índices y consultas se validan contra PostgreSQL; la suite actual por sí sola no prueba ese comportamiento.
- Ejecutar `./mvnw test` según las instrucciones del proyecto.

## 8. Hipótesis de fricción detectadas en el código

No constituyen evidencia causal de baja adopción y no fueron modificadas:

1. `AskLocalityHandler` muestra entre 1 y 4 lugares disponibles calculados aleatoriamente, sin consultar el cupo real para ese mensaje.
2. `confirm_cancel` en el resumen lleva a `FOLLOW_UP_RETENTION`, sin handler registrado. Un siguiente mensaje puede provocar recuperación al inicio.
3. Se solicitan DNI/CUIT y código promocional antes del resumen; interesa medir pérdidas e inputs rechazados en esos pasos.
4. Hay restricciones de fecha, horario, tarifa y cupo que pueden interrumpir el recorrido. Sin eventos de motivo, se confunden con desinterés.
5. Los errores de envío pueden dejar el estado avanzado sin que el usuario haya recibido la siguiente instrucción.

## Decisión pendiente

Aprobar o ajustar el diseño antes de implementar. La propuesta se limita a captura de sesiones, eventos y consultas SQL para explicar el recorrido del usuario. La auditoría no autoriza cambios funcionales del bot ni correcciones de las hipótesis de fricción mencionadas.
