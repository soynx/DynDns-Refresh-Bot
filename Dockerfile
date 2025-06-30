# === STAGE 1: Build with Maven and JDK 23 ===
FROM maven:3.9-eclipse-temurin-23 AS builder

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the project (no tests)
RUN mvn clean package -DskipTests

# === STAGE 2: Runtime with only JDK 23 ===
FROM eclipse-temurin:23-jdk

WORKDIR /app

# Copy the JAR from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Set entrypoint
ENV JAVA_OPTS=""
ENTRYPOINT java $JAVA_OPTS -jar app.jar
