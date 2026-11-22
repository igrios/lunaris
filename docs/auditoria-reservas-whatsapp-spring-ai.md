# Auditoría de reservas, WhatsApp y Spring AI

## Resumen ejecutivo

Prioridad P0: eliminar la pérdida silenciosa de webhooks. El receptor confirma al proveedor aunque falle el ingreso y su registro de deduplicación puede bloquear la recuperación de mensajes que jamás se procesaron. Prioridad P1: reutilizar los casos de uso existentes y reforzar la concurrencia de cambios, cancelaciones y asignaciones. Prioridad P2: incorporar transcripción y NLU como adaptadores del flujo durable, con confirmación explícita para cada operación sensible.

Alcance: revisión estática del árbol local, sus configuraciones y migraciones, más la suite Maven. Los riesgos de rendimiento son hipótesis para medir. La configuración efectiva de Render, los secretos, el volumen de tráfico y el esquema de la base desplegada requieren verificación en el entorno.

## 1. Estructura y modelo de reservas

| Hallazgo | Impacto y cambio recomendado |
|---|---|
| El monolito combina capas globales `domain/application/infrastructure`, el módulo `reservation` con puertos propios e `interurban` con persistencia JDBC. `domain.model.Reservation` y `reservation…ReservationEntity` mapean `reservations`. | Dos mapeos sobre las mismas filas pueden divergir en columnas y reglas. Inventariar todos los escritores, incluidos controladores, bot, pagos y schedulers. Consolidar escrituras detrás de casos de uso canónicos, con pruebas de paridad entre entradas. |
| `Reservation` concentra datos del tramo, del grupo, pagos, promoción, facturación y notificaciones. Guarda localidades y horario como texto, relaciona pasajero y chofer con carga eager y vincula tramos mediante `bookingGroupCode`. | Un cambio de ida puede afectar vuelta, cupo y total facturable. Preservar datos históricos y migrar gradualmente a identidades de reserva, tramo y salida operacional con claves de catálogo. Medir consultas de agenda y cupo con `EXPLAIN (ANALYZE, BUFFERS)` y contadores SQL antes de ajustar índices o loaders. Usar proyecciones y fetch explícito en consultas de lectura. |
| `ReservationService` concentra creación, cupo, cancelación, saldo y regresos. Usa transacciones y bloqueos pesimistas. V114 introduce locks de capacidad, V119 endurece auditoría financiera y V139 agrega revisión y registro de operaciones para modificación integral. | Reutilizar estas garantías. Unificar orden de locks por grupo, salida y pasajero entre creación, modificación, expiración y pago. Probar intercalaciones en PostgreSQL antes de cambiar la granularidad. Limitar la transacción al comando y mover I/O externo a una etapa posterior durable. |
| `UpdateReservationUseCase` ya valida `expectedRevision`, protege el grupo, registra operaciones y recalcula capacidad para cambios admitidos. `getState` hereda una transacción de clase. | Usar su contrato para cambios compatibles. Si un cambio de horario de ida excede ese contrato, añadir un comando explícito con revisión, tarifa y revalidación de la salida vieja y nueva. `getState` inicializa o sincroniza la revisión y requiere escritura. Exponer una consulta `readOnly` separada si se necesita listar propuestas sin adquirir esos locks. Mantener UUID generados donde corresponda y expresar unicidad real con restricciones, además de los tipos de retorno de los repositorios. |
| `CancelReservationHandler` verifica titularidad por teléfono y deriva a operador si ya hay chofer o ruta enviada. `ReservationCancellationService` toma la primera coincidencia para algunas decisiones de retorno. La cancelación de dominio puede acreditar saldo o afectar solo el regreso. | Pedir identificación del tramo o grupo cuando haya varias coincidencias. Aplicar identidad, alcance, despacho, revisión y política de crédito bajo transacción. Un botón de confirmación debe referir a la operación y revisión específicas. |
| `ReservationDriverAssignmentService.assign` bloquea la reserva, lee los asientos ya asignados al manifiesto y usa un límite fijo de cuatro. Dos asignaciones de filas distintas pueden leer el mismo aforo disponible. | Proteger la salida o manifiesto común mediante un lock compartido. Modelar asignación de chofer y vehículo por salida, validar disponibilidad temporal y capacidad real, conservar secuencia y trazabilidad. Derivar conflictos a operaciones. |
| Flyway está habilitado con `out-of-order: true`. Hibernate usa `validate` en base, `none` en prod y `update` en dev. El perfil prod configura Hikari con máximo por defecto de tres conexiones. | Reproducir migraciones desde cero y desde un snapshot anonimizado de producción, validar allí el mapeo Hibernate y revisar si se necesita ejecución fuera de orden. Mantener los locks cortos y medir latencia de adquisición de conexión y de lock. |

