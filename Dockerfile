# ----------- BUILD IMAGE -----------
FROM eclipse-temurin:21-jre-alpine

# Create app directory
WORKDIR /app

# Copy jar
COPY target/ApiGateway-0.0.1-SNAPSHOT.jar app.jar
COPY opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar


# Expose port
EXPOSE 8080

ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_SERVICE_NAME=apigateway
ENV OTEL_RESOURCE_ATTRIBUTES="service.name=apigateway,env=docker"
ENV OTEL_INSTRUMENTATION_JVM_METRICS_ENABLED=true



# Run app
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar","-jar","app.jar"]

