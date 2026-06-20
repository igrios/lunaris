# Paso 1: Compilar la aplicación con Maven
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Paso 2: Ejecutar la aplicación con una imagen ligera de Java
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto que usa Render por defecto
EXPOSE 8080

# Comando para arrancar el backend de Lunaris
ENTRYPOINT ["java", "-jar", "app.jar"]
