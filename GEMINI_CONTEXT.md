# Auditoría técnica y contexto para Gemini — Lunaris Ansenuza

## Objetivo del documento

Este documento resume el estado técnico real de Lunaris Ansenuza y proporciona
un contexto de trabajo para asistentes de inteligencia artificial. Debe usarse
como complemento del código fuente y no como sustituto de su inspección.

## Diagnóstico ejecutivo

Lunaris Ansenuza es un sistema funcional con una base de pruebas considerable,
pero atraviesa una transición arquitectónica incompleta. Los principales riesgos
no están actualmente en la compilación, sino en la seguridad, la duplicación de
modelos y la concentración de responsabilidades en clases demasiado grandes.

Estado comprobado al 18 de agosto de 2026:

- Java 21 y Spring Boot 3.5.14.
- 286 archivos Java de producción.
- 98 archivos de prueba.
- 98 recursos en `src/main/resources`.
- 321 pruebas ejecutadas sin fallos ni errores.
- Cobertura JaCoCo aproximada:
  - Líneas: 51,8 %.
  - Ramas: 39,0 %.
  - Métodos: 30,9 %.
  - Instrucciones: 50,3 %.
- Rama analizada: `dev`.

## Descripción funcional

Lunaris Ansenuza administra transporte de pasajeros en combis y vans. Sus
recorridos conectan localidades del este de Córdoba y sur de Santa Fe con
Córdoba Capital y el Aeropuerto de Córdoba.

El sistema cubre los siguientes dominios:

1. Reservas de ida, ida y vuelta y vuelta abierta.
2. Pasajeros, acompañantes, saldos y perfiles.
3. Choferes, vehículos, postulaciones y hojas de ruta.
4. Agenda operativa, asignación de recorridos y abordaje.
5. Tarifas, promociones y reglas de capacidad.
6. Comprobantes, pagos, facturación y auditoría financiera.
7. Lista de espera y viajes especiales.
8. Bot de WhatsApp y atención humana mediante WebSocket.
9. Paneles Thymeleaf para operadores, administración y facturación.

## Stack tecnológico

- Java 21.
- Spring Boot 3.5.14.
- Maven Wrapper (`./mvnw`).
- Spring MVC, Security, Validation, Data JPA y WebSocket.
- Spring Retry y Spring Integration Mail.
- Hibernate 6 y PostgreSQL.
- Flyway para migraciones.
- Thymeleaf y Bootstrap para vistas web.
- Cloudinary para almacenamiento.
- WhatsApp Cloud API.
- Springdoc OpenAPI.
- Lombok.
- JUnit 5, Mockito, H2 y JaCoCo.

## Arquitectura real

La mayor parte de la aplicación sigue esta estructura:

```text
com.lunaris.ansenuza
├── domain
│   ├── model
│   ├── model.service
│   ├── repository
│   └── port
├── application
│   ├── conversation
│   ├── payment
│   ├── port
│   ├── scheduler
│   └── usecase
├── infrastructure
│   ├── adapter
│   ├── chat
│   ├── config
│   ├── persistence
│   ├── storage
│   ├── web
│   └── whatsapp
├── reservation
│   ├── domain
│   ├── application
│   └── infrastructure
└── shared
```

Aunque la documentación describe una arquitectura hexagonal, el dominio
principal no es puro: muchas clases de `domain.model` son directamente entidades
JPA y contienen anotaciones de Hibernate, relaciones y callbacks de persistencia.

### Módulo `reservation` paralelo

Existe un módulo experimental o de migración bajo
`com.lunaris.ansenuza.reservation` que contiene:

- Un agregado `Reservation` puro.
- Puertos de entrada y salida.
- Un servicio de aplicación.
- Una entidad JPA separada.
- Mapper y adaptador de persistencia.

Este módulo duplica el agregado principal
`com.lunaris.ansenuza.domain.model.Reservation`. Actualmente conviven dos fuentes
de verdad para reservas, estados, invariantes y persistencia. No debe ampliarse ni
eliminarse sin analizar sus consumidores y definir una estrategia explícita de
migración.

## Dominios principales

### Reservas y pasajeros

`Reservation` es el agregado operativo central. Incluye:

- Pasajero y chofer asignado.
- Fecha de viaje y regreso.
- Origen, destino y domicilio.
- Cantidad de pasajeros y acompañantes.
- Importe base, extra y descuento.
- Promoción aplicada.
- Comprobante y verificación del pago.
- Código y grupo de reserva.
- Secuencia y dirección de ruta.
- Requerimiento de factura.
- Timestamps operativos y de auditoría.

