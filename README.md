# API Gateway — Security, Routing & Traffic Control (v3)

> Cloud-native edge service responsible for request routing, JWT validation, Redis-based rate limiting, load balancing, and protecting internal microservices from direct public access.

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud-Gateway-blue)
![Redis](https://img.shields.io/badge/Redis-Rate%20Limiting-red)
![Security](https://img.shields.io/badge/Security-JWT-success)
![Architecture](https://img.shields.io/badge/Architecture-Microservices-blue)

---

# Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture Role](#architecture-role)
- [Request Flow](#request-flow)
- [Security Model](#security-model)
- [Rate Limiting with Redis](#rate-limiting-with-redis)
- [Load Balancing & Service Discovery](#load-balancing--service-discovery)
- [Gateway Routing](#gatewa-routing)
- [Service Isolation](#service-isolation)
- [Observability](#observability)
- [Configuration](#configuration)
- [Run Locally](#run-locally)
- [Run with Docker](#run-with-docker)
- [Version History](#version-history)
- [Author](#author)

---

# Overview

The **API Gateway** is the **single public entry point** of the platform.

All external traffic must pass through the gateway before reaching internal services.

It provides:

- centralized **authentication and authorization**
- **request routing** to microservices
- **rate limiting** to protect services
- **load balancing** across service instances
- **security boundary** between public clients and internal services

The gateway simplifies the client experience while enforcing platform-wide policies.

---

# Key Features

- Single public entry point
- JWT authentication and validation
- Redis-backed rate limiting
- Dynamic routing to internal services
- Service discovery integration
- Load balancing with Spring Cloud
- Protection of internal services
- Observability integration with OpenTelemetry stack

---

# Architecture Role

The gateway sits at the **edge of the system**.

```

Client
↓
API Gateway
├─ User Service
├─ Order Service
└─ Payment Service

```

It acts as a **traffic controller** and **security boundary**.

Responsibilities include:

- validating authentication tokens
- enforcing rate limits
- forwarding requests
- balancing traffic across instances
- protecting internal services from direct access

---

# Request Flow

Typical request lifecycle:

```

Client
↓ HTTP Request
API Gateway
├─ JWT validation
├─ Redis rate limit check
├─ Service discovery lookup
└─ Request routing
↓
Internal Microservice

```

This ensures that **all requests are validated and controlled before reaching backend services**.

---

# Security Model

The gateway enforces **JWT-based authentication**.

### Authentication Flow

```

Client
↓ (JWT Token)
API Gateway
↓ (validated request)
Internal Services

```

### Example Request

```

GET /orders/123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

````

Gateway behavior:

| Condition | Result |
|---|---|
Missing token | `401 Unauthorized` |
Invalid token | `401 Unauthorized` |
Valid token | Request forwarded |

### JWT Configuration Example

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://auth-server:8080
````

The gateway acts as a **resource server**, validating tokens before forwarding requests.

---

# Rate Limiting with Redis

Rate limiting protects backend services from:

* abusive clients
* traffic spikes
* brute-force login attempts
* accidental overload

The gateway uses **Redis as a distributed rate limit store**.

### Why Redis

* extremely fast
* shared across gateway instances
* distributed rate limits
* production-grade solution

### Example Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/orders/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

This configuration allows:

* **10 requests per second**
* **burst capacity of 20 requests**

### When Rate Limit Is Exceeded

```
HTTP/1.1 429 Too Many Requests
```

This protects the entire platform from overload.

---

# Load Balancing & Service Discovery

The gateway integrates with:

* **Spring Cloud LoadBalancer**
* **Eureka Discovery Server**

Services are addressed by **logical name** instead of fixed IP addresses.

Example:

```
uri: lb://payment-service
```

This allows the gateway to:

* discover service instances dynamically
* distribute traffic automatically
* remove failed instances from routing

### Example

Running multiple instances:

```
docker compose up --scale payment-service=2
```

Requests are distributed automatically:

```
Request 1 → payment-service-1
Request 2 → payment-service-2
Request 3 → payment-service-1
```

---

# Gateway Routing

The gateway routes traffic to backend services based on **route predicates**.

Example configuration:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/payments/**
```

This means:

```
/payments/** → payment-service
```

Routing remains dynamic and service instances can scale horizontally.

---

# Service Isolation

The gateway acts as a **security boundary**.

Only the gateway is exposed publicly.

| Service         | Public Access |
| --------------- | ------------- |
| API Gateway     | Yes           |
| User Service    | No            |
| Order Service   | No            |
| Payment Service | No            |
| Config Server   | No            |
| Eureka Server   | No            |

Internal services:

* run inside the Docker network
* accept requests only from the gateway
* remain protected from direct internet access

---

# Observability

The gateway integrates with the platform observability stack:

* OpenTelemetry
* Prometheus (metrics)
* Loki (logs)
* Tempo (tracing)
* Grafana (dashboards)

This enables:

* request tracing from gateway to services
* route latency monitoring
* error rate analysis
* load balancing visibility

---

# Configuration

The gateway retrieves configuration from the **Config Server**.

Externalized configuration includes:

* routing rules
* JWT settings
* rate limit configuration
* Redis connection
* service discovery settings

---

# Run Locally

Prerequisites:

* Java 17+
* Maven
* Redis running
* Config Server running
* Discovery Server running

Start the service:

```
mvn clean spring-boot:run
```

---

# Run with Docker

Build the image:

```
docker build -t api-gateway:3.0 .
```

Run container:

```
docker run -p 8080:8080 api-gateway:3.0
```

---

# Testing Strategy

The gateway includes multiple test layers:

### Unit Tests

Validate gateway configuration logic.

### Slice Tests

Validate routing and filter behavior.

### Integration Tests

Validate:

* JWT validation
* Redis rate limiting
* routing to downstream services

Test dependencies include:

* spring-security-test
* MockMvc
* jackson-databind

---

# Version History

## v3

* Redis-based rate limiting added
* JWT validation improvements
* routing architecture refactored
* observability integration improved
* updated testing strategy

## v2

* Added Principe of Clean Code

## v1

* initial gateway foundation
* gateway routing implementation
* JWT authentication layer

---

## Author

**Bah Youne**
Founder & Backend / Full Stack Java Developer

* GitHub: [http://github.com/bahyoune]
* LinkedIn: [http://linkedin.com/in/younoussa-bah]

