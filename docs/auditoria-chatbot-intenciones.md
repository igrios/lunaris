# Auditoría del chatbot y propuesta de migración

Fecha: 2026-11-08. Alcance: análisis del código actual y diseño propuesto; no se modifica el comportamiento productivo. El proyecto declara Java 21 y Spring Boot 3.5.14.

## Diagnóstico

El bot actual es una máquina de estados persistida, no un motor de intenciones. Su entrada principal es `POST /whatsapp/webhook`, en `WhatsAppWebhookController`: verifica la firma de Meta, reclama el identificador en inbox y delega asincrónicamente por teléfono. Las imágenes se derivan a `ProcessPaymentReceiptUseCase`; el texto a `ConversationOrchestrator`. `WhatsAppWebhookParser` no transcribe audio: los tipos no reconocidos terminan como `OTHER`, sin texto.

`ConversationOrchestrator` selecciona un `ConversationStepHandler` usando `ConversationSession.currentStep`. Un saludo, `menu` o `reinicio` puede devolver al usuario a `START`. `StartHandler` siempre muestra opciones numeradas y pasa a `MAIN_MENU`; `MainMenuHandler` interpreta números del 1 al 6. Por eso enviar una solicitud completa en el primer mensaje no ejecuta directamente la intención.

La rigidez continúa en los handlers de localidad, destino, modalidad, horario y confirmación. `ConversationPresenter.sendAllLocalitiesList` exige responder solamente un número; `sendReservationSummaryWithButtons` utiliza `confirm_ok` y `confirm_cancel`. `WebSocketLiveChatAdapter` incluso transforma selecciones numéricas a localidades para el historial: la presentación está acoplada al estado.

Los handlers contienen reglas que deben sobrevivir: `ConfirmationHandler` crea pasajeros y reservas, aplica promociones/saldo y contempla aeropuerto, regreso abierto y pagos. No debe eliminarse esta lógica junto con los menús. Hay además caminos diferentes de creación (`application.usecase.CreateReservationUseCase`, el puerto homónimo en `reservation.application.port.in` y `ReservationService.saveReservationFlow`): no basta con conectar la IA al primer método de creación disponible.

## Arquitectura recomendada

```text
WhatsApp → n8n (recepción, transcripción e interpretación)
         → API conversacional Spring (contexto, validación, acciones)
         → respuesta JSON → n8n → WhatsApp
```

n8n/OpenAI propone intención y parámetros; Spring determina qué operaciones son válidas y obtiene precios, horarios y cupos reales. El modelo no decide importes, identidad, permisos, pago confirmado ni disponibilidad. El mensaje original es información no confiable, nunca instrucciones ejecutables.

Elegir un solo dueño del ingreso y de la respuesta automática por conversación. Para pasajeros migrados, desactivar la ejecución del bot antiguo y sus envíos. Si Spring sigue recibiendo Meta, debe reenviar el evento al flujo externo en lugar de ejecutar también `ConversationOrchestrator`. Preservar los comprobantes, ubicaciones, notificaciones y comandos de chofer mediante rutas explícitas. El teléfono del cuerpo solo se acepta desde la integración autenticada; no constituye por sí mismo una credencial del pasajero.

El endpoint será stateless respecto de HTTP (sin `HttpSession`), con memoria conversacional en PostgreSQL. No hay contradicción entre ambos requisitos.

## Cambios por clase

