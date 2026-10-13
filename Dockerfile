# Build stage using official Maven image on Temurin JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source files
COPY pom.xml .
COPY src ./src

# Build production JAR (skipping unit tests for fast, deterministic deployment)
RUN mvn clean package -DskipTests -B

# Runtime stage using lightweight Eclipse Temurin JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy packaged JAR from build stage
COPY --from=build /app/target/SkillSwap-0.0.1-SNAPSHOT.jar app.jar

# Render dynamic port binding
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]