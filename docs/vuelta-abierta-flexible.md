# Vuelta abierta con horario flexible

El modelo activo usa `departure_schedule` en cada tramo, sin relación JPA ni columna base `return_schedule_id`. V138 elimina la obligatoriedad de esa columna únicamente si existe en la instalación y agrega `is_flexible_return_time` (false por defecto) y `return_time_preference` (hasta 500 caracteres).

La API de creación admite `tripType: "OPEN_RETURN"`, `isFlexibleReturnTime: true`, `returnDate` como fecha estimada y/o `returnTimePreference`. También acepta los alias `is_flexible_return_time` y `return_time_preference`. La opción requiere vuelta abierta y rechaza fechas anteriores a la ida, preferencias demasiado largas y ausencia de fecha y preferencia.

El tramo de regreso conserva la fecha estimada en `return_date`, pero mantiene `travel_date` y `departure_schedule` nulos y `travel_status = OPEN_RETURN`. Esto evita que una estimación aparezca como viaje programado o consuma cupo de un turno concreto. Los controles de capacidad de la ida siguen vigentes. El importe de ida y vuelta se calcula y distribuye con las reglas existentes.

La programación posterior debe asignar fecha y horario efectivos y validar capacidad dentro de la transacción antes de sacar el regreso del estado OPEN_RETURN. La preferencia no implica disponibilidad garantizada. Esta entrega incorpora el contrato backend; no agrega controles visuales al formulario.
