# ==========================================
# Stage 1: Build
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B && \
    # Rename jar to fixed name for easier COPY \
    mv target/*.jar target/app.jar

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="fmmcb@seplag.mt.gov.br" \
      version="1.0.0" \
      description="SEPLAG Artists and Albums API" \
      org.opencontainers.image.source="https://github.com/francisbenemayer621664" \
      org.opencontainers.image.vendor="SEPLAG-MT"

WORKDIR /app

# Install dependencies (curl for healthcheck)
RUN apk add --no-cache curl tzdata && \
    # Set timezone \
    cp /usr/share/zoneinfo/America/Cuiaba /etc/localtime && \
    echo "America/Cuiaba" > /etc/timezone && \
    apk del tzdata && \
    # Create non-root user \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup && \
    # Create logs directory \
    mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Copy jar from build stage
COPY --from=build --chown=appuser:appgroup /app/target/app.jar app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

# JVM optimization for containers
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs \
    -Djava.security.egd=file:/dev/./urandom"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]