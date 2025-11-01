# Step 1: Use an official Java image
FROM eclipse-temurin:17-jdk-alpine

# Step 2: Set working directory
WORKDIR /app

# Step 3: Copy the built jar (we’ll build it during deploy)
COPY target/*.jar app.jar

# Step 4: Expose port 8080
EXPOSE 8080

# Step 5: Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
