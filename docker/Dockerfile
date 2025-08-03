# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Create directory for certificates
RUN mkdir -p /app/docker

# Copy application jar from build stage and certificates
COPY --from=build /build/target/management-node-0.0.1.jar /app/app.jar
COPY docker/keystore.jks /app/docker/keystore.jks
COPY docker/truststore.jks /app/docker/truststore.jks

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]