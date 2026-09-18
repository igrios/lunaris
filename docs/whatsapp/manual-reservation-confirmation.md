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

## Plantilla compartida con Chat en Vivo

El botón «Hablar con Pasajero» de `/admin/chat/{phone}` envía `POST /admin/chat/{phone}/contactar`. `ChatController.reopenConversation` llama a `WhatsAppService.sendContactoPasajeroTemplate`.

Reservas y facturas manuales usan el mismo contrato `PassengerContactTemplate`: nombre `contacto_pasajero`, idioma `es` y un único parámetro de cuerpo, el nombre del pasajero (o «Pasajero» si falta). No se inyectan los seis campos del resumen en esta plantilla. No se necesita configurar una plantilla adicional.

El resumen completo de reserva, el enlace del comprobante y el mensaje promocional del bot se envían como texto al abrirse la ventana mediante una respuesta del pasajero.

Con ventana abierta se envían texto completo y PDF, cuando existe. Con ventana cerrada se envía la plantilla y queda persistida la espera de respuesta. Un mensaje/interacción recibido por el webhook firmado activa el detalle y PDF; las notas de oficina no se envían al pasajero.

Los resultados del adaptador registran aceptación/rechazo del envío por Meta, no lectura ni entrega final al dispositivo. Los envíos fallidos se reintentan cada 60 segundos (`lunaris.manual-notification.retry-ms`). Se usa un reclamo de cinco minutos para evitar envíos concurrentes y recuperar intentos interrumpidos. Como Meta no recibe una clave de idempotencia, un reinicio después de aceptar el mensaje y antes de guardar el resultado puede producir un reenvío.

Aplicar Flyway V128 antes de ejecutar esta versión.
