# Diagnóstico del flujo interurbano de WhatsApp

## Causa raíz

`LUNARIS_INTERURBAN_ENABLED=true` habilitaba infraestructura interurbana, pero no existía una transición del chatbot hacia ella. La selección de Morteros seguía la misma rama convencional que cualquier otro pueblo. La existencia de tarifas en otro esquema no modifica una consulta JPA ni el handler elegido.

El análisis corresponde al código del repositorio. No se inspeccionó Render ni la base de producción; la configuración y los registros de producción son los informados por el usuario.

## Recorrido y puntos exactos

Las referencias siguientes corresponden al código anterior al parche cuando se indica «original».

1. `application.yaml:64` y `application-prod.yml:3` enlazan correctamente `lunaris.interurban.enabled` con `${LUNARIS_INTERURBAN_ENABLED:false}`. No hay un `@Value` de esa propiedad en los handlers originales.
2. `InterurbanConfiguration.java:10` condiciona los beans de capacidad. `InterurbanDriverConfiguration` habilita operaciones de chofer. Los pagos exigen además `lunaris.interurban.payments.enabled=true`; la liquidación exige `lunaris.interurban.settlement.enabled=true`. Ninguno de estos beans originales registra un estado conversacional interurbano.
3. `WhatsAppWebhookController.java:150` entrega el mensaje a `ConversationOrchestrator.process`. El parser y el dispatcher manejan entrada/transporte; no eligen corredores ni tarifas. `WhatsAppService` y `WhatsAppMessagingAdapter` envían mensajes; no son la máquina de estados. No existen clases `WhatsAppBotService` ni `WhatsAppMenuService` en este backend.
4. `MainMenuHandler.java:45` y la rama de precios ponen la sesión en `ASK_LOCALITY`. `ConversationOrchestrator.java:245` busca un handler por el texto del estado. No consulta la bandera para elegir un producto; `:250` vuelve a START si no encuentra handler.
5. `ConversationPresenter.java:30` (original) construye el menú con `LocalityRepository.findAllWithActiveFare`. `LocalityRepository.java:25` une Locality con Fare y exige un importe positivo. Solo publica localidades con tarifas convencionales; una tarifa interurbana no participa de ese JOIN.
6. `AskLocalityHandler.java:53` (original) vuelve a consultar ese mismo listado. Su única bifurcación de ruta especial es el origen Córdoba, que pasa a `ASK_TOWN_DESTINATION`.
7. **Desvío determinante:** `AskLocalityHandler.java:84` (original; `:133` después del parche) llama incondicionalmente a `pricingAndScheduleService.calculateTripPrice(selected.getName(), true, 1)`. No tiene destino ni identificador de parada. `:97` (original; `:146` actual) pasa a `ASK_MARKETING_CONFIRMATION`.
8. `PricingAndScheduleService.java:136` resuelve la tarifa por localidad. `:169` invoca `FareRepository.findByLocalityNameIgnoreCase`. `Fare.java:19` mapea `@Table(name="fares")`, sin esquema interurbano. En el entorno descrito se resuelve a `public.fares`. No hay cambio de esquema ni repositorio en función de la bandera.
9. El mismo servicio usa reglas convencionales de ida/vuelta y respaldo por kilómetros hasta Córdoba. No puede representar la tarifa direccional Morteros→Brinkmann solo con el nombre de origen. El fallback incluso puede producir una cotización sin tarifa explícita.
10. Después de la confirmación comercial y captura de dirección, `AskAddressTextHandler.java:48` selecciona `ASK_DESTINATION` para un origen distinto de Córdoba. `AskDestinationHandler.java:35` solo reconoce `dest_aeropuerto` y `dest_capital`: Brinkmann no es un destino posible en esta rama.
11. `BotRoute.java:18` lista pueblos para salir desde Córdoba usando nuevamente tarifas convencionales. No interpreta paradas interurbanas.
12. `ConversationSession` persiste estado, origen y destino como texto; no contiene tipo de servicio, trip_id, fare_id ni ordinales interurbanos. `Locality` solo tiene UUID, nombre, kilómetros hasta Córdoba y minutos desde origen. **No existe un flag interurbano olvidado o pendiente de activar en public.localities.** Añadirlo por sí solo no conectaría el flujo.
13. `ConversationPresenter.java:102` (actual) vuelve a calcular un resumen convencional. `ConfirmationHandler.java:80` vuelve a cotizar y `:148` guarda mediante `ReservationService.saveReservationFlow`, en el modelo convencional. Por eso habilitar solo destinos intermedios y continuar por estos estados produciría una reserva incorrecta.

