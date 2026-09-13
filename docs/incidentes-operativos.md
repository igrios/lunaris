# Cambios operativos

## Monitor

La Torre de Control muestra todas las sesiones existentes, con el pasajero y el último mensaje. La consulta elige un único pasajero por teléfono y un único mensaje para evitar multiplicar filas. Los operadores pueden pausar y reactivar por POST con CSRF. La tabla se actualiza mediante `/topic/bot-monitor` y los avisos existentes; no recarga la página. V125 agrega la marca de pausa manual para que el cierre de jornada no la revierta.

## Facturación y datos históricos

Pendientes y emisión comparten `BookingInvoiceAmount`, agrupando primero por `booking_group_code`. Una factura ya emitida excluye todos los tramos del grupo de pendientes.

El escritor actual divide el total entre tramos: $44.500 + $44.500 = $89.000. Es incorrecto dividir nuevamente todos los ROUND_TRIP por dos. V126 agrega `amount_is_group_total`, cuyo valor predeterminado es false para conservar ese contrato. Si los tramos guardan el total repetido, todos los tramos del grupo deben tener esta marca en true: $89.000 + $89.000 se factura como $89.000. Los extras siguen siendo importes de cada tramo y se suman. Se rechazan grupos con marcas mezcladas o totales contradictorios.

La migración no identifica ni modifica automáticamente grupos históricos duplicados. Hace falta un código de reserva afectado o una regla histórica verificable para marcar esos registros. La igualdad entre importes no permite distinguir un total repetido de dos mitades correctas. Esta corrección de datos queda pendiente de esa información; las facturas históricas emitidas no se modifican.

## Disponibilidad y reservas manuales

La capacidad predeterminada es 19 butacas. Las opciones del bot contemplan la cantidad solicitada y un margen mínimo de 60 minutos, usando hora argentina y fechas completas para evitar errores al cruzar medianoche. Si todavía no se conoce la fecha, se muestran bloques orientativos; se revalida al conocerla y al confirmar. Un turno sin opciones deriva a la captura de una consulta para el operador; la lista de espera existente sigue atendiendo los casos de falta de cupo al confirmar.

La carga administrativa tradicional usa `saveManualReservationFlow`: CONFIRMED y SCHEDULED, sin inventar verificación de pago. Las vueltas sin fecha conservan OPEN_RETURN. Una confirmación administrativa explícita de una reserva cancelada restablece SCHEDULED y valida cupo. Se agregan eventos y se conserva el historial de cancelaciones.

## Verificación

`./mvnw test`: 491 pruebas, sin fallos, errores ni omitidos. Incluye repositorios sobre H2 y renderizado Thymeleaf con control de permisos y CSRF. Las migraciones PostgreSQL están preparadas en el repositorio; no se aplicaron sobre una base productiva. `node --check src/main/resources/static/js/bot-monitor.js` pasó.
