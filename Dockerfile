# Paso 1: Compilar la aplicación usando Maven con Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Paso 2: Ejecutar la aplicación con una imagen ligera de Java 21 (Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el jar compilado desde la primera etapa
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto 10000 que es el requerido y configurado para Render
EXPOSE 10000

# Configurar límites de memoria y opciones optimizadas para la JVM en contenedores pequeños
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx224m -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=32m -XX:MaxDirectMemorySize=16m -Xss512k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"

# Comando de inicio de la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