La vuelta abierta utiliza semántica histórica y la fecha centinela
`2099-12-31` en algunos flujos.

### Estados

El modelo mezcla dos dimensiones:

- `status`, almacenado como `String`, para el pago y ciclo de la reserva.
- `TravelStatus`, enum para el estado operativo del viaje.

Existen valores parcialmente redundantes como `ONBOARD`, `BOARDED`,
`ONBOARDED`, `COMPLETED` y `REALIZED`. También hay conversores tolerantes para
compatibilidad histórica. Esto permite combinaciones inválidas y dificulta
establecer una máquina de estados canónica.

### Flota y hojas de ruta

Las entidades principales son `Driver`, `Vehicle` y `DriverApplication`.
La asignación operativa utiliza `driver_id`, `route_sequence`, fecha, horario y
dirección. El flujo de abordaje actualiza el viaje y puede notificar al siguiente
pasajero.

Las operaciones de capacidad y reemplazo de rutas deben mantenerse atómicas y
protegidas contra concurrencia.

### Conversaciones y WhatsApp

El flujo principal está compuesto por:

- `WhatsAppWebhookController`: entrada HTTP.
- `WhatsAppWebhookParser`: transformación del webhook.
- `WhatsAppMessageDispatcher`: procesamiento asincrónico y secuencial por teléfono.
- `ConversationOrchestrator`: carga de sesión y enrutamiento.
- `ConversationStepHandler`: estrategia para cada paso.
- `MessagingPort`: salida de mensajería.
- `LiveChatPort`: integración con atención humana.
- `ReceiptStoragePort`: almacenamiento de comprobantes.

Los handlers se registran como componentes y declaran el paso que procesan. La
sesión conserva el estado parcial de la conversación.

### Tarifas y promociones

`Fare` asocia una localidad por nombre con un importe. La lógica principal se
concentra en `PricingAndScheduleService`, que calcula tarifas y horarios.

`Promotion` y `PromotionUsage` administran descuentos, vencimiento y consumo. La
aplicación debe conservar idempotencia y evitar dobles usos bajo concurrencia.

### Pagos y facturación

El sistema contempla:

- Comprobantes manuales.
- Detección de transferencias mediante correo de Mercado Pago.
- Ledger de transacciones procesadas.
- Outbox de auditoría de pagos.
- Confirmación manual o automática configurable.
- Generación y almacenamiento de facturas PDF.
- Envío o reenvío por WhatsApp.

El total facturable debe considerar el importe de toda la reserva, incluyendo
`amount`, `extraAmount` y descuentos aplicables.

### Lista de espera y viajes especiales

La lista de espera usa OTP, controles de capacidad, conversión a reserva y
reenganche de pasajeros. Los viajes especiales pueden relacionarse con banners o
novedades públicas.

## Roles y seguridad

Roles internos:

| Rol | Responsabilidad |
| --- | --- |
| `ADMIN` | Configuración y control integral. |
| `OPERADOR` | Agenda, reservas, pasajeros, rutas y chats. |
| `CHOFER` | Hoja de ruta, ubicación y abordaje. |
| `FACTURACION` | Facturas y documentación fiscal. |
| `PASSENGER` | Autoridad temporal para el portal del pasajero. |

Hay dos cadenas `SecurityFilterChain`:

1. API, webhooks, WhatsApp y Actuator.
2. Vistas web con autenticación mediante formulario.

## Hallazgos críticos

### 1. Token de WhatsApp expuesto

`application.yaml` contiene un token literal como valor predeterminado. Debe
considerarse comprometido aunque sea temporal.

Acciones requeridas:

- Revocar o rotar el token.
- Eliminar el valor predeterminado.
- Exigir `WHATSAPP_ACCESS_TOKEN` como variable externa.
- Revisar el historial de Git.
- No copiar el token a documentación, logs o respuestas de IA.

### 2. Cuenta operativa con contraseña fija

`SecurityConfig` crea automáticamente una cuenta operativa con usuario y
contraseña definidos en el código.

Debe reemplazarse por una de estas opciones:

- Variables de entorno obligatorias.
- Inicializador exclusivo de un perfil local.
- Proceso explícito de aprovisionamiento.

### 3. Integridad de Flyway desactivada

