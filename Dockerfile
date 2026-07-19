# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy dependency manifest first for Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

EXPOSE 8080
EXPOSE 8081

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/covoitdark-1.0.0.jar app.jar

# Copy the frontend assets
COPY web_ui /web_ui

ENTRYPOINT ["java", "-jar", "app.jar"]
