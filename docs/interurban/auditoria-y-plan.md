# Corredor Ansenuza: auditoría y propuesta de integración

Fecha: 2026-09-20. Alcance: diagnóstico, migración aditiva y propuesta de servicios; no se activan endpoints, jobs, cobros ni transferencias. La compatibilidad absoluta no se puede certificar sólo con inspección y tests: requiere ensayo de migración y regresión sobre copia anonimizada de producción.

## Diagnóstico basado en el repositorio

| Área | Evidencia | Consecuencia |
|---|---|---|
| Plataforma | `pom.xml`: Java 21, Spring Boot 3.5.14, JPA, PostgreSQL, Flyway | Mantener stack; ZXing todavía no figura como dependencia. |
| Arquitectura | `com.lunaris.ansenuza.application`, `domain`, `infrastructure`; segundo módulo `reservation` con puertos/adaptadores | Hay dos representaciones JPA de `public.reservations`. No crear una tercera sobre esa tabla. |
| Viajes | V4, V7, V22, V36, V40, V47, V104/V55, V109, V113, V114 | El viaje regular vive en reservas, con ida/regreso, horario, chofer y secuencia. No existe `trips` general; `special_trips` (V111) es otra funcionalidad. |
| Precios | V5 `fares(locality_name, amount)`, V118 índices | No modela una matriz direccional del corredor. Preservar tarifas y endpoints vigentes. |
| Finanzas | V6, V23, V32, V119–V121, V126 | Hay extras, facturas, saldo aplicado y alcance de importes por grupo. No reinterpretar esos importes para liquidar interurbano. |
| Capacidad | `ReservationService.lockAndValidateCapacity`, V114 `reservation_capacity_locks` | El bloqueo actual agrupa fecha/horario/sentido; no representa asientos por segmento físico y vehículo. |
| Pagos | `MercadoPagoImapAdapterConfig`, `ProcessBankEmailService`, V110 | Se ingieren correos; se deduplican transacciones y se usa outbox. Autoconfirmación desactivada por defecto. No se encontró webhook HTTP MP `payment.updated`, ni adaptador de transferencias. |
| Chofer | `DriverViewController`, `DriverAuthorizationService`, `SecurityConfig` | Existe hoja de ruta y validación de pertenencia por teléfono de cuenta. No existen los dos endpoints nuevos solicitados. |
| Seguridad | Dos `SecurityFilterChain`; `/api/**` usa sesión opcional y CSRF desactivado; fallback `authenticated()` | Los endpoints nuevos necesitan reglas explícitas; cualquier autenticado no equivale a CHOFER. |
| Jobs | `@EnableScheduling`, schedulers de regreso, expiración y notificaciones | Un `@Scheduled` solo no evita ejecuciones en varias réplicas ni recupera ejecuciones perdidas. |
| Flyway | Última versión observada V128; producción usa `validate`, desarrollo `update` y Flyway desactivado | V129 está libre en esta revisión. Ensayar con configuración equivalente a producción. |

## Aislamiento y migración

`src/main/resources/db/migration/V129__add_interurban_corridor.sql` crea un esquema nuevo `interurban`. Incluye las cuatro tablas solicitadas y soportes necesarios: viajes, reservas individuales, asientos, pagos/inbox, QR, órdenes/items de liquidación y outbox. No contiene ALTER, UPDATE ni DELETE sobre objetos existentes, ni cambia el search_path. UUID se suministra desde la aplicación; no necesita nuevas extensiones ni valores de tarifas inventados.

Los identificadores de chofer y vehículo son referencias lógicas al dominio existente, sin FK hacia tablas productivas. Un puerto de lectura valida existencia, actividad, capacidad física y disponibilidad. Antes de activar hay que resolver agenda compartida con Córdoba: una consulta seguida de asignación no impide que otro operador asigne simultáneamente el mismo recurso. Primera etapa: recursos dedicados al corredor; flota compartida sólo con coordinación atómica común acordada y probada. La migración por sí sola no resuelve este conflicto.

Mantener los paquetes existentes. Moverlos a `com.lunaris.service.longdistance` cambiaría escaneo, imports y wiring sin beneficio para este alcance. Usar `com.lunaris.ansenuza.service.interurban` bajo el escaneo actual. Si se exige literalmente `com.lunaris.service.interurban`, importar su configuración explícitamente: queda fuera del paquete raíz actual. Ver propuesta de clases en [servicios.md](servicios.md).

Activación futura mediante `@ConditionalOnProperty(prefix="lunaris.interurban", name="enabled", havingValue="true", matchIfMissing=false)` en configuración exclusiva. El flag no evita que Flyway aplique V129 al arrancar. Para probar sólo el SQL usar base desechable; para probar actualización real arrancar sobre copia con historial Flyway existente, nunca ejecutar migraciones históricas manualmente en producción.

## Capacidad y cierre

Paradas ordenadas: SG=0, SUA=1, MOR=2, BRI=3. Para O→D, segmentos con ordinal en `(min(O,D), max(O,D)]`. Validar O≠D y sentido del viaje; invertir orden para vuelta. Disponibilidad = **mínimo** de plazas libres en segmentos requeridos, no suma. Ocupación [2,4,1] permite 0 SG→BRI y 3 MOR→BRI.