| Clase o grupo actual | Cambio propuesto |
| --- | --- |
| `WhatsAppWebhookController` | Conservar firma, handshake y adaptación Meta cuando siga siendo punto de entrada. Extraer el enrutamiento a aplicación; evitar doble procesamiento con n8n. |
| `ConversationOrchestrator` | Extraer identificación de chofer, políticas de pausa/operador y comandos operativos. Sustituir el despacho por `currentStep`, reinicio por saludo y `resetToStart` por un caso de uso de intenciones. |
| `StartHandler`, `MainMenuHandler` | Retirar del nuevo flujo: eliminar construcción del menú, comparaciones numéricas y respuestas de opción inválida. Extraer consulta de reservas, saludo personalizado y derivación humana a casos de uso. |
| `steps/Ask*`, `SelectScheduleHandler`, `ReturnWindowSelectionHandler` | Extraer normalización/validaciones útiles; retirar transiciones secuenciales, índices numéricos y obligación de responder en un orden fijo. |
| `ConfirmationHandler`, `InterurbanConfirmationHandler`, handlers de lista de espera y cancelación | Extraer comandos transaccionales independientes del canal antes de retirar handlers. Conservar las políticas de cada modalidad. |
| `ConversationPresenter` | Separar consultas/cálculos de presentación. Reemplazar listas numéricas y botones obligatorios por resultados y texto natural retornados al caller. |
| `ConversationSession` | Conservar teléfono, pausas y operador asignado. Migrar campos de reserva a un borrador versionado; retirar `currentStep` solo cuando ningún consumidor lo use. |
| `ConversationState`, `ConversationStep`, `ConversationStepHandler` | Retirar después de migrar referencias y pruebas. Los enums actuales no gobiernan el despacho, que usa strings. |
| `ChatMessage`, `ChatMessageRepository` | Aprovechar historial; agregar autor explícito, identificador externo y consulta reciente paginada. La consulta actual trae todo el historial. |
| `WebSocketLiveChatAdapter` | Retirar `mapLocalitySelection` y traducciones de botones antiguos tras la migración. Emitir eventos del mensaje real sin volver a persistirlo. |
| `ConversationSessionCleanupScheduler` | Expirar el borrador/contexto; no borrar accidentalmente metadatos de atención humana. Hoy elimina sesiones inactivas no pausadas. |
| `AssistedChatService`, `TakeOverConversationUseCase`, `ManualReservationChatHandoffService` | Mantener compatibles con el nuevo borrador y respetar pausa manual antes de cualquier acción automática. |
| `WhatsAppService`, `WhatsAppMessagingAdapter`, `MessagingPort` | Conservar para canales operativos y notificaciones. El nuevo caso de uso retorna su respuesta sin enviarla también por estos adaptadores. |
| `WhatsAppWebhookInboxService`, `WhatsAppMessageDispatcher` | Extender fiabilidad e idempotencia; el dispatcher solo ordena dentro de una JVM. |
| `SecurityConfig` | Cadena específica para `/api/chatbot/**`, `STATELESS` y autenticación de servicio con permiso acotado. No habilitar `permitAll` ni cambiar globalmente las sesiones del panel. |
| Telemetría y simulador | Cambiar métricas de pasos por intención, aclaración, resultado, derivación y error. Adaptar simulador y pruebas. |

## Contrato propuesto

`POST /api/chatbot/mensaje`, `Content-Type: application/json`, autenticación de servicio mediante Bearer validado y alcance exclusivo del chatbot. `messageId` es obligatorio y estable ante reintentos.

```json
{
  "messageId": "wamid.ejemplo",
  "telefono": "5493512345678",
  "mensaje": "Mañana quiero ir a Córdoba, somos dos",
  "receivedAt": "2026-11-08T15:20:00Z",
  "interpretacion": {
    "intencion": "CONSULTAR_DISPONIBILIDAD",
    "parametros": {
      "destino": "Córdoba",
      "fechaIda": "2026-11-09",
      "cantidadPasajeros": 2
    }
  }
}
```

DTOs Java `record` con Bean Validation. Alias de entrada `from` → `telefono` y `texto` → `mensaje` mediante `@JsonAlias`; rechazar alias contradictorios. No usar `Map<String,Object>` como comando de negocio. Usar tipos para fechas, localidades, pasajeros y modalidades; limitar texto y cantidades. Fechas relativas se resuelven con `receivedAt` y zona `America/Argentina/Cordoba`, evitando que un reintento cambie el significado de “mañana”.

