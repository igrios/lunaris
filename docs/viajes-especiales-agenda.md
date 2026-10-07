# Viajes especiales de agenda

La agenda persiste en `reservations`. `trip_type` conserva `ONE_WAY`, `ROUND_TRIP` y
`OPEN_RETURN`; la categoría comercial se agrega como `trip_category` (`REGULAR` / `SPECIAL`).
`passenger_count` ya existe. La migración V133 agrega los demás campos a esa tabla y
conserva pagos y facturas históricos. No requiere cambios en el módulo interurbano ni
en el catálogo promocional `special_trips`.

## Alta compartida

El formulario existente `/reservations/new` (también accesible desde el monitor)
usa Thymeleaf. Ahora permite elegir Especial, ingresar origen/destino libres, cantidad
de pasajeros, horario libre y precio total acordado. El alta conserva el estado
operativo `CONFIRMED` / `SCHEDULED` para aparecer en agenda, con pago `PENDING`,
`payment_verified=false` e `invoice_issued=false`. No vence a los veinte minutos.

La agenda semanal y diaria tienen accesos «Cargar viaje especial». Abren
`/reservations/new?tripCategory=SPECIAL`; desde el detalle diario también se precarga
`travelDate`. En modo especial el formulario Thymeleaf envía JSON autenticado a
`POST /api/admin/trips`, usando el mismo contrato del componente React de referencia.
Después del alta abre el detalle de la fecha seleccionada. Si la API rechaza los
datos, muestra el motivo en el mismo formulario y conserva los valores ingresados.
El botón se bloquea mientras se guarda para evitar envíos simultáneos.

El modo regular conserva el envío tradicional al controlador del monitor. Las
localidades, horarios y cotizaciones de Córdoba no se consultan al abrir el formulario
especial. Los datos flexibles se validan mediante el caso de uso compartido; la
confirmación WhatsApp conserva el envío posterior al commit y los reintentos existentes.

El precio especial cubre todo el grupo: no se multiplica por pasajeros ni tramos,
no descuenta saldo a favor y no incorpora descuentos/adicionales de rutas regulares.
Para ida y vuelta el importe se distribuye entre ambos tramos para que la factura
posterior cubra exactamente el total acordado. Estos servicios tienen una sección
propia en la agenda diaria y no consumen cupos de los turnos regulares.

La confirmación usa el adaptador existente de WhatsApp Cloud API después del commit.
En ventana activa envía el detalle y el estado del pago. Fuera de ventana envía la
plantilla existente `contacto_pasajero`, y entrega el detalle al recibir respuesta.
Los envíos fallidos se reintentan con el mecanismo existente. No envía PDF fiscal
mientras el pago esté pendiente.

## API para React

`POST /api/admin/trips` (ADMIN / OPERADOR), cuerpo de ejemplo:

```json
{
  "tripCategory": "SPECIAL",
  "firstName": "Ana",
  "lastName": "Pérez",
  "phone": "3511234567",
  "travelDate": "2030-01-02",
  "pickupAddress": "Belgrano 100",
  "originCustom": "De Suardi",
  "destinationCustom": "Alta Gracia",
  "passengerCount": 12,
  "customPrice": "150000.00",
  "departureSchedule": "09:30",
  "roundTrip": false,
  "paymentStatus": "PENDING",
  "requiresInvoice": true
}
```

Responde 201 con una lista de reservas (una por tramo), sus identificadores, categoría,
precio personalizado, `paymentStatus` e `invoiceIssued`. Para REGULAR se envían
`pickupLocality` y `destination`, y opcionalmente `amount`; los campos personalizados
se omiten. Las reglas de origen, fecha, pasajeros y precio se verifican en el dominio.
Los viajes especiales no pueden darse de alta como PAID.

`POST /api/admin/trips/{id}/paid` (ADMIN / OPERADOR / FACTURACION), sin cuerpo:
registra el pago de todos los tramos bajo bloqueo pesimista, con fecha y evento de
auditoría del usuario autenticado. Es idempotente; no emite una factura ni modifica
la fecha del pago ante reintentos. Rechaza viajes cancelados, vencidos y regulares
(estos conservan su flujo de confirmación existente).

Una vez registrado el pago, la operadora usa el flujo existente de facturación para
subir el PDF fiscal. Se revalida el pago de todo el grupo al registrar la factura,
bajo bloqueo, y se actualiza `invoice_issued` de todos los tramos en esa transacción.
Solicitar factura al crear el viaje no implica emitirla automáticamente.

`docs/frontend/ManualTripForm.jsx` es un componente React de referencia para servicios
de un tramo; recibe `localities` (array de nombres), `apiBaseUrl` y `onCreated`. Consulta
los horarios regulares y usa el mismo endpoint para ambos tipos. El backend y el
formulario Thymeleaf también admiten ida y vuelta. No hay una app React ni un pipeline
de frontend en este repositorio; el componente debe incorporarse en la app externa.
Requiere la sesión autenticada del backend y un origen permitido por su CORS.
