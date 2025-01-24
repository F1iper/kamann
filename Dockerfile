FROM openjdk:17-jdk-slim
WORKDIR /kamann
COPY target/*.jar kamann.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "kamann.jar"]