# Activación de endpoints interurbanos

El archivo base del proyecto es `src/main/resources/application.yaml`.
Tanto ese archivo como `application-prod.yml` declaran:

```yaml
lunaris:
  interurban:
    enabled: ${LUNARIS_INTERURBAN_ENABLED:false}
```

En Render, configurar `LUNARIS_INTERURBAN_ENABLED=true` y desplegar la versión
que contiene este cambio. El valor por defecto es `false`.

Los servicios, controlador, manejo de excepciones y seguridad de chofer se
activan juntos mediante `lunaris.interurban.enabled`. Ya no requieren el flag
adicional `lunaris.interurban.driver.enabled`.

Rutas exactas, sin prefijo `/interurban` ni context-path configurado:

| Método | Ruta | Activación |
| --- | --- | --- |
| GET | `/api/v1/driver/route-sheet?date=YYYY-MM-DD` | Flag general |
| POST | `/api/v1/checkin/verify` | Flag general |
| POST | `/webhook/interurban/mercadopago` | Flag general y `lunaris.interurban.payments.enabled=true` |

Las rutas de chofer mantienen autenticación, roles CHOFER/ADMIN y CSRF para
check-in. El webhook requiere además las credenciales de pagos documentadas en
`etapa-2-pagos-qr.md`. La liquidación conserva su flag independiente.
