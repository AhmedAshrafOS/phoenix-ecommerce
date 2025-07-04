# Stage 1: Build the application using Maven and JDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Stage 2: Run the application in a lightweight JDK 17 image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /opt/app
COPY --from=builder /build/target/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
CMD []
