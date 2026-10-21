# Alta manual, factura y confirmación por WhatsApp

El panel de este repositorio es Thymeleaf/JavaScript (no contiene un frontend React/Vite). Los formularios `/reservations/new` y el monitor de chat usan `CreateManualReservationUseCase`.

## Importes

- `amount` vacío solicita la tarifa vigente; un valor explícito, incluido cero, la reemplaza.
- `amount` es el importe final con descuento incluido. `discountAmount` registra ese descuento; no se descuenta otra vez. `extraAmount` se suma al total.
- En ida y vuelta, el importe y el descuento corresponden al grupo y se distribuyen entre tramos. El adicional se registra una sola vez.
- El alta manual no consume automáticamente saldo a favor. El operador valida el pago mediante la casilla correspondiente o adjuntando explícitamente un comprobante; se sincronizan `payment_verified=true`, `payment_status=PAID` y `requires_invoice=true`. Las reservas confirmadas o con comprobante quedan visibles en facturación, aunque aún falte validar el pago.
- La modalidad de regreso determina `tripType`: solo ida, ida y vuelta o vuelta abierta.
- El formulario de viajes especiales envía `tripCategory=SPECIAL`, `originCustom`, `destinationCustom`, `customPrice` y `customPriceIsTotal=true`: el precio cubre todos los pasajeros y ambos tramos si hay regreso. El pago inicial es `PENDING` salvo que el operador lo valide o adjunte un comprobante. Las cargas existentes que omiten `customPriceIsTotal` conservan el precio por persona. Flyway V134 agrega esta distinción sin modificar importes históricos.

## Facturación manual

No se integra AFIP/ARCA ni se emiten comprobantes automáticamente. El panel de facturación informa el monto total del grupo (`amount + extraAmount`, incluyendo todos los tramos) con dos decimales para que una persona emita la factura por fuera del sistema.

Al crear una reserva con `requiresInvoice=true`, se registra la solicitud y se confirma la reserva por WhatsApp. No se genera ni se envía una factura en ese momento. Si no hay comprobante disponible, la confirmación informa que la factura está pendiente de emisión y envío por administración.

Después de confirmar el pago, la persona puede cargar el PDF emitido y pulsar «Enviar» en el panel existente (`POST /facturacion/emitir/{reservationId}`). Esa acción explícita asocia el documento y solicita el envío por WhatsApp; con ventana cerrada se usa HSM y se entrega el PDF al responder. Se conserva la acción manual de reenvío.

El PDF se almacena mediante `InvoiceStoragePort`; el link público se asocia a todos los tramos en `invoice_url`. `payment_receipt_url` conserva la evidencia de pago. Configurar `lunaris.public-base-url` con la URL pública de la instalación.

## Plantilla compartida con Chat en Vivo

El botón «Hablar con Pasajero» de `/admin/chat/{phone}` envía `POST /admin/chat/{phone}/contactar`. `ChatController.reopenConversation` llama a `WhatsAppService.sendContactoPasajeroTemplate`.

Reservas y facturas manuales usan el mismo contrato `PassengerContactTemplate`: nombre `contacto_pasajero`, idioma `es` y un único parámetro de cuerpo, el nombre del pasajero (o «Pasajero» si falta). No se inyectan los seis campos del resumen en esta plantilla. No se necesita configurar una plantilla adicional.

El resumen completo de reserva, el enlace del comprobante y el mensaje promocional del bot se envían como texto al abrirse la ventana mediante una respuesta del pasajero.

Con ventana activa el alta envía una sola confirmación consolidada. Con ventana cerrada inicia con la plantilla aprobada y persiste la espera de respuesta. Su único placeholder sigue siendo el nombre: el backend no puede modificar el texto aprobado en Meta. Al responder el pasajero, se envía el detalle con dirección y acompañantes pendientes cuando corresponda, pago pendiente, invitación a responder libremente para que los operadores lo anoten y el recordatorio del bot. Para incluir ese contexto en el primer mensaje sería necesaria una plantilla aprobada que lo contemple.