`spring.flyway.validate-on-migrate=false` permite que modificaciones de
migraciones históricas no sean detectadas. Debe restaurarse la validación después
de revisar y reparar los checksums de forma controlada.

### 4. Agregado `Reservation` duplicado

La coexistencia del agregado JPA principal y el agregado puro del módulo
`reservation` introduce dos modelos de estados e invariantes. Debe definirse cuál
será la fuente de verdad y ejecutar una migración gradual con pruebas de
compatibilidad.

## Hallazgos de prioridad alta

### Clases sobredimensionadas

Principales concentraciones observadas:

- `WhatsAppService`: aproximadamente 936 líneas.
- `ReservationService`: aproximadamente 693 líneas.
- `AgendaViewController`: aproximadamente 627 líneas.
- `ConversationOrchestrator`: aproximadamente 537 líneas.
- `BotMonitorController`: aproximadamente 443 líneas.
- `ReservationViewController`: aproximadamente 430 líneas.
- `ReservationRepository`: aproximadamente 417 líneas.

Los controladores contienen parte de la orquestación y reglas que deberían vivir
en casos de uso. `WhatsAppService` también concentra construcción de mensajes,
plantillas, llamadas HTTP, retry y fallbacks.

### Método productivo sin implementar

`domain.model.Reservation#setScheduleBlock()` lanza siempre
`UnsupportedOperationException`. Debe implementarse correctamente o eliminarse
después de revisar todos sus consumidores.

### Protección HTTP amplia

La cadena API:

- Deshabilita CSRF completamente.
- Usa sesiones `IF_REQUIRED`.
- Habilita HTTP Basic.
- Declara públicos patrones amplios de reservas y Actuator.

La creación pública de reservas puede ser legítima, pero las reglas deberían
declarar métodos y rutas exactos. Las operaciones públicas necesitan rate
limiting, validación estricta y controles específicos para archivos.

### Endpoints de reserva duplicados

La creación pública aparece en dos controladores:

- JSON en `PublicApiController`.
- Multipart en `ReservationApiController`.

Spring puede diferenciarlos por `Content-Type`, pero el contrato está fragmentado
y ambos caminos pueden divergir funcionalmente.

## Hallazgos de prioridad media

### Prueba principal insuficiente

`LunarisAnsenuzaApplicationTests` solo comprueba que la clase principal no sea
nula. No levanta el `ApplicationContext` completo.

Debe añadirse una prueba real de arranque y, para las rutas críticas de
persistencia, pruebas con PostgreSQL mediante Testcontainers.

### Cobertura desigual

Aunque pasan 321 pruebas, la cobertura de ramas es 39 % y la de métodos 30,9 %.
Se debe priorizar:

- Seguridad y autorizaciones negativas.
- Transiciones inválidas de estados.
- Concurrencia de capacidad y rutas.
- Idempotencia de webhooks, pagos y promociones.
- Rollback de operaciones compuestas.
- Contratos REST completos.

### Observabilidad ruidosa

Se encontraron `System.out.println`, `printStackTrace`, trazas extensas para
errores esperados y SQL habilitado por defecto. Algunos logs contienen teléfonos
y códigos de reserva.

Se recomienda:

- Logging estructurado.
- Sanitización de PII.
- Correlation IDs.
- Niveles de log por perfil.
- `show-sql=false` por defecto.
- Métricas de webhook, colas, pagos y tiempos de respuesta.

### Inyección inconsistente

Persisten usos de `@Autowired`. La convención debe ser constructor injection, con
campos `final` y sin inyección de campo.

### Gobierno de migraciones

Existen saltos en la numeración y varias migraciones que vuelven a modificar
reservas o tipos de viaje. No implica necesariamente un error, pero exige:

- No editar migraciones ya aplicadas.
- Restaurar validación de checksums.
- Probar contra PostgreSQL real.
- Documentar compatibilidad de datos históricos.

## Aspectos positivos

- Suite estable de 321 pruebas.
- Uso de `BigDecimal` para dinero.
- UUID defensivo en varias entidades.
- Casos de uso transaccionales en operaciones relevantes.
- Bot dividido en handlers por paso.
- Puertos para mensajería, almacenamiento, pagos y chat.
- Procesamiento secuencial por teléfono y uso de virtual threads.
- Ledger idempotente y outbox para auditoría de pagos.
- Roles de dominio diferenciados.
- Uso de Flyway y `ddl-auto=validate` como estrategia base.
- Pruebas relevantes para tarifas, promociones, rutas y conversación.

