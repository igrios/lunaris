# Alta manual, factura y confirmación por WhatsApp

El panel de este repositorio es Thymeleaf/JavaScript (no contiene un frontend React/Vite). Los formularios `/reservations/new` y el monitor de chat usan `CreateManualReservationUseCase`.

## Importes

- `amount` vacío solicita la tarifa vigente; un valor explícito, incluido cero, la reemplaza.
- `amount` es el importe final con descuento incluido. `discountAmount` registra ese descuento; no se descuenta otra vez. `extraAmount` se suma al total.
- En ida y vuelta, el importe y el descuento corresponden al grupo y se distribuyen entre tramos. El adicional se registra una sola vez.
- El alta manual no consume automáticamente saldo a favor ni marca el pago como verificado.
- La modalidad de regreso determina `tripType`: solo ida, ida y vuelta o vuelta abierta.

## Facturación manual

No se integra AFIP/ARCA ni se emiten comprobantes automáticamente. El panel de facturación informa el monto total del grupo (`amount + extraAmount`, incluyendo todos los tramos) con dos decimales para que una persona emita la factura por fuera del sistema.

Al crear una reserva con `requiresInvoice=true`, se registra la solicitud y se confirma la reserva por WhatsApp. No se genera ni se envía una factura en ese momento. Si no hay comprobante disponible, la confirmación informa que la factura está pendiente de emisión y envío por administración.

Después de confirmar el pago, la persona puede cargar el PDF emitido y pulsar «Enviar» en el panel existente (`POST /facturacion/emitir/{reservationId}`). Esa acción explícita asocia el documento y solicita el envío por WhatsApp; con ventana cerrada se usa HSM y se entrega el PDF al responder. Se conserva la acción manual de reenvío.

El PDF se almacena mediante `InvoiceStoragePort`; el link público se asocia a todos los tramos en `invoice_url`. `payment_receipt_url` conserva la evidencia de pago. Configurar `lunaris.public-base-url` con la URL pública de la instalación.

## Plantilla que debe existir aprobada en Meta

Nombre predeterminado: `reservation_and_bot_promo`. Configurable con `whatsapp.templates.reservation-and-bot-promo`. Idioma utilizado por el adaptador: `es`. La aprobación en Meta es un requisito externo; agregar este código no registra ni aprueba la plantilla.

```text
¡Hola {{1}}! Tu reserva fue registrada con éxito 🚌✨

📍 Trayecto: {{2}} -> {{3}}
📅 Fecha y hora: {{4}}
🎟️ Código de reserva: {{5}}
📄 Factura/Comprobante: {{6}}

💡 Tip Lunaris: ¡La próxima vez podés pedir tu viaje directamente por acá en 1 minuto! Nuestro Bot automático está disponible las 24 hs para cotizar, reservar y confirmarte al instante sin esperas. ¡Probalo en tu próximo viaje!
```

| Parámetro | Contenido |
| --- | --- |
| 1 | Nombre del pasajero |
| 2 | Localidad de retiro |
| 3 | Destino |
| 4 | Fecha y horario |
| 5 | Código de reserva |
| 6 | Link de factura, comprobante de pago o indicación de pendiente/no solicitado |

Con ventana abierta se envían texto completo y PDF, cuando existe. Con ventana cerrada se envía la plantilla y queda persistida la espera de respuesta. Un mensaje/interacción recibido por el webhook firmado activa el detalle y PDF; las notas de oficina no se envían al pasajero.

Los resultados del adaptador registran aceptación/rechazo del envío por Meta, no lectura ni entrega final al dispositivo. Los envíos fallidos se reintentan cada 60 segundos (`lunaris.manual-notification.retry-ms`). Se usa un reclamo de cinco minutos para evitar envíos concurrentes y recuperar intentos interrumpidos. Como Meta no recibe una clave de idempotencia, un reinicio después de aceptar el mensaje y antes de guardar el resultado puede producir un reenvío.

Aplicar Flyway V128 antes de ejecutar esta versión.
