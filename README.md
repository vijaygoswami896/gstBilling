---
title: Gst Billing API
emoji: ⚡
colorFrom: blue
colorTo: green
sdk: docker
app_port: 7860
pinned: false
---

# GST Billing Spring Boot API
This application is automatically built and deployed via GitHub Actions.

[//]: # (# GST Billing REST API)

A production-ready GST Billing backend built with Spring Boot 3, demonstrating JWT authentication, role-based data ownership, async messaging, distributed rate limiting, and Indian GST tax calculation logic — deployed live on managed cloud infrastructure.

> Built as a backend-only REST API — tested via Postman and Swagger UI. No frontend, no PDF generation, no payment integration.

---

## 🔗 Live Demo

**API Base URL:** `https://vijaygoswami896-gst-billing-api.hf.space`
**Swagger UI:** `https://vijaygoswami896-gst-billing-api.hf.space/swagger-ui/index.html`

> Note: the database is on Supabase's free tier, which may pause after inactivity — the first request after idle time can take a few extra seconds to wake up.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 + JWT (jjwt 0.12.6) |
| Database | PostgreSQL — hosted on Supabase |
| ORM | Spring Data JPA + Hibernate |
| Validation | Jakarta Bean Validation |
| Email | Spring Mail + Gmail SMTP |
| Messaging | RabbitMQ — hosted on CloudAMQP (Spring AMQP) — async email + DLQ retry handling |
| Caching / Rate Limiting | Redis — hosted on Upstash (Bucket4j, distributed across instances) |
| API Documentation | springdoc-openapi (Swagger UI) |
| Hosting | Koyeb (Buildpacks deployment) |
| Boilerplate | Lombok |
| Logging | SLF4J + Logback |
| Build | Maven |

---

## Features

### Auth System
- User registration with email verification (tokenised, 24hr expiry)
- JWT access tokens (15 min) + refresh tokens (7 days) with rotation
- Refresh tokens stored as SHA-256 hash — never raw value in DB
- Refresh token delivered via httpOnly cookie (XSS-safe)
- Max 2 active sessions per user — oldest revoked automatically
- **Distributed rate limiting** on auth routes: 5 requests per 15 minutes per IP, backed by Redis — consistent across multiple app instances, survives restarts

### Async Email via RabbitMQ
- Email verification is sent asynchronously — registration responds instantly, doesn't block on SMTP
- Retry policy: 3 attempts with 2-second backoff on transient failures
- Dead Letter Queue (DLQ) — permanently failed messages are preserved for inspection instead of being lost or retried forever
- JSON message conversion (not raw Java serialization) for cross-platform readability
- Runs against a managed CloudAMQP instance in production

### GST Billing
- Customer management (per-user, isolated)
- Product management with HSN/SAC codes and GST rates
- Invoice creation with automatic GST calculation
  - Intra-state: CGST + SGST (split equally)
  - Inter-state: IGST
- Auto-generated invoice numbers per user (INV-0001, INV-0002 ...)
- Invoice status lifecycle: `DRAFT → SENT → PAID → CANCELLED`
- Only `DRAFT` invoices can be deleted

### API Design
- Paginated list endpoints with search/filter support
- User data isolation — users can only access their own data
- Consistent error responses with field-level validation messages
- Layered architecture: Controller → Service → Repository
- **Interactive API documentation via Swagger UI** — explore and test every endpoint, including JWT-protected ones, directly from the browser

### Production-Ready Configuration
- Zero hardcoded secrets — all credentials (DB, JWT, mail, RabbitMQ, Redis) injected via environment variables
- Safe to make the repository public — `application.properties` contains only `${VAR_NAME}` references, never actual values

---

## Deployment Architecture

```
                         ┌──────────────────────────┐
        Client ─────────▶│  Hugging Face (hosting) 	│
                         │  Spring Boot App        	│
                         └────────┬─────────────────┘
                ┌─────────────────┼─────────────────┬──────────────┐
                ▼                 ▼                 ▼              ▼
        ┌───────────────┐ ┌──────────────┐ ┌───────────────┐ ┌──────────┐
        │   Supabase    │ │  CloudAMQP   │ │    Upstash    │ │  Gmail   │
        │ (PostgreSQL,  │ │ (RabbitMQ,   │ │   (Redis,     │ │  SMTP    │
        │  pooled conn) │ │  TLS, 5671)  │ │ TLS, rate     │ │          │
        │               │ │              │ │ limiting)     │ │          │
        └───────────────┘ └──────────────┘ └───────────────┘ └──────────┘
```

All four external services are managed/hosted — no self-managed infrastructure. Connections to Redis, RabbitMQ, and PostgreSQL all use TLS/SSL in production.

---

## Project Structure

```
src/main/java/com/vijay/gstbilling/
├── GstBillingApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── RabbitMQConfig.java
│   └── OpenApiConfig.java
├── entity/
│   ├── User.java
│   ├── RefreshToken.java
│   ├── EmailVerification.java
│   ├── Customer.java
│   ├── Product.java
│   ├── Invoice.java
│   └── InvoiceItem.java
├── dto/
│   ├── auth/
│   ├── customer/
│   ├── product/
│   ├── invoice/
│   └── email/
├── repository/
├── service/
├── controller/
├── listener/
│   └── EmailVerificationListener.java
├── middleware/
│   ├── JwtAuthFilter.java
│   └── RateLimitFilter.java       (Redis-backed, distributed)
└── exception/
    ├── GlobalExceptionHandler.java
    └── AppException.java (+ subclasses)
```

---

## Getting Started (Local Development)

### Prerequisites
- Java 17+
- Maven 3.8+
- Accounts on: [Supabase](https://supabase.com) (PostgreSQL), [Upstash](https://upstash.com) (Redis), [CloudAMQP](https://www.cloudamqp.com) (RabbitMQ) — all have free tiers
- Gmail account with App Password for email

> Alternatively for quick local-only testing, swap PostgreSQL for H2, and run RabbitMQ/Redis via Docker instead of cloud services — see "Local Docker Alternative" below.

### 1. Clone the repo
```bash
git clone https://github.com/vijaygoswami896/gstBilling.git
cd gst-billing
```

### 2. Set environment variables

This project reads **all** configuration from environment variables — nothing is hardcoded in `application.properties`. Set the following (in your IDE's run configuration, or your shell):

```bash
# Database (Supabase)
DB_HOST=aws-0-region.pooler.supabase.com
DB_PORT=6543
DB_NAME=postgres
DB_USERNAME=postgres.xxxxxxxxxxxx
DB_PASSWORD=your-supabase-password

# JWT
JWT_SECRET=your-256-bit-secret-key-minimum-32-characters
JWT_ACCESS_EXPIRY=900000
JWT_REFRESH_EXPIRY=604800000

# Mail (Gmail SMTP)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password

# RabbitMQ (CloudAMQP)
SPRING_RABBITMQ_HOST=your-host.cloudamqp.com
SPRING_RABBITMQ_PORT=5671
SPRING_RABBITMQ_USERNAME=your-cloudamqp-username
SPRING_RABBITMQ_PASSWORD=your-cloudamqp-password
SPRING_RABBITMQ_VIRTUAL_HOST=your-vhost
SPRING_RABBITMQ_SSL_ENABLED=true

# Redis (Upstash)
SPRING_DATA_REDIS_HOST=your-endpoint.upstash.io
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=your-upstash-password
SPRING_DATA_REDIS_SSL_ENABLED=true

# Base URL (used in email verification links)
APP_BASE_URL=http://localhost:8080
```

### 3. Run
```bash
mvn spring-boot:run
```
App starts at `http://localhost:8080`

---

## Local Docker Alternative

For fast local iteration without depending on internet-hosted services, you can run RabbitMQ and Redis locally via Docker, and use H2 in place of PostgreSQL:

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

Then set local-friendly environment variables (no SSL, default ports):
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.ssl.enabled=false

spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.ssl.enabled=false

spring.datasource.url=jdbc:h2:mem:gstdb
```
RabbitMQ management UI: `http://localhost:15672` (guest/guest)

---

## API Documentation (Swagger UI)

Interactive API documentation is available once the app is running:

```
http://localhost:8080/swagger-ui/index.html       (local)
https://vijaygoswami896-gst-billing-api.hf.space/swagger-ui/index.html   (live)
```

Raw OpenAPI JSON spec:
```
/v3/api-docs
```

**Testing protected endpoints in Swagger UI:**
1. Call `POST /api/auth/login` from the Swagger UI to get an `accessToken`
2. Click the **Authorize** button (top right of the Swagger UI page)
3. Paste the token as: `Bearer <your_access_token>`
4. All subsequent requests from Swagger UI will include the token automatically — protected endpoints (Customers, Products, Invoices) can now be tested directly in the browser

This is powered by `springdoc-openapi-starter-webmvc-ui`, which auto-generates the spec from your controllers, DTOs, and validation annotations — no manual YAML/JSON spec maintenance required.

---

## API Reference

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register, sends verification email (async via RabbitMQ) |
| GET | `/api/auth/verify-email?token=` | No | Verify email |
| POST | `/api/auth/login` | No | Login → access token + refresh cookie |
| POST | `/api/auth/refresh` | No | Rotate refresh token |
| POST | `/api/auth/logout` | Yes | Revoke refresh token |

### Customers
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/customers` | Yes | Create customer |
| GET | `/api/customers?search=&page=&size=` | Yes | List (paginated + search) |
| GET | `/api/customers/{id}` | Yes | Get one |
| PUT | `/api/customers/{id}` | Yes | Update |
| DELETE | `/api/customers/{id}` | Yes | Delete |

### Products
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/products` | Yes | Create product |
| GET | `/api/products?search=&page=&size=` | Yes | List (paginated + search) |
| GET | `/api/products/{id}` | Yes | Get one |
| PUT | `/api/products/{id}` | Yes | Update |
| DELETE | `/api/products/{id}` | Yes | Delete |

### Invoices
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/invoices` | Yes | Create with GST auto-calculation |
| GET | `/api/invoices?status=&page=&size=` | Yes | List (paginated + status filter) |
| GET | `/api/invoices/{id}` | Yes | Get with all items |
| PUT | `/api/invoices/{id}/status` | Yes | Update status only |
| DELETE | `/api/invoices/{id}` | Yes | Delete (DRAFT only) |

> Full interactive reference with request/response schemas available in Swagger UI.

---

## GST Calculation

```
For each item:
  lineTotal  = quantity × unitPrice
  gstAmount  = lineTotal × gstRate / 100

  Intra-state (isInterState = false):
    CGST = gstAmount / 2
    SGST = gstAmount - CGST   ← remainder to avoid rounding loss
    IGST = 0

  Inter-state (isInterState = true):
    IGST = gstAmount
    CGST = SGST = 0

Invoice totals:
  subtotal   = Σ lineTotals
  totalGst   = Σ gstAmounts
  grandTotal = subtotal + totalGst
```

All monetary values use `BigDecimal` with `RoundingMode.HALF_UP` — never `double` or `float`.

---

## Async Email Architecture

```
POST /api/auth/register
       ↓
AuthService.register()
   - saves User, EmailVerification
   - publishes message to RabbitMQ exchange (CloudAMQP)  ← returns instantly
       ↓
   201 response to client (doesn't wait for email)

       (meanwhile, in the background...)

[email.exchange] → [email.verification.queue]
       ↓
EmailVerificationListener (@RabbitListener)
       ↓
EmailService.sendVerificationEmail()  ← actual Gmail SMTP call

   On failure: retries 3× with 2s backoff
   On permanent failure: message routed to
   [email.dlx] → [email.verification.dlq] for inspection
```

This decouples registration from email delivery — the API responds quickly regardless of SMTP latency, and failed sends are never silently lost.

---

## Distributed Rate Limiting Architecture

```
Request to /api/auth/**
       ↓
RateLimitFilter
       ↓
Bucket4j ProxyManager (Jedis client)
       ↓
Upstash Redis  ← bucket state lives here, not in app memory
       ↓
Allowed (≤5 req / 15 min)  →  proceed
Blocked (>5 req / 15 min)  →  429 Too Many Requests
```

**Why this matters:** the original implementation stored rate-limit buckets in a local `ConcurrentHashMap` — state was lost on every restart and inconsistent if multiple app instances ran behind a load balancer (each instance would allow its own separate 5 requests). Moving the bucket state to Redis means rate limiting is correct and consistent regardless of how many instances are running or how often they restart.

---

## Security Design

| Concern | Implementation |
|---|---|
| Password storage | BCrypt hash |
| Access token | JWT, signed HS256, 15 min TTL |
| Refresh token | SHA-256 hash in DB, raw in httpOnly cookie |
| Email verification | SHA-256 hashed token, 24hr expiry, single use |
| Brute force protection | Bucket4j + Redis — distributed, 5 req/15min per IP on auth routes |
| Data isolation | Every query filters by authenticated user's ID |
| Session | Stateless — no server-side sessions |
| Failed message handling | RabbitMQ DLQ — no silent data loss on email failures |
| Secrets management | All credentials via environment variables — zero hardcoded values in source |
| Transport security | TLS/SSL enforced on DB, Redis, and RabbitMQ connections in production |

---

## Sample Requests

**Register**
```json
POST /api/auth/register
{
  "name": "Vijay Goswami",
  "email": "vijay@example.com",
  "password": "SecurePass123"
}
```

**Create Invoice**
```json
POST /api/invoices
Authorization: Bearer <access_token>
{
  "customerId": "uuid-here",
  "invoiceDate": "2026-06-19",
  "dueDate": "2026-07-19",
  "interState": false,
  "notes": "Q1 supply",
  "items": [
    {
      "productId": "uuid-here",
      "description": "Office Chair",
      "quantity": 2,
      "unitPrice": 5000.00,
      "gstRate": 18
    }
  ]
}
```

**Response**
```json
{
  "id": "uuid",
  "invoiceNumber": "INV-0001",
  "subtotal": 10000.00,
  "totalGst": 1800.00,
  "grandTotal": 11800.00,
  "status": "DRAFT",
  "items": [
    {
      "cgst": 900.00,
      "sgst": 900.00,
      "igst": 0.00,
      "lineTotal": 10000.00
    }
  ]
}
```

---

## Known Limitations / Next Steps

- **Invoice number generation** has a theoretical race condition under concurrent requests for the same user — would fix with a DB sequence or unique constraint + retry in a production setting
- **Database migrations** currently rely on Hibernate's `ddl-auto=update` — would move to Flyway or Liquibase for real production schema control
- **No distributed tracing yet** — with multiple services/instances, would add Zipkin or similar for request-level visibility
- **Supabase free tier cold starts** — acceptable for a demo project, would use a dedicated/always-on instance in real production

---

## Author

**Vijay Goswami**  
[GitHub](https://github.com/vijaygoswami896) · [LinkedIn](www.linkedin.com/in/vijay-goswami-vg896)