Una reserva por pasajero; acompañantes comparten booking_id, con QR individual. Mantener el grupo en el mismo vehículo salvo consentimiento explícito para dividirlo. Una reserva SG→BRI inserta tres filas de `leg_seats`. La PK `(trip_leg_id, seat)` y rango 1..4 limitan a cuatro ocupantes por tramo. Las FK compuestas impiden asignar una reserva a segmentos de otro viaje. La integridad de recorrido completo, sentido, horarios y estados se valida en el servicio transaccional; el SQL no la garantiza por sí solo.

Bloquear primero viajes por UUID y luego todos sus segmentos por ID ascendente (`SELECT ... ORDER BY tl.id FOR UPDATE OF tl`), según el requerimiento de implementación de etapa 1; releer estado/cierre, calcular, insertar todas las plazas y reserva, confirmar. Rollback integral ante falta de lugar. Lectura de disponibilidad es orientativa, nunca una promesa de venta. Holds también ocupan capacidad; expiración/cancelación liberan filas bajo los mismos bloqueos. Check-in NO libera asientos. Mantener ocupación histórica hasta finalizar el viaje.

Propuesta operativa a confirmar antes de activar: cierre a las 20:00 del día anterior al servicio. `closes_at` explícito permite otra política sin inferirla del cron. Rechazar reservas cuando `now >= closes_at` aunque el scheduler esté caído. Cron `0 0 20 * * *`, zona `America/Argentina/Cordoba`; recuperar cierres vencidos al reiniciar. Cobros aprobados tardíamente pasan a revisión/reembolso si venció el hold; nunca recrear capacidad sin verificarla.

Vender sobre viajes virtuales de cuatro plazas con oferta limitada por flota comprometida; no crear infinitos vehículos para aceptar pagos. A las 20:00 cerrar viajes vencidos y agrupar pasajeros por sentido, fecha, ventana horaria, direcciones y segmentos compatibles. Se pueden transportar más de cuatro personas a lo largo del recorrido si no coinciden más de cuatro por tramo. Usar first-fit determinista sobre intervalos, validando matrices de ocupación y tiempos puerta a puerta; una heurística no garantiza mínimo de vehículos. Congelar asignación y generar hoja de ruta tras commit.

Fallback: chofer local disponible → chofer que llegue con posicionamiento vacío factible → flota propia disponible. Excluir superposición de trabajo, documentación/vehículo inactivo y posicionamiento incompatible. Registrar origen de asignación; si no hay recurso, incidencia operativa persistida y aviso por outbox, sin declarar asignado ni sobrecargar. Cambios de vehículo antes de abordaje migran todas las ocupaciones atómicamente; después requieren intervención controlada.

## Pagos y QR

