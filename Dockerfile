FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-slim
WORKDIR /kamann

COPY --from=builder /app/target/kamann-0.0.1-SNAPSHOT.jar app.jar

COPY src/main/resources/keystore.p12 /kamann/keystore.p12

EXPOSE 8080 8443

ENTRYPOINT ["java", "-jar", "app.jar"]