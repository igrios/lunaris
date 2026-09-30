# Diagnóstico de HTTP Basic

La aplicación usa `BCryptPasswordEncoder` directo, no `DelegatingPasswordEncoder`.
`accounts.password_hash` debe contener un hash BCrypt completo (`$2a$`, `$2b$` o
`$2y$`), sin el prefijo `{bcrypt}`. No guardar la contraseña en texto plano.

Para verificar un hash real sin imprimirlo ni ponerlo en argumentos de procesos:

```bash
bash scripts/verify-password.sh
```

Ingresar el hash de `accounts.password_hash` y la contraseña cuando se soliciten.
El test optativo usa el mismo encoder que la aplicación. Un resultado exitoso
confirma la coincidencia; no modifica la BD ni comprueba el estado o los roles.
La suite habitual no necesita esos secretos y omite este test.

Revisar en producción los logs de `AccountUserDetailsService` y
`AuthenticationDiagnostics` (niveles WARN/ERROR, sin habilitar DEBUG global):

| Diagnóstico | Significado |
| --- | --- |
| `USER_NOT_FOUND` | La búsqueda por username no encontró cuenta. |
| `BadCredentialsException` | Contraseña incorrecta o hash incompatible; Spring oculta también usuarios inexistentes. |
| `DisabledException` | La cuenta tiene `active=false`. |
| `BCRYPT_PREFIX_UNSUPPORTED` | El hash contiene `{bcrypt}`, incompatible con el encoder directo. |
| `INVALID_BCRYPT_FORMAT` | El valor almacenado no tiene la estructura esperada de BCrypt. |
| `ACCOUNT_LOAD_FAILED` | Falló la consulta o el mapeo de cuenta/roles; se registra el tipo de excepción raíz. |

No se registran contraseñas, hashes, cabeceras Authorization ni mensajes de
excepciones de persistencia. Los clientes siguen recibiendo errores genéricos.
Spring sigue ocultando la diferencia entre usuario inexistente y contraseña
incorrecta al cliente.

HTTP Basic se habilita en las cadenas API e interurbana. Las vistas web usan
formulario de login. Una autenticación correcta sin rol suficiente produce 403;
un chofer sin vínculo activo produce `DRIVER_ACCESS_DENIED`, también con 403.

No se ha accedido a la base de producción: el test automático de `juan123` usa
un hash generado como fixture y no confirma la contraseña almacenada allí.
