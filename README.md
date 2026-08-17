# VitalEdge API

Production-grade AI nutrition and wellness recommendation platform (Spring Boot 4.1, Java 21).

This is an independent Java re-implementation of a reference Python/FastAPI codebase. It shares **no** source code, Git history, or identifiers with the original: it uses new names (`vitaledge`), its own schema, and its own project structure.

## Stack

- **Spring Boot 4.1** / **Java 21**, Maven wrapper
- **Spring Security** + JWT (jjwt 0.12.x) — access/refresh token separation, refresh rotation, logout blacklist
- **Spring Data JPA** + **Flyway** (PostgreSQL for production)
- **Spring Data Redis** (Lettuce) — rate limiting, token blacklist, product cache
- **Spring Validation**, **Spring Actuator**
- Tests: JUnit 5, MockMvc, H2 (in-memory), Testcontainers for Postgres

## Quick start (Docker)

```bash
cp .env.example .env
# set a strong SECRET_KEY (>= 32 chars), e.g.:
#   SECRET_KEY=$(openssl rand -base64 48)
docker compose up --build
```

The API listens on `http://localhost:8080`. Health check: `http://localhost:8080/actuator/health`.

## Local development (without Docker)

Requirements: JDK 21, Maven (or use the bundled wrapper `mvnw.cmd`/`mvnw`), PostgreSQL, Redis.

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/vitaledge
export DB_USER=postgres
export DB_PASSWORD=your-password
export REDIS_URL=redis://localhost:6379/0
export SECRET_KEY=$(openssl rand -base64 48)

./mvnw spring-boot:run
```

Flyway applies `V1` (schema) and `V2` (seed data) migrations on startup (`ddl-auto: validate`).

## Tests

Unit and API flow tests run against an in-memory H2 database with an in-memory Redis substitute and require **no external services**:

```bash
./mvnw test
```

The Postgres integration test is tagged `integration` and runs via Failsafe only with a Docker daemon present (it uses Testcontainers); it is skipped automatically otherwise:

```bash
./mvnw clean verify        # integration test runs if Docker is available, else skips
```

> Note: the in-memory H2 test profile maps JSON columns to `text` (`TestH2Dialect`) because H2's native `json` type round-trips JSON as a double-encoded string. Production PostgreSQL uses real `jsonb` via the Flyway schema.

## API overview

All endpoints are under `/api/v1`.

| Area | Endpoints |
|------|-----------|
| Auth | `POST /auth/register`, `POST /auth/verify-email`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /auth/me` |
| Assessments | `POST /assessments`, `POST /assessments/start`, `POST /assessments/{id}/answers`, `POST /assessments/{id}/complete`, `GET /assessments/{id}` |
| Products | `GET /products`, `GET /products/{id}`, `GET /products/search`, `GET /products/recommendations` |
| Admin | `POST /admin/products`, ... (requires `ADMIN` role) |

See `src/main/java/com/vitaledge/web/controller` for the full set.

## Configuration

Configuration is externalized via environment variables. See [`.env.example`](.env.example) and `src/main/resources/application.yml` for the complete list, including rate limits and JWT TTLs.

See [`REQUIREMENTS.md`](REQUIREMENTS.md) for the feature/behavior requirements and [`SECURITY.md`](SECURITY.md) for the security model.