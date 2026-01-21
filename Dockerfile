# ----------- BUILD IMAGE -----------
FROM eclipse-temurin:21-jre-alpine

# Create app directory
WORKDIR /app

# Copy jar
COPY target/ApiGateway-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run app
ENTRYPOINT ["java","-jar","app.jar"]

