# Vinculación explícita de cuentas y choferes

La migración V132 agrega `public.drivers.account_id`, su FK y unicidad,
y los índices de consultas operativas. V130 y V131 ya estaban ocupadas.
Las vinculaciones existentes quedan en NULL: no se infieren por teléfono o nombre.

Antes de aplicar la migración, verificar usernames duplicados sin distinguir mayúsculas:

```sql
SELECT UPPER(username), COUNT(*)
FROM public.accounts
GROUP BY UPPER(username)
HAVING COUNT(*) > 1;
```

Resolver esos duplicados antes de crear el índice único. La migración no elimina
ni fusiona cuentas automáticamente.

Asignar cada vínculo con IDs previamente verificados, por ejemplo con parámetros
UUID `:accountId` y `:driverId`:

```sql
UPDATE public.drivers
SET account_id = :accountId
WHERE id = :driverId AND account_id IS NULL;
```

Comprobar que se actualizó exactamente una fila. La cuenta debe tener rol CHOFER
(o ADMIN) y el chofer debe estar activo. No hay un formulario nuevo de vinculación.

Las cuentas sin vínculo activo reciben `403 / DRIVER_ACCESS_DENIED` al consultar
la hoja de ruta interurbana. Las sesiones anteriores contienen el principal viejo:
cerrar sesión e iniciar nuevamente para obtener `UserPrincipal` con `accountId`.
La modificación corresponde al adaptador interurbano; los flujos legados conservan
sus reglas actuales.