Crear endpoint separado `POST /webhook/interurban/mercadopago`; conservar IMAP y confirmación de Córdoba. Validar firma `x-signature` HMAC según [documentación oficial de notificaciones](https://www.mercadopago.com.ar/developers/en/docs/wallet-connect/notifications), usando los campos exactos de la firma, comparación constante y política de antigüedad compatible con reintentos. Persistir inbox antes del 2xx; fallo de persistencia devuelve error reintentable. Deduplicar evento, pero no descartar todas las actualizaciones por payment_id: el pago puede cambiar de estado.

Worker consulta [GET /v1/payments/{id}](https://www.mercadopago.com.ar/developers/es/reference/online-payments/subscriptions/get-payment/get), verifica `approved`, collector esperado, moneda ARS, importe total y external_reference inequívoca `INTERURBAN:<booking_id>`. No confiar en estado/monto del webhook ni en retorno del navegador. Una transacción bloquea booking/reservas, registra pago idempotente, marca PAID y añade evento QR. Refund/chargeback invalida QR y registra compensaciones, sin borrar asientos contables ni resucitar reservas por eventos atrasados.

`QrGeneratorService` genera token aleatorio de 256 bits por pasajero y representa el pase con ZXing (dependencia futura). Guardar SHA-256 en `qr_tokens`, expiración y consumo; jamás PII en QR/logs. Para entrega reintentable, guardar token cifrado o artefacto QR privado en almacenamiento con acceso restringido y referencia en outbox; no regenerar sin revocar el anterior. Evento de generación único por reserva; recuperación tras caída y revocación deben estar definidas. No incluir secret/token en outbox sin cifrar.

## Contratos PWA propuestos

| Endpoint | Acceso y contrato |
|---|---|
| `GET /api/v1/driver/route-sheet?date=YYYY-MM-DD` | CHOFER sólo identidad autenticada; ADMIN con selección explícita y auditada. Devuelve trips y paradas pickup/dropoff ordenadas por scheduledAt + sequence, dirección, nombre, teléfono E.164, `tel:` y enlace `https://wa.me/<dígitos>`, reserva y estado. No aceptar driverId arbitrario para chofer. |
| `POST /api/v1/checkin/verify` | CHOFER asignado al viaje, ADMIN auditado. Body `{token, tripId}`; no recibir monto ni driverId autoritativos. Verifica pago, expiración, viaje, día/ventana de abordaje, identidad y token. Transacción: consumir QR, CHECKED_IN, fijar chofer efectivo e insertar EARNED único por reserva. |

200 para primer escaneo y repetición legítima del mismo check-in, sin doble crédito; 400 formato inválido, 401 sin sesión, 403 sin permiso/pertenencia, 409 estado o viaje incompatible, 410 token vencido/revocado. Errores de dominio centralizados con `@ControllerAdvice`. No revelar datos de tokens ajenos. Rate limit y auditoría sin token.

Reglas específicas para estos paths antes del fallback de SecurityConfig. FACTURACION puede consultar liquidaciones mediante futuros endpoints contables; OPERADOR gestiona asignaciones; ninguno cobra o escanea sólo por estar autenticado. Para PWA con cookies, habilitar CSRF en POST nuevo con matcher acotado o cadena específica que preserve rutas actuales; no ampliar la exclusión actual. `html5-qrcode` sólo decodifica: backend autoriza. HTTPS/cámara; sin conexión se puede mostrar ruta cacheada con datos mínimos y caducidad, pero no confirmar abordaje ni crédito offline.

## Liquidación de las 22:00

`DailyDriverPayoutJob`: cron `0 0 22 * * *`, zona Argentina; ventana hasta instante de corte y recuperación tras caída. Crédito nace al CHECKED_IN, no al recibir pago ni cada noche. Neto = Σ(tarifa snapshot − comisión snapshot) de pasajeros escaneados; sólo equivale a N×tarifa−N×comisión cuando son iguales. No multiplicar por tres segmentos un boleto SG→BRI. BigDecimal y NUMERIC, nunca double; comisión fija en ARS por pasaje en esta propuesta, sin inventar porcentajes.

Bloquear saldo/entradas de cada chofer de manera consistente. Seleccionar créditos y compensaciones no incluidos en `payout_items`, crear orden única `(driver_id, settlement_date)` y asociar items en una transacción. No liquidar por consulta ingenua de todos los CHECKED_IN: repetiría pagos. Si neto ≤0, mantener saldo para futura compensación. Escaneos posteriores al corte se liquidan al siguiente corte; no agregar a una orden ya enviada. Ajustes posteriores son entradas nuevas y se trasladan al siguiente lote.

Commit de orden+outbox antes de I/O. Worker reclama orden, envía con clave idempotente estable; ante timeout registra UNKNOWN y concilia antes de reintentar. PAID sólo con confirmación del proveedor; recién entonces agrega asiento PAYOUT negativo único, en la misma transacción que el cambio de estado. Saldo contable = suma del ledger; saldo disponible descuenta además órdenes pendientes/UNKNOWN para no volver a disponer de fondos comprometidos.

**`POST /v1/transfers` no está verificado para esta cuenta/producto.** La [referencia oficial consultada](https://www.mercadopago.com.ar/developers/es/reference) no aportó un contrato verificable para esa operación. Esto no demuestra que no exista bajo acuerdos específicos. Diseñar `DriverPayoutPort`, mantener adaptador deshabilitado y producir orden interna revisable hasta contar con documentación, habilitación comercial, idempotencia y conciliación probadas en sandbox. No implementar una llamada ficticia ni marcarla pagada.

## Plan de adopción y validación

1. Ensayar V129 en PostgreSQL y en copia del esquema/historial real. Comparar objetos y datos public antes/después; mantener checksum de migraciones anteriores. Revisar privilegio CREATE SCHEMA del usuario Flyway y permisos DML del usuario de aplicación.
2. Implementar servicios y adapters aislados, flag apagado; semillas de tarifas aprobadas, política de cierre/hold/escaneo y flota dedicada definidas. Mapping JPA `schema="interurban"`; UUID con `@GeneratedValue(strategy=GenerationType.UUID)` y fallback antes de save según convención del proyecto, verificando comportamiento persist/merge.
3. Tests PostgreSQL con transacciones independientes: cinco compradores simultáneos → máximo cuatro; recorrido parcial/vuelta; rollback de tres tramos; cancelación y expiración concurrentes con pago; cierre simultáneo; asignación multiinstancia y agenda de chofer; webhook duplicado/fuera de orden; dos escaneos → un crédito; dos jobs → una orden; timeout de transferencia → conciliación sin segundo envío.
4. Regresión de Córdoba: bot, manual, ida/vuelta, capacidad, extras, facturación, comprobantes, hoja de ruta y permisos. La suite actual H2 no sustituye pruebas de migraciones PostgreSQL.
5. Piloto con recursos dedicados, observar ocupación, hold vencido, inbox/outbox atrasado, asignaciones pendientes y pagos UNKNOWN. Activación gradual sólo después de validar proveedor de pagos.
6. Reversión operativa: apagar flag y detener nuevas ventas; resolver viajes/cobros en curso. Conservar tablas e historial; no ejecutar DROP ni limpiar ledger. El flag debe dejar accesible la gestión de operaciones ya comprometidas mediante runbook de soporte.

Resultados de ejecución de esta auditoría: ver [validacion.md](validacion.md).
