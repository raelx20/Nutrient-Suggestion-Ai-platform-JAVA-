# Requirements

Functional and non-functional requirements implemented by VitalEdge, mirrored from the reference Python/FastAPI application.

## Functional requirements

### Authentication (`/api/v1/auth`)
- **FR-A1 Register** — `POST /auth/register`: normalize email, hash password (BCrypt), assign `CONSUMER` role, rate-limit per IP (2/h), reject duplicate email with `409`.
- **FR-A2 Verify email** — `POST /auth/verify-email`: consume a signed verification token to mark the account verified.
- **FR-A3 Login** — `POST /auth/login`: authenticate (per-IP 10/h, per-email 5/h), reject unverified/locked accounts, return access + refresh tokens.
- **FR-A4 Refresh** — `POST /auth/refresh`: rotate the refresh token (issue new, revoke old), reject reuse, cascade revocation across the token family.
- **FR-A5 Logout** — `POST /auth/logout`: blacklist the access token in Redis and revoke the refresh-token family.
- **FR-A6 Me** — `GET /auth/me`: return the current user profile.

### Assessments (`/api/v1/assessments`)
- **FR-AS1 Create** — `POST /assessments`: start a new assessment (rate-limited 5/h).
- **FR-AS2 Fetch questions** — `POST /assessments/start`: return the active questionnaire (question options, validation rules, scoring).
- **FR-AS3 Submit answers** — `POST /assessments/{id}/answers`: persist structured answers (rate-limited 30/h).
- **FR-AS4 Complete** — `POST /assessments/{id}/complete`: finalize the assessment and compute a score (rate-limited 5/h).
- **FR-AS5 Retrieve** — `GET /assessments/{id}`: fetch an assessment with its answers and score (owner-only).

### Products (`/api/v1/products`)
- **FR-P1 List** — `GET /products`: paginated, active products, filterable by category.
- **FR-P2 Detail** — `GET /products/{id}`: single product with allergens, dietary tags, nutritional info.
- **FR-P3 Search** — `GET /products/search`: keyword + category + age-group search (query length capped).
- **FR-P4 Recommendations** — `GET /products/recommendations`: products matched to an assessment's recommended categories.

### Admin (`/api/v1/admin`)
- **FR-AD1** — product/rule management endpoints guarded by the `ADMIN` role (403 otherwise).

## Non-functional requirements

- **NFR-1 Isolation** — the implementation must be fully independent: new naming, no shared Git history, no derived source files.
- **NFR-2 Build** — `mvnw test` must pass without external services (H2 + in-memory Redis).
- **NFR-3 Migration** — production schema managed by Flyway (`validate` on startup), real `jsonb` for JSON columns.
- **NFR-4 Security** — rate limiting, token rotation/hashing, blacklisting, BCrypt, role-based access, security headers.
- **NFR-5 Observability** — Actuator exposes health/info/metrics; request-id correlation.
- **NFR-6 Portability** — 12-factor config via environment variables; Docker Compose orchestration.

## Rate-limit matrix (enforced in Redis)

| Key | Per | Window | Limit |
|-----|-----|--------|-------|
| `register` | IP | hour | 2 |
| `login:ip` | IP | hour | 10 |
| `login:email` | email | hour | 5 |
| `assessment:create` | user | hour | 5 |
| `assessment:answer` | user | hour | 30 |
| `assessment:complete` | user | hour | 5 |
| `global` | IP | minute | 60 |