## Orden recomendado de remediación

### P0 — Seguridad e integridad

1. Rotar y eliminar secretos del repositorio.
2. Eliminar la cuenta bootstrap con contraseña fija.
3. Auditar `permitAll`, uploads, verificación de webhooks y rate limiting.
4. Restaurar validación Flyway.

### P1 — Consistencia del dominio

1. Definir un único agregado `Reservation`.
2. Canonizar las máquinas de estados de pago y viaje.
3. Unificar los contratos REST de reservas.
4. Añadir pruebas reales del contexto y PostgreSQL/Testcontainers.

### P2 — Mantenibilidad

1. Dividir `WhatsAppService`, `ReservationService` y controladores grandes.
2. Subir cobertura de ramas críticas.
3. Sanitizar logging y desactivar SQL por defecto.
4. Eliminar field injection, impresiones directas y métodos incompletos.

---

# Contexto operativo para Gemini

## Rol

Actúa como Senior Staff Java/Spring Boot Engineer y arquitecto de dominio, con
experiencia avanzada en Java 21, Spring Boot 3, DDD, arquitectura limpia,
persistencia JPA, seguridad y sistemas concurrentes orientados a eventos.

## Reglas obligatorias

- Usa Java 21 y constructor injection.
- Mantén los controladores delgados.
- Coloca reglas en casos de uso o servicios de dominio.
- Usa `@Transactional(readOnly = true)` en operaciones exclusivamente de lectura.
- Los UUID JPA deben usar `GenerationType.UUID` y fallback defensivo antes de
  `save()` cuando corresponda.
- No modifiques migraciones Flyway ya aplicadas; crea una nueva migración.
- No uses `double` o `float` para dinero.
- Mantén `America/Argentina/Cordoba` como zona horaria operativa.
- No registres tokens, contraseñas, teléfonos completos, CUIL ni contenido de
  comprobantes.
- No cambies estados históricos sin migración y compatibilidad explícitas.
- Preserva idempotencia en pagos, webhooks y promociones.
- Protege concurrencia en capacidad, asignación de rutas y mensajes.
- Ejecuta `./mvnw test` después de cada modificación.
- Añade pruebas de regresión para todo comportamiento corregido.
- Responde y documenta en español; usa nombres de código en inglés.

## Forma de trabajo

Antes de modificar código:

1. Inspecciona consumidores, repositorios, migraciones y pruebas involucradas.
2. Distingue comportamiento actual, comportamiento deseado y compatibilidad.
3. Explica riesgos transaccionales, de concurrencia y de seguridad.
4. Propón el cambio mínimo seguro.
5. Implementa pruebas antes o junto con la corrección.
6. Verifica la suite completa.
7. No asumas que `CLAUDE.md` o este documento reflejan cada detalle del código
   actual; el código y las migraciones son la evidencia principal.

## Restricciones arquitectónicas

- La dirección deseada de dependencias es infraestructura → aplicación → dominio.
- Los handlers conversacionales deben depender de puertos, no directamente de
  `WhatsAppService` ni de `SimpMessagingTemplate`.
- No expandas el módulo `com.lunaris.ansenuza.reservation` sin resolver primero la
  duplicación con el agregado principal.
- No añadas nuevos estados como strings arbitrarios.
- No coloques reglas de precios, capacidad, cancelación o facturación en
  controladores.
- No confíes exclusivamente en H2 para validar SQL y migraciones PostgreSQL.

## Criterios de aceptación para cambios

Todo cambio relevante debería cumplir:

1. Compilación correcta con Java 21.
2. Pruebas unitarias del comportamiento principal y casos límite.
3. Pruebas de autorización cuando afecte rutas o roles.
4. Pruebas transaccionales o de concurrencia cuando corresponda.
5. Migración Flyway para cambios de esquema.
6. Ausencia de secretos y PII en código y logs.
7. `./mvnw test` exitoso.
8. Explicación concisa de riesgos residuales.

## Comandos habituales

```bash
./mvnw test
./mvnw clean package
./mvnw spring-boot:run
./mvnw verify
```

## Advertencia final

Las prioridades inmediatas son la rotación de secretos, la eliminación de
credenciales bootstrap, la recuperación de la validación Flyway y la definición
de una única fuente de verdad para `Reservation`. Agregar funcionalidad antes de
resolver estos puntos aumentará el costo de mantenimiento y el riesgo operativo.
