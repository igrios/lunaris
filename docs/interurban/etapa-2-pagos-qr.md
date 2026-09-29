# Etapa 2: pagos y QR interurbanos

Implementación aislada en `com.lunaris.ansenuza.service.interurban`. Se conserva la integración IMAP y todos los endpoints, servicios y reglas de Córdoba. `pom.xml` incorpora `com.google.zxing:javase:3.5.3`; V130 añade únicamente `interurban.qr_artifacts` y un índice del inbox. V129 no se modifica.

## Activación

Ambos flags son obligatorios; pagos permanece apagado aunque se habilite sólo capacidad:

```properties
lunaris.interurban.enabled=true
lunaris.interurban.payments.enabled=true
lunaris.interurban.payments.access-token=${INTERURBAN_MP_ACCESS_TOKEN}
lunaris.interurban.payments.webhook-secret=${INTERURBAN_MP_WEBHOOK_SECRET}
lunaris.interurban.payments.collector-id=${INTERURBAN_MP_COLLECTOR_ID}
lunaris.interurban.payments.live-mode=false
lunaris.interurban.payments.qr-encryption-key=${INTERURBAN_QR_ENCRYPTION_KEY}
lunaris.interurban.payments.qr-key-id=key-2026-01
```

El ejemplo usa sandbox (`live-mode=false`); el default es true. La clave QR debe ser 32 bytes aleatorios codificados en Base64, distinta del secreto HMAC y gestionada fuera del repositorio. Conservar cada clave mientras existan artefactos cifrados con su key-id; esta etapa no incluye rotación ni entrega de pases. Credenciales faltantes o inválidas impiden arrancar el módulo explícitamente activado. No se agregaron valores activos ni secretos a application.properties.

## Webhook y procesamiento

`POST /webhook/interurban/mercadopago?data.id=<payment_id>` recibe los headers `x-request-id`, `x-signature` y cuerpo:

```json
{"type":"payment","action":"payment.updated","data":{"id":"12345"}}
```

También admite `payment.created`. La firma HMAC-SHA256 usa `id:<data.id de query>;request-id:<header>;ts:<ts original>;` y comparación constante. No se utiliza el event-id no firmado del body como autoridad. Se exige coincidencia entre query y body; los IDs de la Payments API se restringen a dígitos. Timestamps de 10/13 dígitos permiten ejemplos en segundos/milisegundos, firmando siempre su representación original.

