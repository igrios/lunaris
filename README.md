# Lunaris Ansenuza

Sistema de gestión para transporte de pasajeros, reservas, flota y operación diaria. Centraliza la venta y administración de pasajes, la atención automatizada por WhatsApp, las hojas de ruta, los pagos y la facturación.

## Funcionalidades principales

- Reservas de ida, ida y vuelta y viajes con regreso abierto.
- Asignación de horarios, asientos, choferes y hojas de ruta.
- Gestión de pasajeros, localidades, tarifas y promociones.
- Bot conversacional de WhatsApp para consultas, reservas y notificaciones.
- Lista de espera y viajes especiales.
- Recepción de comprobantes y conciliación de pagos por transferencia.
- Facturación y almacenamiento de comprobantes y documentos.
- Panel operativo con chat en tiempo real mediante WebSocket.
- Control de acceso por roles: `ADMIN`, `OPERADOR`, `CHOFER` y `FACTURACION`.

## Tecnologías

- Java 21
- Spring Boot 3.5
- Spring MVC, Data JPA, Security, Validation y WebSocket
- Thymeleaf
- PostgreSQL y Flyway
- H2 para desarrollo local
- Cloudinary
- OpenAPI / Swagger UI
- Maven Wrapper
- JUnit 5, Mockito y JaCoCo

## Requisitos

- JDK 21
- Docker, opcional
- PostgreSQL, sólo si no se utiliza el perfil local `dev`

No es necesario instalar Maven: el repositorio incluye Maven Wrapper.

## Ejecución local

El perfil `dev` utiliza una base H2 en memoria, deshabilita Flyway y configura valores simulados para la integración con WhatsApp.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación quedará disponible en:

- Panel web: <http://localhost:8080>
- Inicio de sesión: <http://localhost:8080/login>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificación OpenAPI: <http://localhost:8080/api-docs>
- Estado de la aplicación: <http://localhost:8080/actuator/health>
- Consola H2, sólo con perfil `dev`: <http://localhost:8080/h2-console>

Las credenciales administrativas iniciales del entorno local se controlan con `ADMIN_INITIAL_USERNAME` y `ADMIN_INITIAL_PASSWORD`. Cambie siempre sus valores predeterminados fuera de un entorno de desarrollo aislado.

## Configuración con PostgreSQL

La configuración predeterminada espera PostgreSQL y valida el esquema administrado por Flyway:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lunaris_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD='una-clave-segura'
export WHATSAPP_PHONE_NUMBER_ID='...'
export WHATSAPP_ACCESS_TOKEN='...'