## Interpretación del esquema interurbano

`V129__add_interurban_corridor.sql:5` define tramos, no una tabla de localidades. El ordinal 2 es Suardi→Morteros y el ordinal 3 es Morteros→Brinkmann. Las paradas son los extremos: origen de un tramo ordinal N = parada N−1; destino = parada N. Morteros es parada 2 y Brinkmann parada 3.

La migración define tarifas direccionales versionadas por `valid_from`; no existe columna `active`. El parche considera vigente la última versión cuya fecha sea menor o igual a hoy en Argentina. No copia automáticamente la tarifa inversa. Si producción agregó reglas de baja o vigencia adicionales, habrá que incorporarlas a la consulta; no están presentes en las migraciones del repositorio.

## Parche aplicado

- `InterurbanCatalog` solo existe con la bandera en true. Consulta explícitamente `interurban.corridor_legs` e `interurban.interurban_fares`, reconstruye las paradas, y elige la última tarifa vigente por par direccional. Las lecturas son transacciones readOnly.
- El presenter y ASK_LOCALITY reciben opcionalmente el catálogo mediante constructor. Sus menús incluyen orígenes con tarifas interurbanas aunque no tengan tarifa convencional. Con la bandera ausente/false conservan el comportamiento previo.
- ASK_LOCALITY consulta destinos interurbanos antes de cotizar por localidad. Morteros pasa a `ASK_INTERURBAN_DESTINATION` cuando tiene una tarifa vigente. No necesita una modificación de public.localities.
- El destino se identifica con un código estable de parada (`i_3` para Brinkmann), no con una posición cambiante del listado. Se mantiene `c` para la cotización convencional hacia Córdoba/Aeropuerto y `0` para volver.
- ASK_INTERURBAN_DESTINATION valida otra vez los destinos vigentes, guarda el destino y presenta la tarifa por pasajero, solo ida, como referencia actual. ASK_INTERURBAN_CONFIRMATION permite volver o solicitar coordinación con un operador.
- La sesión se pausa únicamente cuando el pasajero solicita coordinar la reserva. Este flujo no entra en los estados que cotizan y reservan en public.

**Alcance:** queda implementada la activación, selección de destino y cotización interurbana. La confirmación de reserva es asistida. La reserva interurbana automática requiere un flujo adicional que capture fecha, viaje interurban.trips, direcciones y pasajeros, seleccione la tarifa para esa fecha, ejecute CapacityService.hold y conecte el pago. El módulo existente tiene la operación de hold, pero el bot no tenía esta integración. No se declara implementada por este parche.

## Validación y operación

`InterurbanBotRoutingTest` verifica Morteros→Brinkmann, ausencia de llamadas al pricing convencional en esa rama, origen con tarifa exclusivamente interurbana, bandera desactivada, rechazo de destino inválido y pausa después de solicitar reserva. Validación final: `./mvnw test` terminó con BUILD SUCCESS: 668 pruebas, 0 fallos, 0 errores y 13 omitidas. Las pruebas PostgreSQL condicionadas a INTERURBAN_TEST_JDBC_URL no se ejecutaron; no se verificó el catálogo contra una instancia PostgreSQL real en esta sesión.

Con el despliegue nuevo y la bandera en true, iniciar una conversación nueva o volver con Menú. Las sesiones que ya estén en ASK_MARKETING_CONFIRMATION/ASK_DESTINATION conservan su estado convencional hasta reiniciar: la bandera no migra conversaciones en curso.

Se seguirán viendo consultas a public.fares al construir el listado convencional y al elegir Córdoba. La señal de corrección es que al seleccionar Morteros se consulta el catálogo interurbano y se muestra `i_3) Brinkmann`, sin cotizar Morteros por PricingAndScheduleService en esa rama. Cambiar globalmente search_path no soluciona el problema y no es necesario.