La interpretación puede omitirse: en ese caso `IntentDetectionPort` delega al flujo externo. En producción con n8n como entrada, enviarla evita una segunda inferencia y ciclos HTTP. Una intención desconocida o parámetros ambiguos producen una pregunta, nunca una reserva por defecto. No implementar coincidencias de palabras como sustituto del motor.

Respuesta `200` de ejemplo:

```json
{
  "messageId": "wamid.ejemplo",
  "conversationId": "171983de-45b1-4c26-8f46-9a9892630758",
  "contextVersion": 4,
  "estado": "REQUIERE_DATOS",
  "intencion": "CONSULTAR_DISPONIBILIDAD",
  "respuesta": "¿Desde qué localidad salen mañana hacia Córdoba?",
  "faltantes": ["origen"],
  "datos": null,
  "reenviar": true
}
```

n8n reenvía `respuesta` cuando `reenviar=true`. Estados: `RESUELTO`, `REQUIERE_DATOS`, `REQUIERE_CONFIRMACION`, `DERIVADO`. Si hay atención humana, registrar el mensaje, evitar acciones automáticas y retornar `reenviar=false`. Resultados de horarios/cupos incluyen datos estructurados reales para que n8n pueda reformular sin inventarlos.

Intenciones iniciales: `CONSULTAR_VIAJES`, `CONSULTAR_DISPONIBILIDAD`, `COTIZAR`, `REGISTRAR_PASAJERO`, `PREPARAR_RESERVA`, `CONFIRMAR_RESERVA`, `CONSULTAR_RESERVAS`, `CANCELAR_RESERVA`, `HABLAR_CON_OPERADOR`, `DESCONOCIDA`. Registrar pasajero no implica crear reserva. Cada acción valida propiedad del recurso y datos requeridos.

Confirmar requiere un borrador completo y un resumen previamente presentado, con `draftId` y versión. Un “sí” solo confirma ese borrador vigente. Revalidar precio y cupo al escribir; una consulta de disponibilidad no retiene asientos. Si cambian datos o importes, solicitar nueva confirmación. La confirmación del usuario no confirma el pago.

Errores mediante excepciones de dominio y `@ControllerAdvice`: `400` contrato inválido; `401/403` autenticación/permisos; `409` versión obsoleta o reutilización de `messageId` con contenido diferente; `503` dependencia temporalmente indisponible. Dato conversacional faltante es `200 REQUIERE_DATOS`. Respuestas de error con código estable, mensaje y correlación, sin trazas internas.

## Memoria y Flyway

Para varios audios y varias instancias, recomiendo memoria durable. Ya existen `conversation_sessions` (teléfono único) y `chat_messages`; no crear otra tabla de historial equivalente.

Migraciones nuevas, usando las siguientes versiones libres al implementar, sin editar migraciones aplicadas:

1. Extender `chat_messages` con `sender_type` (`USER/BOT/OPERATOR/SYSTEM`), `external_message_id`, estado de entrega y marca temporal inequívoca. El booleano actual `fromOperator` no distingue todos los autores. Hacer backfill solo donde el origen pueda acreditarse; mantener desconocidos los ambiguos. Índice por teléfono, timestamp e id para recuperar los últimos 10–20 mensajes en orden estable.
2. Crear `conversation_context` con UUID, teléfono único normalizado, intención activa, `draft` JSONB validado, `version`, `expires_at`, `updated_at`. El borrador se actualiza parcialmente: el segundo audio agrega/corrige datos sin borrar los anteriores. Las pausas y asignación humana siguen siendo responsabilidad de la sesión operativa existente durante la transición.
3. Crear inbox de integración o evolucionar el existente con canal/identificador único, hash del request, estado (`RECEIVED/PROCESSING/COMPLETED/FAILED`), respuesta JSONB y recuperación de trabajos interrumpidos. Reclamar un ID no equivale a completar su procesamiento.

