# CLAUDE.md

Guía para Claude Code (y otras IAs) al trabajar en este repositorio.

## Qué es el proyecto

**Lunaris Ansenuza**: backend operativo, administrativo y de tracking para una
empresa de transporte de pasajeros en Vans/combis. Recorridos desde el este de
Córdoba / sur de Santa Fe (Morteros, San Guillermo, Brinkmann, Miramar, etc.)
hacia **Córdoba Capital** y **Aeropuerto CBA**.

Incluye: panel web de operadores, bot de WhatsApp con flujo conversacional, y
chat en vivo operador↔cliente por WebSockets.

## Stack

- **Java 21**, **Spring Boot 3.5.14** (Maven, con wrapper `./mvnw`)
- **Spring Data JPA / Hibernate** + **PostgreSQL**
- **Flyway** para migraciones de esquema (`src/main/resources/db/migration`)
- **Thymeleaf** + **Bootstrap 5** para vistas web
- **Spring WebSocket** para chat en vivo
- **Lombok** (`@Getter`, `@Setter`, `@Slf4j`, `@Builder`, etc.)
- **springdoc-openapi** (Swagger UI en `/swagger-ui.html`, API docs en `/api-docs`)

## Comandos

```bash
./mvnw spring-boot:run        # levantar la app (puerto 8080 por defecto)
./mvnw clean package          # compilar y empaquetar el jar
./mvnw test                   # correr tests
./mvnw flyway:migrate         # aplicar migraciones manualmente (también corre al arrancar)
```

Requiere PostgreSQL. Por defecto conecta a `jdbc:postgresql://localhost:5432/lunaris_db`
(usuario/pass `postgres`/`postgres`). En producción (Render) todo se sobreescribe
con variables de entorno. `ddl-auto` está en `validate`: el esquema lo maneja Flyway,
**no** Hibernate — toda modificación de tablas va por una nueva migración `V##__*.sql`.

## Arquitectura (hexagonal / clean)

```
com.lunaris.ansenuza
├── domain
│   ├── model            # modelos de dominio puros (Reservation, Locality, Fare...)
│   ├── model.service    # servicios de dominio (PricingAndScheduleService, ReservationService)
│   └── repository       # interfaces de repositorio (puertos)
├── application
│   └── usecase          # casos de uso (CreateReservationUseCase, GetDailyOperationSummaryUseCase...)
└── infrastructure
    ├── persistence
    │   ├── entity       # entidades JPA (separadas del modelo de dominio)
    │   └── repository   # adaptadores JPA que implementan los puertos de domain.repository
    ├── web
    │   ├── controller   # controllers MVC (vistas) y REST
    │   ├── dto          # DTOs por feature: agenda/, dashboard/, reservation/
    │   └── config       # WebMvcConfig
    ├── whatsapp         # WhatsAppService (integración con la API de WhatsApp Cloud)
    ├── storage          # LocalReceiptStorageService (comprobantes de pago)
    └── config           # WebSocketConfig, OpenApiConfig
```

**Convención clave:** el modelo de dominio (`domain.model`) está separado de las
entidades JPA (`infrastructure.persistence.entity`). Al agregar features, respetar
esa separación: el dominio no conoce JPA; los adaptadores en `infrastructure`
hacen el mapeo.

## Dominio — modelos clave

- **Reservation**: entidad principal. Tramos de viaje (Ida / Vuelta / **Vuelta Abierta**),
  estados (`PENDING_PAYMENT`, `CONFIRMED`, `CANCELLED`), datos del pasajero,
  cantidad de asientos, acompañantes, marcas de verificación de pago, código de reserva.
- **Locality**: localidades físicas, kilómetros reales a Córdoba y minutos acumulados
  desde el origen del recorrido (datos de logística para cálculo de horarios).
- **Fare**: cuadro tarifario comercial vivo desde PostgreSQL (`id` UUID,
  `localityName`, `amount` en `BigDecimal`).
- **Passenger**, **Driver**, **Vehicle**: pasajeros y flota.
- **ConversationSession / ConversationState / ConversationStep**: máquina de estados
  del bot de WhatsApp (menú de 5 opciones, carga guiada de reserva).
- **ChatMessage**: mensajes del chat en vivo (persistidos, vía WebSocket).
- **BusinessParameter**: parámetros de negocio configurables desde BD.

> Nota: existen `Fare` **y** `Fares` como clases separadas — posible duplicación /
> deuda técnica a revisar antes de tocar lógica tarifaria.

## Vistas web (Thymeleaf)

- `/dashboard` — métricas diarias y accesos rápidos (incl. nueva reserva)
- `/reservas-panel` (`reservations-grid.html`) — grilla interactiva con buscador
  global; oculta cancelados automáticamente
- `/agenda` — calendario de operaciones por fecha; incluye pasajes con vuelta abierta
- `/reservations/new` (`reservation-form.html`) — carga manual; destinos limitados
  **exclusivamente** a "Córdoba" y "Aeropuerto CBA"
- `/fares` — cuadro tarifario comercial vivo desde BD
- `admin/bot-monitor.html`, `admin/chat-room.html`, `admin/hoja-ruta.html` — panel de operador

## Bot de WhatsApp — flujo conversacional (patrón Strategy)

El webhook es un adaptador de entrada fino; la lógica vive en `application`:

- `infrastructure.web.controller.WhatsAppWebhookController` — solo HTTP: handshake, delega.
- `infrastructure.whatsapp.WhatsAppWebhookParser` — mapea el JSON de Meta → `IncomingMessage`.
- `infrastructure.whatsapp.WhatsAppMessagingAdapter` — implementa `MessagingPort` (salida).
- `infrastructure.chat.WebSocketLiveChatAdapter` — implementa `LiveChatPort` (salida).
- `application.port.*` — puertos de salida (`MessagingPort`, `LiveChatPort`, `ReceiptStoragePort`) y `Button`.
- `application.conversation.ConversationOrchestrator` — sesión, chat en vivo, bypass de bot pausado,
  saludos, y ruteo al handler del `currentStep`.
- `application.conversation.ConversationStepHandler` — interfaz; **un handler por paso** en
  `application.conversation.steps.*`. Para agregar un paso: crear una clase `@Component` que
  implemente la interfaz y devuelva su `step()`; Spring la registra automáticamente.
- `application.conversation.ConversationPresenter` / `PassengerAddressResolver` — colaboradores
  compartidos entre handlers.

**Regla:** los handlers/casos de uso dependen de los **puertos** (`MessagingPort`, etc.), nunca
de `WhatsAppService` ni de `SimpMessagingTemplate` directamente.

## Convenciones

- Idioma del dominio/UI: **español** (rótulos, mensajes al usuario). Nombres de clases
  y código en inglés.
- Todo cambio de esquema → nueva migración Flyway (`V<n>__descripcion.sql`), nunca
  editar migraciones ya aplicadas.
- Usar Lombok para boilerplate, en línea con el código existente.
- Respetar la dirección de dependencias hexagonal: `infrastructure` → `application` → `domain`.

## ⚠️ Seguridad

`src/main/resources/application.yaml` tiene credenciales como **valores por defecto**
literales. El token de WhatsApp actual es **temporal de Meta** (de prueba, expira),
así que no es crítico. Pero al pasar a producción, el token permanente / de System User
**no** debe hardcodearse: dejar solo el placeholder (`${WHATSAPP_ACCESS_TOKEN}` sin
default) y proveerlo por variable de entorno. Mismo criterio para credenciales reales de BD.
