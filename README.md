# API Gateway
The **API Gateway** is the **single public entry point** of the platform.

It acts as a smart edge service that:
* Secures APIs using **JWT authentication**
* Routes requests to internal microservices
* Performs **load balancing**
* Applies **rate limiting** to protect downstream services
* Hides internal services from public access

All external traffic **must** go through the gateway.

---

## Responsibilities
The API Gateway is responsible for:
* **Authentication & Authorization**
* **Load Balancing** across service instances
* **Rate Limiting** to prevent abuse
* **Request routing** to internal services
* **Security boundary** between public and private services

Internal services (Order, Payment, etc.) are **never exposed publicly**.

---

## Security -> JWT Authentication
### Overview
The gateway enforces **JWT-based security** for all incoming requests.

* Clients must provide a valid JWT token
* Tokens are validated at the gateway level
* Invalid or missing tokens are rejected immediately
* Downstream services trust the gateway and do not expose public auth

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
http
GET /orders/123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
If the token is:
* Missing -> 401 Unauthorized
* Invalid / expired -> 401 Unauthorized
* Valid -> Request is forwarded

### JWT Configuration (example)
```
yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://auth-server:8080

```
The gateway acts as a **resource server**, validating JWT tokens before routing.

---

## Load Balancing
### Overview
The API Gateway uses **Spring Cloud LoadBalancer** together with **Eureka Discovery**.
* Services are addressed by logical name
* Multiple instances are automatically load-balanced
* Failed instances are removed from routing
* No hardcoded IPs or ports
### Example Route Configuration
```
yaml
spring:
  cloud:
    gateway:
      routes:
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/payments/**
```
lb:// tells the gateway to:
* Discover instances via Eureka
* Distribute traffic across them
* Automatically handle failover
### Load Balancing Behavior
With two instances running:
```
bash
docker compose up --scale payment-service=2
```
Requests are routed like:
```
nginx
Request 1 -> payment-service-1
Request 2 -> payment-service-2
Request 3 -> payment-service-1
```
If one instance crashes, traffic is automatically redirected.

---

## Rate Limiting (Redis-based)
### Overview
Rate limiting is implemented using Redis to protect services from:
* Abuse
* Traffic spikes
* Accidental overload

Limits are applied before requests reach backend services.
### Why Redis?
* Distributed and fast
* Works across multiple gateway instances
* Production-grade solution

### Example Rate Limit Configuration
```
yaml
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
* 10 requests per second
* Burst up to 20 requests
### When limit is exceeded
```
http
HTTP/1.1 429 Too Many Requests
```

---

## Service Isolation & Security Boundary
The API Gateway is the only publicly exposed service.

Service	---------------------- Publicly Accessible <br>
API Gateway ------------------ Yes <br>
User Service ----------------- No <br>
Order Service ---------------- No <br>
Payment Service -------------- No <br>
Config Server ---------------- No <br>
Eureka Server ---------------- No <br>

Internal services:
* Are only accessible via Docker network
* Accept requests only from the gateway
* Are protected from direct access

---

## Observability

The gateway integrates with:

* **OpenTelemetry**
* **Prometheus** (metrics)
* **Loki** (Logs)
* **Tempo** (distributed tracing)
* **Grafana** (dashboards)
* **Kafka metrics & traces**

This allows:

* Request tracing from gateway -> service
* Per-route latency monitoring
* Error rate analysis
* Load balancing visibility

---

## Startup Order
The API Gateway depends on:
* Config Server
* Eureka Discovery
* Redis (for rate limiting)

Once running, all client traffic flows through the gateway.

---

## Why this design?
This gateway follows industry best practices:

* Zero-trust boundary
* Centralized security
* Horizontal scalability
* Built-in resilience
* Clean separation of concerns

This is the same pattern used in:
* Cloud-native systems
* Kubernetes ingress layers
* Large-scale microservice platforms

---

## Summary

+ Single public entry point
+ JWT-based security
+ Redis-backed rate limiting
+ Dynamic load balancing
+ Failover-ready routing
+ Fully observable




