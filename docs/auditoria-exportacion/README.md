# Exportación para auditoría — Lunaris Ansenuza

## Contexto tecnológico

Java 21; Spring Boot 3.5.14; Spring Data JPA / Hibernate; PostgreSQL; Flyway; Spring Security; Bean Validation; Thymeleaf; WebSocket. Integraciones presentes: WhatsApp, Mercado Pago, correo y Cloudinary. Versiones y dependencias obtenidas del `pom.xml` local.

## Entregables

| Orden | Archivo | Archivos fuente | Líneas |
| --- | --- | ---: | ---: |
| 1 | [CAPA DE PERSISTENCIA](01-persistencia.md) | 160 | 5295 |
| 2 | [CAPA DE NEGOCIO / SERVICIOS](02-negocio-servicios.md) | 158 | 8675 |
| 3 | [CAPA WEB Y CONTROLADORES](03-web-controladores.md) | 74 | 5393 |
| 4 | [INTEGRACIONES EXTERNAS / BOT](04-integraciones-bot.md) | 78 | 6995 |

## Alcance y criterios

Se incluyen los 384 archivos Java de producción y todos los archivos de `src/main/resources/db/migration`. Las rutas originales encabezan cada bloque de código. `manifest.json` registra clasificación, cantidad de líneas y SHA-256 del archivo original.

1. Persistencia: entidades, repositorios JPA y JDBC, adaptadores, mapeadores, conversores y migraciones.
2. Negocio: casos de uso, servicios, modelos sin persistencia, reglas, puertos, excepciones, tareas programadas, utilidades y arranque.
3. Web: controladores, DTOs, validaciones, mapeadores, serialización, manejo de errores y seguridad/autenticación funcional.
4. Integraciones: clientes y adaptadores externos, almacenamiento, pagos externos, correo, WhatsApp, webhooks y motor conversacional.

La clasificación combina ruta, nombre y anotaciones. Los controladores de webhooks se ubican en integraciones; las entidades y repositorios del bot se ubican en persistencia. DTOs compartidos están en web. Las clases mixtas del paquete `service/interurban` se distribuyen entre las cuatro capas. No representa una auditoría de defectos ni una garantía de separación arquitectónica.

Se omiten configuración de entorno (`application*.properties/yml`, `.env`), archivos del IDE, compilados, logs, dependencias, pruebas, plantillas HTML, recursos estáticos y SQL fuera de Flyway. Las clases Java de configuración funcional sí se incluyen para conservar el contexto de seguridad, integración y negocio.

Se realizó una búsqueda heurística de claves privadas, claves AWS/GitHub y asignaciones literales sensibles en los fuentes seleccionados, sin coincidencias. El código se conserva íntegro; esta búsqueda no garantiza la ausencia de datos sensibles.

## Verificación

`./mvnw test`: BUILD SUCCESS. Pruebas: 668; fallos: 0; errores: 0; omitidas: 13.