El estado de la reserva combina el eje comercial (`PENDING_PAYMENT`, `PAYMENT_RECEIVED`, `CONFIRMED`, `CANCELLED` y rutas de compatibilidad), el operativo (`TravelStatus`, incluido `OPEN_RETURN`, `ROUTE_SENT`, abordaje y finalización) y el financiero (`PaymentStatus`, verificación, saldo y facturación). Introducir transiciones explícitas por comando y una matriz de invariantes antes de simplificar estados persistidos. Ejemplos: una modificación tras despacho requiere derivación, una vuelta abierta puede carecer de fecha, una cancelación parcial conserva los tramos realizados y una revisión de itinerario preserva el pago reconocido y el cálculo de facturación grupal. Mantener las reglas del total en `BookingInvoiceAmount` y sus casos de uso, incluidos `amount`, `extraAmount` e indicadores de alcance.

Para un modelo objetivo incremental, usar `Booking` como raíz comercial y de revisión, `BookingLeg` como tramo con estado operacional y `TripDeparture` como salida con cupo y asignación. Antes de imponer el modelo, reconstruir grupos históricos usando las reglas vigentes, cotejar cupo y facturación y mantener una transición de lectura compatible.

## 2. Canal de Meta Cloud API

| Prioridad | Evidencia | Recomendación |
|---|---|---|
| P0 | `WhatsAppWebhookParser.parse` usa `firstMap` en `entry`, `changes` y `messages`. | Recorrer el lote completo. Distinguir mensajes, estados y tipos desconocidos. Mantener métricas de entrada y motivos de descarte. |
| P0 | `WhatsAppWebhookInboxService.claim` hace `REQUIRES_NEW` y `ON CONFLICT DO NOTHING` por ID. V122 guarda ID y hora. Luego `WhatsAppMessageDispatcher` encola en memoria y registra excepciones. | Confirmar recepción solo tras persistir el trabajo recuperable. Reinicio, rechazo del ejecutor y excepción del handler deben permitir reentrega interna. |
| P0 | `WhatsAppWebhookController.receive` devuelve `200` incluso si falla el parseo, PostgreSQL o el despacho. | `200` tras commit durable o para eventos válidos sin trabajo, `401` para firma inválida, `400` para cuerpo inválido y `503` ante persistencia temporalmente indisponible. Aislar elementos inválidos de un lote válido. |
| P1 | El GET valida token con comparación de tiempo constante y recibe `hub.mode` sin comprobar su valor. El POST calcula HMAC SHA-256 sobre bytes crudos antes del JSON y rechaza un secreto ausente. | Conservar la firma sobre bytes, exigir `hub.mode=subscribe`, validar configuración en el arranque del perfil receptor y normalizar identidad. Incluir `phone_number_id` y timestamp, comprobar números empresariales admitidos y limitar tamaño del cuerpo. |
| P1 | Soporta texto, imagen, ubicación y botones/listas. Envía imágenes a comprobantes. Audio llega como `OTHER` y queda sin tratamiento. El orden por teléfono existe solo en memoria y comienza después del encolado asíncrono. | Enrutar por tipo con DTOs explícitos y orden durable por cuenta y remitente. Mantener comprobantes, comandos de chofer, pausa humana, botones y ubicación como flujos deterministas. |
| P1 | `WhatsAppService` usa varias versiones de Graph en rutas de envío y parte del envío ocurre en callbacks poscommit. Los estados de entrega se descartan por el parser. | Configurar una versión Graph validada, consolidar el transporte Meta y registrar el ID externo y los estados para reconciliar entregas. Reemplazar callbacks críticos por outbox durable. |

