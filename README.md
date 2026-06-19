# GST Billing REST API

A production-ready GST Billing backend built with Spring Boot 3, demonstrating JWT authentication, role-based data ownership, and Indian GST tax calculation logic.

> Built as a backend-only REST API — tested via Postman. No frontend, no PDF generation, no payment integration.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 + JWT (jjwt 0.12.6) |
| Database | PostgreSQL (prod) / H2 (dev) |
| ORM | Spring Data JPA + Hibernate |
| Validation | Jakarta Bean Validation |
| Email | Spring Mail + Gmail SMTP |
| Rate Limiting | Bucket4j |
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
- Rate limiting on auth routes: 5 requests per 15 minutes per IP

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

---

## Project Structure

```text
src/main/java/com/vijay/gstbilling/
├── GstBillingApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── JwtConfig.java
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
│   └── invoice/
├── repository/
├── service/
├── controller/
├── middleware/
│   ├── JwtAuthFilter.java
│   └── RateLimitFilter.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── AppException.java (+ subclasses)
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (or use H2 for dev — no setup needed)
- Gmail account with App Password for email

### 1. Clone the repo
```bash
git clone https://github.com
cd gst-billing
```

### 2. Configure `application.properties`
```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/gstbilling
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwt.secret=your-256-bit-secret-key-here-minimum-32-characters
app.jwt.access-token-expiry=900000
app.jwt.refresh-token-expiry=604800000

# Gmail SMTP
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password

# Base URL
app.base-url=http://localhost:8080
```

> For dev with H2, use `jdbc:h2:mem:gstdb` as the datasource URL and skip PostgreSQL setup.

### 3. Run
```bash
mvn spring-boot:run
```
App starts at `http://localhost:8080`
H2 console (dev only): `http://localhost:8080/h2-console`

---

## API Reference

### Auth

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register, sends verification email |
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

---

## GST Calculation
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

All monetary values use `BigDecimal` with `RoundingMode.HALF_UP` — never `double` or `float`.
---

## Security Design

| Concern | Implementation |
|---|---|
| Password storage | BCrypt hash |
| Access token | JWT, signed HS256, 15 min TTL |
| Refresh token | SHA-256 hash in DB, raw in httpOnly cookie |
| Email verification | SHA-256 hashed token, 24hr expiry, single use |
| Brute force protection | Bucket4j rate limit — 5 req/15min per IP on auth routes |
| Data isolation | Every query filters by authenticated user's ID |
| Session | Stateless — no server-side sessions |

---

## Sample Requests

**Register**
```json
POST /api/auth/register
{
  "name": "Test User",
  "email": "test@example.com",
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

## Author

**Vijay Goswami**  
[GitHub](https://github.com/vijaygoswami896) · [LinkedIn](www.linkedin.com/in/vijay-goswami-vg896)
