# Render: presupuesto inicial para 512 MiB

Activar el perfil `prod` en Render con `SPRING_PROFILES_ACTIVE=prod`.
Este perfil desactiva la validación de esquema de Hibernate y el SQL por consola,
reduce Hikari a 3 conexiones (sin conexiones ociosas mínimas) y limita Tomcat a
20 workers, 2 workers mínimos, 100 conexiones y 50 conexiones en espera.
Esto reduce la concurrencia máxima: revisar latencia y espera de conexiones bajo carga.
Flyway sigue ejecutando migraciones y validando sus checksums. Esa validación no
reemplaza la comparación entre entidades JPA y esquema: mantener esa comprobación
en integración/CI o en un entorno con mayor memoria.

## Variables en Render

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_JPA_HIBERNATE_DDL_AUTO=none
SPRING_JPA_SHOW_SQL=false
JAVA_TOOL_OPTIONS=-Xms64m -Xmx224m -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=32m -XX:MaxDirectMemorySize=16m -Xss512k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError
```

Configurar estos valores en Environment del servicio, sin comillas envolventes.
Las variables explícitas de JPA evitan conservar overrides antiguos como `validate`
o `show-sql=true`. El Dockerfile ejecuta `java -jar app.jar`; la JVM reconoce
`JAVA_TOOL_OPTIONS` directamente. `JAVA_OPTS` no se interpola en ese ENTRYPOINT.
No aplicar estos límites a Maven ni a la etapa de compilación.

## Alcance de los límites

Es un punto de partida para medir, no una garantía de RSS menor a 512 MiB.
Los máximos de heap (224), metaspace (160), code cache (32) y buffers directos (16)
suman 432 MiB. Quedan unos 80 MiB para stacks, estructuras de la JVM, bibliotecas,
memoria nativa y otros gastos del contenedor. No todos esos gastos tienen un
límite conjunto en la JVM. `-Xms64m` reduce el heap inicial; no es un límite total.
Serial GC usa menos estructuras e hilos que colectores paralelos, a cambio de
pausas que deben evaluarse bajo carga.

Revisar el pico de memoria del arranque y el uso sostenido en Render después del
despliegue. Si aparece `OutOfMemoryError: Metaspace`, `Java heap space`, fallo de
buffers directos o `StackOverflowError`, el presupuesto necesita ajustarse; no
seguir bajando límites indiscriminadamente. Si el conjunto no entra con margen
en 512 MiB, aumentar la memoria del servicio.

Para diagnóstico temporal agregar `-XX:NativeMemoryTracking=summary` y
`-Xlog:gc+heap=info,gc+metaspace=info` a JAVA_TOOL_OPTIONS. NMT agrega sobrecarga;
el contenedor actual es JRE y no incluye necesariamente `jcmd`, por lo que un
desglose NMT en vivo requiere una imagen diagnóstica con JDK. Desactivar el
diagnóstico al terminar. El RSS/métricas del contenedor son la referencia final.

No se activa inicialización lazy global: puede trasladar el pico a la primera
petición sin reducir el coste permanente de JPA. Tampoco se desactiva el acceso
a metadatos JDBC ni la validación de consultas de los repositorios.

## Referencias

- [Opciones de Java 21](https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html)
- [JAVA_TOOL_OPTIONS](https://docs.oracle.com/en/java/javase/17/troubleshoot/environment-variables-and-system-properties.html)
- [Propiedades de Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/appendix/application-properties/)
- [Validación de Flyway](https://documentation.red-gate.com/flyway/reference/commands/validate)
