# Etapa de build
FROM maven:3-jdk-8-alpine AS builder
WORKDIR /usr/src/app

# Copia apenas pom.xml para baixar dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código e faz o build
COPY src ./src
RUN mvn package -DskipTests

# Etapa de runtime
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=builder /usr/src/app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]