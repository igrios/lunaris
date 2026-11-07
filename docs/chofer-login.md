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