Entidades nuevas con UUID: `@GeneratedValue(strategy = GenerationType.UUID)` y fallback defensivo antes de guardar, conforme a AGENTS.md. No cambiar los IDs `Long` existentes sin una migración específica. `@Version` o actualizaciones condicionadas por versión para evitar pérdida de datos; unicidad en base para carreras de creación. Un `Optional` por sí solo no garantiza resultados únicos.

La memoria expira por inactividad configurable, separada de la retención del historial. No reutilizar indefinidamente datos de viajes anteriores. Enviar al modelo únicamente la ventana reciente y el borrador necesario, evitando documentos/datos fiscales cuando no hacen falta para interpretar.

Concurrencia: ordenar por teléfono mediante coordinación durable entre instancias. Registrar eventos y aplicar transiciones en transacciones cortas; no sostener locks durante llamadas a IA. Si la interpretación se calculó sobre una versión anterior, reintentar con contexto vigente antes de ejecutar. Revalidar pausa manual inmediatamente antes de una mutación. Dos audios concurrentes deben combinarse en el orden de recepción acordado, con timestamp y secuencia persistidos.

Idempotencia: persistir mutación y resultado de comando de forma atómica. Un reintento retorna la misma respuesta sin repetir reserva, descuento o alta. La entrega WhatsApp necesita su propio registro de envío en n8n para evitar respuestas duplicadas; el `200` del backend no acredita entrega al pasajero. El webhook actual reclama antes de procesar, devuelve `200` incluso al capturar excepciones y el dispatcher solo registra errores: existe riesgo de perder el reintento útil tras un fallo.

## Nuevas piezas de aplicación

- `ChatbotMessageController`: validar DTO y delegar; sin repositorios ni envíos directos.
- `ProcessChatbotMessageUseCase`: idempotencia, pausa humana, contexto, interpretación y despacho.
- `IntentDetectionPort`: interpretación externa cuando sea necesaria.
- `ConversationContextService`: mezcla de parámetros, versiones, expiración y datos faltantes.
- Casos de uso de consulta con `@Transactional(readOnly = true)` y comandos transaccionales de pasajero/reserva/cancelación.
- `ConversationResponseComposer`: devolver texto natural y datos, sin efectos de envío.

Antes de elegir el caso de uso de reserva definitivo, extraer y unificar políticas hoy repartidas entre handlers y servicios: ida/vuelta, interurbano, aeropuerto/especial, lista de espera, horarios del mismo día, promoción, saldo, facturación y pago. Reutilizar `PricingAndScheduleService`, políticas de capacidad y `ReservationService` mediante contratos de aplicación, sin aceptar precios calculados por el modelo.

## Plan de migración y criterios de aceptación

1. Caracterizar las reglas existentes y extraerlas de los handlers sin cambiar resultados.
2. Incorporar contrato, autenticación, memoria versionada y comandos idempotentes.
3. Integrar n8n/transcripción e interpretación estructurada; habilitar por teléfono o cohorte. Mantener un solo procesador activo por conversación.
4. Migrar consultas, borradores, confirmación, cancelación y derivación; luego retirar estados y menús y sus referencias en panel/simulador/telemetría.
5. Probar: solicitud completa al primer mensaje; varios audios y correcciones; “hola” sin pérdida de borrador; ambigüedad y fechas relativas; intención inválida; pausa humana; comprobante; chofer; regreso abierto/interurbano/especial; cupo agotado al confirmar; promociones/saldo; reintento duplicado; recuperación tras fallo y concurrencia entre instancias.

Las pruebas de integración nuevas de persistencia deben ejercitar PostgreSQL real para JSONB, índices, unicidad y locks. Ejecutar `./mvnw test` como verificación obligatoria del repositorio. Esta auditoría no equivale a haber implementado ni validado esos escenarios nuevos.
