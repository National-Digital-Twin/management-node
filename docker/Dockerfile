# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-alpine

# Create non-root user and group
RUN addgroup -S app && adduser -S -G app -u 10001 app

WORKDIR /app

# Create directories and set permissions
RUN mkdir -p /app/docker /app/logs /app/tmp && chown -R app:app /app

# Copy application jar from build stage
COPY --from=build /build/target/management-node-0.0.1.jar /app/app.jar
RUN chown app:app /app/app.jar

# Use non-root user from here on
USER app:app

# Expose HTTPS port
EXPOSE 8443

# Helpful defaults for Java in containers
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -XX:MaxRAMPercentage=75.0 -Djava.io.tmpdir=/app/tmp"

# Use sh -c so JAVA_OPTS is expanded
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]