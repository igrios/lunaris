# Login de choferes

`POST /api/choferes/login`, con `Content-Type: application/json`:

```json
{"usuario":"hector","password":"cantorlunaris123"}
```

Las credenciales son fijas y se comparan exactamente, respetando mayúsculas y
espacios. No se consulta la base de datos.

Devuelve `200` con `{"usuario":"hector","nombre":"Hector","rol":"CHOFER"}`
y una cookie de sesión `JSESSIONID`. Credenciales distintas, vacías o ausentes
devuelven `401` con `{"message":"Credenciales inválidas."}`.

El frontend debe usar `credentials: "include"` en el login y al consultar
`GET /api/choferes/agenda?fecha=2026-11-07`. CORS utiliza los orígenes permitidos
en `SecurityConfig`, incluyendo localhost y los dominios del frontend.

La agenda exige rol `CHOFER`, `ADMIN` u `OPERADOR` y conserva el listado existente
sin filtrar por chofer.

## Consulta sin cookies

La agenda admite la identificación fija `hector` mediante:

```http
GET /api/choferes/agenda?fecha=2026-11-07&usuario=hector
```

O mediante `X-Chofer-Usuario: hector`, con la misma ruta y fecha.
La coincidencia es exacta; si se envían ambos campos, ambos deben ser `hector`.
Identificaciones desconocidas o vacías devuelven `401`. Sin identificación
explícita se conserva el acceso con sesión/HTTP Basic y los roles permitidos.

Esta modalidad permite leer la agenda con solo conocer `hector`; no verifica
contraseña ni consulta el estado de un registro en la base de datos. Usa la
identidad fija habilitada en `ChoferAuthService`. Los datos y el filtrado por
fecha conservan el comportamiento existente, sin filtro adicional por chofer.

CORS permite `X-Chofer-Usuario` mediante la configuración global de cabeceras.
Los orígenes Vercel y dominios personalizados existentes están permitidos.

`GET /api/viajes` es un alias de `/api/choferes/agenda`: admite los mismos
parámetros `fecha`, `usuario` y la cabecera `X-Chofer-Usuario`, con igual JSON.
Los GET de `/api/viajes` y `/api/viajes/**` pasan la seguridad sin sesión;
el controlador exige identificación válida o autenticación autorizada.
No se agregan endpoints funcionales bajo las subrutas de `/api/viajes/**`.
