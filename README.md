# E-Commerce Backend Engine

A production-quality REST API built with Java and Spring Boot, designed to mirror the backend architecture of large-scale e-commerce platforms. Built as a learning project targeting Amazon SDE roles.

## Architecture Overview

```
Client → Spring Security (JWT Filter) → Controllers → Services → Repositories → PostgreSQL
                                                              ↕
                                                           Redis Cache
```

## Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| Language | Java 21 | Amazon's primary backend language |
| Framework | Spring Boot 3.3 | Industry standard for Java microservices |
| Database | PostgreSQL (AWS RDS-ready) | ACID-compliant relational store |
| Cache | Redis (AWS ElastiCache-ready) | In-memory cache for high-read endpoints |
| Auth | JWT (JJWT 0.12) | Stateless authentication - scales horizontally |
| ORM | Spring Data JPA / Hibernate | Object-relational mapping with optimistic locking |
| Security | Spring Security 6 | Filter chain, BCrypt password hashing |

## Features

### Phase 1 - Core API + Authentication
- Full product catalog CRUD (create, read, update, delete)
- User registration with BCrypt password hashing
- Stateless JWT authentication - tokens signed with HS256
- Custom Spring Security filter chain - `JwtAuthFilter` validates every request before it reaches a controller
- DTO boundary - entities never leak into API responses
- Input validation on all endpoints using Jakarta Bean Validation — 
  malformed requests are rejected at the controller layer with field-level 
  error messages before touching the database

### Phase 2 - Cart, Orders, and Concurrency
- Shopping cart with per-user item management
- Atomic order placement using `@Transactional` - stock decrement and order creation happen as one indivisible operation
- **Overselling prevention** using JPA optimistic locking (`@Version`) - if two concurrent requests try to buy the last item, only one succeeds; the other receives a `409 Conflict` with a clear error message
- Price snapshot at time of purchase - order history is accurate even if product prices change later

### Phase 3 - Caching
- Cache-aside pattern using Redis and Spring's `@Cacheable` / `@CacheEvict`
- Product catalog reads served from Redis after first request - database never touched on repeat reads
- Automatic cache invalidation on any write operation (create, update, delete)
- 10-minute TTL on all cache entries
- Configured for AWS ElastiCache drop-in replacement

## Key Technical Decisions

**Why optimistic locking over pessimistic locking?**

Pessimistic locking (`SELECT FOR UPDATE`) holds a database lock for the duration of a transaction, which kills throughput under high concurrency. Optimistic locking uses a `version` column - reads are free, and conflicts are detected only at write time. For an e-commerce platform where reads vastly outnumber purchases, optimistic locking is the correct choice.

**Why JWT over session-based auth?**

Session-based auth requires server-side state - every server in a cluster needs access to the same session store. JWT is stateless: the token itself carries the proof of identity, cryptographically signed. Any server can verify it without a database call. This is essential for horizontal scaling.

**Why cache the product catalog specifically?**

Product data changes infrequently but is read on every page load. Without caching, a traffic spike hits the database with thousands of identical queries per second. Redis serves repeated reads from memory - orders of magnitude faster - while `@CacheEvict` ensures data is never stale after a write.

**Why Argon2id over BCrypt?**

Argon2id won the Password Hashing Competition in 2015 and is the current 
OWASP recommendation. Unlike BCrypt which is CPU-bound only, Argon2id is 
both memory-hard and CPU-hard, making it significantly more resistant to 
GPU and ASIC brute-force attacks. The memory cost parameter (64MB) means 
an attacker needs substantial RAM per attempt, not just raw compute speed.

**Why idempotency keys on orders?**

Network failures during checkout are common — a user's request succeeds 
server-side but the response never reaches the client. Without idempotency, 
retrying creates duplicate orders and double-charges. The client sends a 
unique Idempotency-Key header; the server stores the response in Redis for 
24 hours and returns it on duplicate requests without re-executing the 
business logic. This is how Amazon's own payment systems handle retries 
at scale.

## API Endpoints

### Auth
```
POST /api/auth/register    - register a new user, returns JWT
POST /api/auth/login       - authenticate, returns JWT
```

### Products (public reads, auth required for writes)
```
GET    /api/products        - list all products (cached)
GET    /api/products/{id}   - get one product (cached)
POST   /api/products        - create product
PUT    /api/products/{id}   - update product (invalidates cache)
DELETE /api/products/{id}   - delete product (invalidates cache)
```

### Cart (authentication required)
```
GET    /api/cart                      - view current cart
POST   /api/cart/items?productId=&quantity=  - add item to cart
DELETE /api/cart/items/{cartItemId}   - remove item from cart
```

### Orders (authentication required)
```
POST /api/orders    - place order from cart (atomic stock decrement)
GET  /api/orders    - view order history
```

## Running Locally

**Prerequisites:** Java 21, PostgreSQL, Redis

```bash
# Clone the repo
git clone https://github.com/yourusername/ecommerce-catalog.git
cd ecommerce-catalog

# Set up PostgreSQL
psql -U postgres
CREATE DATABASE catalogdb;
CREATE USER cataloguser WITH PASSWORD 'catalogpass';
GRANT ALL PRIVILEGES ON DATABASE catalogdb TO cataloguser;
\q

# Start Redis
redis-server --daemonize yes

# Configure application.yml
# Update datasource.url with your PostgreSQL host
# Update data.redis.host with your Redis host

# Run
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. Tables are created automatically on first run.

## Testing the Overselling Protection

To see the concurrency protection in action:

1. Create a product with `stockQuantity: 1`
2. Add it to your cart with `quantity: 1`
3. Place the order - succeeds, stock becomes 0
4. Try to place another order for the same item - returns `409 Conflict: Not enough stock`

Under real concurrent load, JPA's `@Version` column ensures that even if two transactions read `version = 5` simultaneously, only the first write succeeds. The second transaction finds `version` has changed and throws `OptimisticLockException`, which is caught and returned as a clean error response.

## Project Roadmap

- [x] Phase 1 - REST API + JWT authentication
- [x] Phase 2 - Cart, orders, atomic transactions
- [x] Phase 3 - Redis caching with cache-aside pattern
- [x] Phase 4 - Async order notifications via AWS SQS + Lambda