Ventana configurable `lunaris.interurban.payments.signature-max-age=PT24H`, tolerancia futura 60 segundos. El intervalo permite reintentos del proveedor; las notificaciones más antiguas necesitan conciliación operativa. El worker no revalida la antigüedad de eventos que ya ingresaron autenticados. Algoritmo contrastado con [documentación oficial de firma](https://www.mercadopago.com.ar/developers/en/docs/wallet-connect/notifications).

La respuesta 200 vacía significa **inbox confirmado en BD**, no pago aprobado. Firma inválida devuelve 401, cuerpo incoherente 400, fallo de almacenamiento 503. No devuelve QR ni token. `SecurityConfig` ya permite `/webhook/**`; no fue modificado. El advice nuevo se limita al controlador interurbano.

`MercadoPagoWebhookService.processPending()` procesa hasta 50 pendientes, cada 10 segundos por defecto (`lunaris.interurban.payments.poll-ms`). Consulta [GET /v1/payments/{id}](https://www.mercadopago.com.ar/developers/es/reference/online-payments/subscriptions/get-payment/get) con Bearer token, URL fija HTTPS y timeouts de conexión/lectura de 3/5 segundos. El I/O ocurre fuera de la transacción de confirmación. Reintentos vuelven a consultar el estado real. Los errores técnicos conservan el inbox pendiente y un diagnóstico sanitizado; supervisar pendientes e intentos para detectar problemas persistentes.

Antes de confirmar valida ID del pago, collector-id, live-mode, ARS, external_reference `INTERURBAN:<booking_uuid>` e importe igual a la suma de snapshots `fare` del grupo. El futuro checkout debe crear esa referencia y congelar pasajeros, precios y bookingId; no agregar pasajeros ni reutilizar bookingId después de iniciar el pago. No se implementa creación de preferencias/cobros en esta etapa.

El procesador bloquea inbox, pago mediante advisory lock, viajes por ID y reservas por ID. Sólo confirma grupos completamente HELD, con holds vigentes, viajes no cancelados/completados y claims en todos los segmentos. No recrea cupos vencidos. Importe incorrecto, referencia ajena, pago adicional sobre un grupo ya pagado o llegada tardía generan `PAYMENT_REVIEW_REQUIRED` en outbox y se conservan para resolución operativa.

La transacción persiste pago APPROVED, cambia HELD→PAID y genera cada QR con su outbox. Un error revierte todo, incluyendo cualquier QR anterior del mismo grupo. Evento repetido o notificación distinta del mismo pago aprobado no vuelve a emitir pases. Un pago pendiente no bloquea una notificación posterior de aprobación. Los reintentos técnicos son distintos de las incidencias terminales de negocio: estas últimas necesitan revisión, no reintento automático infinito.

## Token, imagen y almacenamiento

`QrGeneratorService` requiere una transacción activa y una reserva PAID bloqueada. Genera 32 bytes con SecureRandom (256 bits), los representa como Base64URL sin padding y renderiza con ZXing un PNG de 320×320. El QR contiene únicamente el token opaco. `qr_tokens.token_hash` contiene SHA-256 hexadecimal del texto Base64URL (UTF-8), convención que debe usar el futuro check-in.

El PNG se cifra con AES-256-GCM, nonce aleatorio de 12 bytes y reservationId como AAD. `qr_artifacts` conserva ciphertext y key-id; formato binario `nonce || ciphertext || tag`. No hay token en claro en tablas, respuesta HTTP, outbox ni logs. El PNG recuperable evita regenerar tokens al reintentar una futura entrega. `QR_READY` contiene sólo reservationId, como referencia al artefacto privado.

La expiración propuesta es departure_at + 12 horas; debe confirmarse con operación antes de producción. El check-in futuro deberá verificar también estado de reserva/pago, expiración, consumed_at y chofer autorizado. Esta etapa NO implementa lector QR, endpoint público para PNG ni envíos WhatsApp.

Reembolso total/parcial o chargeback de un pago ya reconocido revoca el QR mediante expires_at, cambia PAID→REFUNDED y registra revisión. CHECKED_IN se conserva para auditoría; los créditos/liquidaciones requieren compensaciones de la etapa contable futura. Un estado local REVOKED impide resucitar el pase con una aprobación atrasada. No hay reembolso o transferencia automática.

## Validación

`MercadoPagoWebhookServiceTest` simula ingreso firmado y pago aprobado, verifica PAID, descifra y decodifica el PNG, confirma 256 bits y compara el hash persistido. Cubre duplicados, firma/cuerpo alterado, monto/collector incorrecto, hold vencido, pendiente→aprobado, error API, rollback solicitado y revocación. Tests adicionales verifican firma, cliente HTTP, controlador y activación condicional.

`InterurbanPaymentPostgresTest` usa proxies Spring y PostgreSQL real: dos eventos concurrentes producen un pago y un QR por pasajero; un fallo en el segundo pase revierte pago, estados, tokens y artefactos, manteniendo inbox pendiente.

```bash
# Sólo base DESECHABLE con V129 previamente aplicada:
psql "$INTERURBAN_TEST_DATABASE_URL" -v ON_ERROR_STOP=1 -f src/main/resources/db/migration/V130__interurban_encrypted_qr_artifacts.sql
INTERURBAN_TEST_JDBC_URL=jdbc:postgresql://localhost:55439/postgres \
INTERURBAN_TEST_DB_USER=usuario ./mvnw test
```

Sin la variable JDBC se omiten las pruebas PostgreSQL. La consulta HTTP al proveedor se simula: no se usaron credenciales reales ni se cobraron pasajeros. La habilitación productiva requiere probar el contrato con una cuenta sandbox y definir entrega privada, revisión de incidencias y política operativa de expiración.