Los logs del parser de interactivos y del handler de cancelación incluyen identificadores o entrada del pasajero. Aplicar redacción en logs y métricas sin etiquetas de alta cardinalidad. Para la especificación de firma y lotes, ver [Webhooks de Meta](https://developers.facebook.com/docs/graph-api/webhooks/getting-started/).

### Inbox, worker y outbox

1. El controlador verifica, decodifica y delega a `AcceptWhatsAppEventsUseCase`. Un puerto de persistencia guarda el lote normalizado con clave única `(phone_number_id, wamid)`, remitente, tiempo del proveedor, secuencia de recepción y estado inicial. Migrar V122 de forma aditiva y conservar los marcadores históricos como deduplicación legada sin intentar reconstruir payloads ausentes.
2. El worker reclama filas listas con `FOR UPDATE SKIP LOCKED`, en una transacción corta. Una exclusión por conversación permite una sola tarea activa por cuenta y remitente. Guardar intentos, vencimiento del lease y token de reclamación. Usar una secuencia de ingreso estable y tratar timestamps tardíos explícitamente. Una respuesta tardía con revisión vencida fuerza una nueva consulta o aclaración.
3. Descargar, transcribir e interpretar fuera de transacciones. Guardar el resultado intermedio con su versión de modelo y esquema para evitar repetir etapas completadas. Aplicar timeout y concurrencia acotada para cada proveedor. Para 429, red y 5xx, usar backoff con jitter y respetar `Retry-After` cuando exista. Errores permanentes pasan a un estado terminal visible al operador. Reprocesar desde la última etapa completada.
4. Aplicar el cambio con un identificador de operación derivado del evento y propósito. Dentro del mismo commit escribir la mutación, el resultado de la operación y la respuesta en outbox, y completar el trabajo validando el lease. La entrega es al menos una vez. Un timeout saliente tras aceptación de Meta deja un resultado incierto. Guardar el intento, correlacionar estados cuando sea posible y aplicar una política explícita de reenvío. Una restricción única interna protege los efectos de negocio y el registro de respuestas.
5. Conservar el timestamp real de la primera recepción para la ventana de WhatsApp, aun si el worker tarda. Al enviar, reutilizar la política de ventana conversacional y las plantillas permitidas. Vincular `PassengerMessageReceived` y sus efectos al procesamiento durable.

## 3. Arquitectura propuesta con Spring AI

```text
Meta → webhook → inbox PostgreSQL → worker → router
                                            ├─ flujo determinista / operador
                                            └─ audio → transcripción → NLU
                                                            ↑ texto libre
                                                      política conversacional
                                                            ↓
                                    comandos de reserva + outbox → Meta
```

| Componente | Contrato y responsabilidad |
|---|---|
| `WhatsAppWebhookParser` y `AcceptWhatsAppEventsUseCase` | Lista de `InboundEvent(messageId, businessPhoneId, sender, occurredAt, receivedAt, payload)`. Payload sellado Java 21 con variantes `Text`, `Audio`, `Image`, `Interactive`, `Location`, `DeliveryStatus` y `Unsupported`. |
| `InboundMessageWorker` y `ConversationRouter` | Orquestan etapas y enrutan comprobantes a `ProcessPaymentReceiptUseCase`. Extraer del `ConversationOrchestrator` los accesos directos a `WhatsAppService` hacia `MessagingPort` y conservar su precedencia de chofer y pausa manual. |
| `InboundMediaPort` y `MetaMediaAdapter` | `fetch(MediaRef)` devuelve contenido acotado, nombre y MIME validado. Extraer el transporte común de comprobantes y audio. El almacenamiento de comprobantes conserva su responsabilidad. |
| `TranscriptionPort` y `OpenAiTranscriptionAdapter` | `transcribe(AudioContent, locale)` devuelve texto y versión de modelo. Descarta audio temporal al terminar y clasifica errores recuperables. |
| `ReservationIntentPort` y `SpringAiReservationIntentAdapter` | `interpret(UserUtterance, ConversationContext)` devuelve `ReservationIntent`. Adaptador configurable junto al bot determinista. `FreeTextReservationExtractor` ya existe para sugerencias del borrador asistido y puede aportar una ruta de degradación tras evaluar su cobertura. |
| `BookingConversationService` | Contexto por conversación con revisión, borrador y propuesta vigente. Resuelve localidades y horarios, pregunta por faltantes, consulta capacidad, escala a operador y aplica comandos confirmados. Adaptar `ConversationSession` o extender el borrador asistido sin duplicar estado autoritativo. |
| `BookingCommand` y `BookingOffer` | El comando lleva `operationId`, actor derivado del canal, `bookingId`, tramo, `expectedRevision` y `offerId`. La oferta lleva ID de salida, hora, cupo consultado, importe calculado, vigencia y revisión. Los crea la aplicación con datos del catálogo y del dominio. |

### Audio de WhatsApp

Detectar `type=audio` y capturar `audio.id`, MIME declarado y marca de voz si está presente. Resolver `GET /{media-id}?phone_number_id=…` con el token y descargar de inmediato la URL devuelta mediante otro GET autenticado. Meta indica cinco minutos de vigencia. En un reintento, volver a resolver el ID. Limitar destinos y redirecciones para proteger el bearer, validar tamaño, formato y hash cuando esté disponible. Medir tamaño durante la descarga y evitar acumular binarios concurrentes en heap. El sistema de archivos local sirve como espacio temporal. [Obtener URL de media](https://www.postman.com/meta/whatsapp-business-platform/request/ptjyi84/retrieve-media-url) · [Descargar media](https://www.postman.com/meta/whatsapp-business-platform/request/zsq66eh/download-media)

En el adaptador, pasar un `Resource` con nombre `.ogg` válido a `AudioTranscriptionPrompt` y llamar a `OpenAiAudioTranscriptionModel` con modelo configurable, idioma `es` y respuesta JSON. OpenAI documenta OGG y hasta 25 MB. Probar OGG/Opus real de WhatsApp por multipart con el starter seleccionado. Transcodificar solo formatos que lo requieran. Si el audio falla definitivamente o queda vacío, pedir texto o derivar a operador.

**Whisper tiene fecha de retiro.** Configurar `whisper-1` para el piloto requerido y validar `gpt-transcribe` antes del despliegue sostenido. OpenAI anuncia el cierre de `whisper-1` para el 26 de febrero de 2027. Probar el modelo sucesor a través del adaptador, con su formato de respuesta y opciones admitidas. [Guía de transcripción de OpenAI](https://developers.openai.com/api/docs/guides/speech-to-text)

### NLU y negociación

```java
record ReservationIntent(
    IntentType type,
    String bookingReference,
    String originText,
    String destinationText,
    String departureDateText,
    String departureTimeText,
    Integer passengerCount,
    List<String> preferences,
    List<BookingField> explicitCorrections,
    List<BookingField> ambiguities
) {}
```

Contemplar `NEW_BOOKING`, `RESCHEDULE`, `CANCEL`, `QUERY`, `CONFIRM`, `HUMAN_HANDOFF` y `UNKNOWN`. Interpretar texto libre y transcripción con `ChatClient` mediante salida estructurada (`.call().entity(ReservationIntent.class)`). Activar esquema JSON nativo si la versión y el modelo elegidos lo soportan. Validar el resultado con reglas Java y catálogo. Resolver fechas relativas con fecha de recepción y zona `America/Argentina/Cordoba`. Mantener el texto fuente de cada dato ambiguo y aplicar solo correcciones expresas, para que «mejor a las cinco, seguimos siendo tres» cambie el horario sin perder el origen ni sobrescribir silenciosamente cantidad.

La aplicación identifica al pasajero, comprueba titularidad y aplica políticas. Puede ofrecer al modelo herramientas de consulta de catálogo y disponibilidad con argumentos validados. El flujo determinista cotiza, selecciona alternativas y emite una propuesta de vigencia limitada. Si se confirma, vuelve a comprobar capacidad, precio, revisión y estado bajo lock, y ejecuta el caso de uso existente. Si ya no hay lugar o la ruta se despachó, ofrece nueva consulta o derivación. Las instrucciones de mensajes y transcripciones son datos de entrada. No exponer herramientas de escritura, credenciales, DNI ni comprobantes al modelo. Minimizar texto y datos personales enviados, acordar retención y redactar logs y eventos. [Structured Output en Spring AI](https://docs.spring.io/spring-ai/reference/api/structured-output.html) · [Controles de datos de OpenAI](https://developers.openai.com/api/docs/guides/your-data)

### Maven, configuración y Render

El `pom.xml` declara Java 21 y Boot 3.5.14. Para el piloto, la pareja comprobable por notas de release es Spring AI **1.1.8** con Boot **3.5.15**. Antes de fijar la versión definitiva, revisar parches disponibles para la rama 3.5 y verificar el árbol de dependencias y las pruebas. Importar `org.springframework.ai:spring-ai-bom` con `type=pom` y `scope=import` bajo `dependencyManagement`. Añadir `org.springframework.ai:spring-ai-starter-model-openai` sin versión en `dependencies`. Mantener la clave en `OPENAI_API_KEY` vinculada mediante `spring.ai.openai.api-key`, y parametrizar `spring.ai.openai.chat.options.model` y `spring.ai.openai.audio.transcription.options.model`. Habilitar los beans mediante flags de audio y NLU; parametrizar límites y deadlines y evitar la multiplicación de reintentos entre cliente y worker. [Spring AI 1.1.8](https://spring.io/blog/2026/06/12/spring-ai-1-1-8-1-0-9-avaialble-now/) · [BOM y starters](https://docs.spring.io/spring-ai/reference/getting-started.html)

Fragmentos propuestos para incorporar en las secciones existentes de `pom.xml`.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.1.8</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>
</dependencies>
```

Empezar con PostgreSQL y el mismo artefacto, con perfiles para webhook y worker. Al inicio pueden convivir en una instancia siempre activa si el presupuesto de recursos lo permite. En Render un Background Worker permite aislar el consumo, mientras el web service conserva `/whatsapp/webhook`. Habilitar solo las tareas programadas y listeners correspondientes a cada perfil y ejecutar migraciones mediante un único paso de release o propietario. Ajustar el total de conexiones por todos los procesos, limitar audio concurrente y medir RSS contra el presupuesto de 512 MiB documentado en el repo. Si el web service usa el plan Free, puede entrar en reposo y su arranque en frío afecta el ACK. Probar shutdown con SIGTERM y recuperación de leases. [Background Workers](https://render.com/docs/background-workers) · [Render Free](https://render.com/docs/free)

## Plan de entrega y criterios de aceptación

1. **Ingesta recuperable.** Desplegar inbox aditiva, parser de lotes y worker con el bot actual. Probar lote con varias entradas, duplicados concurrentes, remitentes múltiples, firma incorrecta, caída de PostgreSQL, reinicio tras commit y expiración de lease. Un mensaje aceptado debe quedar consultable hasta su resultado terminal.
2. **Comandos seguros.** Probar modificación simultánea con pago y despacho, cancelación parcial reintentada, expiración concurrente y dos asignaciones a la última plaza con PostgreSQL real. Verificar saldo, importe facturable, revisión y una sola mutación por operación.
3. **Audio y NLU.** Activar audio en un piloto y comparar NLU en sombra con el flujo vigente. Probar OGG/Opus, URL vencida, 429, límite de tamaño, transcripción vacía, topónimos locales, ruido, correcciones y horarios ambiguos. Activar acciones tras validar que las solicitudes ambiguas se aclaran y los cambios requieren confirmación.
4. **Operación.** Medir p95 del ACK y de cada etapa, edad del trabajo más antiguo, reintentos, fallos terminales, latencia de locks, memoria, costo por conversación y tasa de derivación. Poder desactivar IA conservando ingesta y flujo determinista.

## Verificación

`./mvnw test` terminó con 889 pruebas contabilizadas, 0 fallos, 0 errores y 19 omitidas. Entre las omitidas hay pruebas de concurrencia y migración que requieren `RESERVATION_UPDATE_TEST_JDBC_URL` o `INTERURBAN_TEST_JDBC_URL`. La integración propuesta con Meta y OpenAI y el comportamiento en Render quedan pendientes de validación durante la implementación.
