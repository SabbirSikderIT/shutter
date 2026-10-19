# ==========================================
# Stage 1: Build the application
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy the Maven wrapper and pom.xml first (for better caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Fix the permission issue for mvnw (Git on Windows doesn't preserve execute permissions)
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the source code and build the jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Run the application
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Copy the built jar file from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port (Render automatically detects this)
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]