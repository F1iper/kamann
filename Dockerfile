FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:resolve-plugins dependency:resolve go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-slim
WORKDIR /kamann

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]