./mvnw spring-boot:run
```

Flyway ejecuta las migraciones disponibles en `src/main/resources/db/migration` al iniciar la aplicación.

### Variables de entorno relevantes

| Variable | Descripción | Valor predeterminado |
| --- | --- | --- |
| `PORT` | Puerto HTTP | `8080` |
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/lunaris_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de base de datos | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de base de datos | `postgres` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Validación o gestión del esquema | `validate` |
| `WHATSAPP_PHONE_NUMBER_ID` | Identificador del número en Meta | obligatorio en producción |
| `WHATSAPP_ACCESS_TOKEN` | Token de acceso a WhatsApp | obligatorio en producción |
| `ADMIN_INITIAL_USERNAME` | Usuario administrador inicial | `admin` en `dev` |
| `ADMIN_INITIAL_PASSWORD` | Contraseña administrador inicial | definida en `dev` |
| `CLOUDINARY_CLOUD_NAME` | Cuenta de Cloudinary | vacío |
| `CLOUDINARY_API_KEY` | Clave de Cloudinary | vacío |
| `CLOUDINARY_API_SECRET` | Secreto de Cloudinary | vacío |
| `STORAGE_LOCAL_DIR` | Directorio de comprobantes | `/tmp/comprobantes/` |
| `STORAGE_INVOICES_DIR` | Directorio de facturas | `/tmp/facturas/` |
| `STORAGE_DRIVER_APPLICATIONS_DIR` | Documentos de postulantes | `/tmp/driver-applications/` |
| `LUNARIS_SUPPORT_PHONE` | Teléfono de soporte | vacío |
| `LUNARIS_TRIP_CAPACITY` | Capacidad predeterminada por viaje | `12` |
| `LUNARIS_EXTERNAL_DRIVER_COST` | Costo de chofer externo | `0` |
| `PROMOTIONS_AUTHORIZED_OPERATOR_PHONE` | Teléfono autorizado para promociones | vacío |
| `PAYMENT_IMAP_USERNAME` | Usuario del buzón de pagos | vacío |
| `PAYMENT_IMAP_PASSWORD` | Contraseña del buzón de pagos | vacío |

La conciliación automática por correo está desactivada por defecto. Sus propiedades se encuentran en `src/main/resources/application.properties`.

## Pruebas y compilación

Ejecutar la suite completa:

```bash
./mvnw test
```

Generar el artefacto y el reporte de cobertura JaCoCo:

```bash
./mvnw verify
```

El JAR se genera dentro de `target/` y el reporte de cobertura en `target/site/jacoco/index.html`.

## Docker

Construir y ejecutar la imagen:

```bash
docker build -t lunaris-ansenuza .
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/lunaris_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD='una-clave-segura' \
  -e WHATSAPP_PHONE_NUMBER_ID='...' \
  -e WHATSAPP_ACCESS_TOKEN='...' \
  lunaris-ansenuza
```

La imagen expone el puerto `8080`. Para despliegues reales deben suministrarse también las variables de almacenamiento, seguridad e integraciones que correspondan.

## Arquitectura

El código sigue una organización inspirada en Clean Architecture y arquitectura hexagonal:

```text
src/main/java/com/lunaris/ansenuza/
├── domain/          # Entidades, reglas de negocio, servicios y puertos
├── application/     # Casos de uso, orquestación y DTO
├── infrastructure/  # Web, persistencia, seguridad e integraciones
├── reservation/     # Módulo hexagonal de reservas
└── shared/          # Utilidades compartidas
```

Los controladores HTTP y las vistas se encuentran en infraestructura; las reglas del negocio permanecen en los casos de uso y servicios de dominio. La persistencia utiliza JPA y adaptadores, mientras que las integraciones externas se expresan mediante puertos.

## Roles

| Rol | Alcance principal |
| --- | --- |
| `ADMIN` | Configuración completa, usuarios, flota, tarifas y administración |
| `OPERADOR` | Agenda diaria, reservas, pasajeros, chat y operación de viajes |
| `CHOFER` | Consulta y ejecución de hojas de ruta asignadas |
| `FACTURACION` | Panel de facturación y tareas contables |

## Documentación adicional

- [Manual de usuario](USER_MANUAL.md)
- [Diagramas de procesos y roles](SYSTEM_DIAGRAMS.md)
- [Flujo integral de reservas](RESERVATION_FLOW.md)

## Seguridad

- No almacene tokens, contraseñas ni claves de servicios externos en el repositorio.
- Configure secretos mediante variables de entorno o el gestor de secretos de la plataforma.
- Reemplace inmediatamente las credenciales iniciales en cualquier entorno compartido.
- Revise los endpoints públicos y la política CORS antes de desplegar en un dominio nuevo.

## Contribución

Antes de enviar cambios:

1. Respete la separación entre dominio, aplicación e infraestructura.
2. Agregue o actualice las pruebas correspondientes.
3. No modifique migraciones Flyway ya aplicadas; cree una migración nueva.
4. Ejecute `./mvnw test` y compruebe que la suite finaliza correctamente.

## Licencia

Este repositorio no declara actualmente una licencia de distribución. Consulte con los responsables del proyecto antes de copiar, modificar o redistribuir el software.