El evento de alta se procesa explícitamente en `AFTER_COMMIT`; las llamadas al adaptador se ejecutan sin una transacción de base de datos activa. Los envíos posteriores de facturas conservan la entrega directa con ventana abierta y usan plantilla con ventana cerrada. Un mensaje/interacción recibido por el webhook firmado activa el detalle y PDF; las notas de oficina no se envían al pasajero.

Los resultados del adaptador registran aceptación/rechazo del envío por Meta, no lectura ni entrega final al dispositivo. Los envíos fallidos se reintentan cada 60 segundos (`lunaris.manual-notification.retry-ms`). Se usa un reclamo de cinco minutos para evitar envíos concurrentes y recuperar intentos interrumpidos. Como Meta no recibe una clave de idempotencia, un reinicio después de aceptar el mensaje y antes de guardar el resultado puede producir un reenvío.

Aplicar Flyway V128 antes de ejecutar esta versión.

## Solicitudes especiales desde el monitor

El monitor muestra el detalle estructurado de las sesiones con `tripCategory=SPECIAL` y la última consulta pendiente de cada teléfono. Las consultas de la opción «Consulta / Viaje Especial» son texto libre y no se clasifican ni cotizan automáticamente. El operador puede usar «Registrar / editar solicitud especial» para confirmar origen, destino, fecha, pasajeros y precio total; los datos aún desconocidos quedan pendientes.

«Crear reserva especial» abre `/reservations/new?sessionId=...` y carga los datos de la sesión y del pasajero desde el servidor. Abrir el formulario no crea la reserva ni confirma el pago. El operador revisa y completa los campos antes de guardar. Aplicar Flyway V135 para los campos de la sesión.

## Borrador de reserva asistida y mensajes libres

El chat y el formulario abierto desde el monitor comparten un borrador por teléfono del chat. El último mensaje entrante puede sugerir una dirección explícita (por ejemplo, «Dirección: Belgrano 450» o una línea «Bv. Belgrano 450») y referencias etiquetadas. El mensaje original permanece visible. Un teléfono mencionado se muestra como sugerencia y no reemplaza el número del chat. No se deducen nombres, fechas ni horarios ambiguos.

«Guardar borrador» permite datos incompletos. «Guardar y abrir formulario completo» y «Guardar y volver al chat» guardan antes de cambiar de pantalla. Los campos editados tienen prioridad sobre nuevas sugerencias. El borrador no reserva asientos, no crea pasajeros, no registra pagos ni envía mensajes; la confirmación definitiva mantiene las validaciones de dominio. Se almacena fuera de la sesión para sobrevivir a su limpieza. Aplicar Flyway V136.

## Edición e integridad fiscal

El formulario asistido transporta `reservationId` al cargar una reserva existente. El caso de uso actualiza el grupo bajo bloqueo, conserva IDs y códigos, distribuye el total entre los mismos tramos y valida capacidad excluyendo sus propias butacas. No convierte una edición en otra alta. La cantidad de tramos se conserva: agregar o cancelar un regreso usa su operación específica. Los grupos facturados, cancelados o ya despachados no se modifican por este formulario.

La categoría enviada por el operador se respeta tanto en el POST tradicional como en la API. Solo el comprobante seleccionado explícitamente se vincula; los enlaces del historial no se interpretan automáticamente como pagos.

Confirmación y PDF tienen estados de entrega separados. Registrar una factura no repite la confirmación ya entregada ni otra plantilla mientras se espera respuesta. Un fallo del PDF reintenta únicamente el documento; volver a guardar los mismos datos no dispara otra confirmación. La acción explícita de reenvío puede reenviar el PDF. La factura se envía automáticamente después de registrar su PDF y de la confirmación, sin integrar emisión fiscal automática. Aplicar V137 para reparar banderas históricas y conservar los estados de entrega.
