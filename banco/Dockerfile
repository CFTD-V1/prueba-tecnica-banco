# =============================================================================
# Imagen de la aplicacion, construida en dos etapas.
#
# La primera etapa compila el proyecto con Maven y el JDK completo. La segunda
# parte de una imagen que solo trae el entorno de ejecucion de Java y recibe
# unicamente el jar ya construido, de modo que la imagen final no arrastra ni el
# codigo fuente, ni Maven, ni el compilador.
# =============================================================================

# --- Etapa 1: compilacion ----------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# El pom se copia solo primero: mientras las dependencias no cambien, Docker
# reutiliza esta capa y no vuelve a descargarlas en cada construccion.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

# Las pruebas ya se ejecutan en el ciclo de desarrollo con "./mvnw test"; aqui se
# omiten para que la construccion de la imagen sea rapida y reproducible.
RUN mvn -B clean package -DskipTests

# --- Etapa 2: ejecucion ------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

# La aplicacion no necesita privilegios de administrador: se crea un usuario
# propio para no ejecutar el proceso como root dentro del contenedor.
RUN addgroup -S banco && adduser -S banco -G banco

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar
RUN chown banco:banco app.jar

USER banco

EXPOSE 8080

# Docker consulta un endpoint real de la API para saber si el contenedor esta
# listo. Responder 200 implica que la aplicacion arranco y que la conexion con
# la base de datos funciona.
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget --quiet --spider http://localhost:8080/api/clientes || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
