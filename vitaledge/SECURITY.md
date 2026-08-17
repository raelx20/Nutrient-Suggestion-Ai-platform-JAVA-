# Security

VitalEdge follows defense-in-depth. This document describes the security model ported from the reference implementation.

## Authentication

- **JWT access + refresh tokens** with explicit token-type separation (`type` claim). A refresh token can never be used as an access token and vice versa.
- **Refresh token rotation**: every refresh issues a new refresh token and revokes the old one. Reuse of a revoked token is rejected.
- **Refresh token families**: a revocation cascades across the whole family (all sessions logged out together).
- **Refresh token hashing**: tokens are stored/compared as SHA-256 hashes, never in plaintext.
- **Access token blacklist**: on logout the access token is blacklisted in Redis for its remaining TTL.

## Email verification

- Registration returns a short-lived verification token; accounts must be verified before login.
- Passwords are hashed with **BCrypt** (`PasswordEncoder`).

## Rate limiting

Token-bucket / sliding-window limits enforced in Redis, per remote IP and per email where relevant:

| Operation | Limit |
|-----------|-------|
| Registration | 2 / IP / hour |
| Login | 10 / IP / hour, 5 / email / hour |
| Assessment create / complete | 5 / hour |
| Assessment answers | 30 / hour |
| Global (all endpoints) | 60 / IP / minute |

Security-sensitive limits (auth) **fail closed**; the global limiter fails open to avoid denial-of-service on the whole API.

## Secrets

- `SECRET_KEY` is required at startup (>= 32 chars) and drives JWT signing. Startup fails if missing/too short.
- No secrets are committed. Use the environment variables in `.env.example`.

## Transport & headers

- `SecurityHeadersFilter` sets browser-safety headers (X-Content-Type-Options, X-Frame-Options, Referrer-Policy, etc.).
- CORS is restricted to a configurable allow-list (`CORS_ORIGINS`), credentials enabled.

## Request hygiene

- `RequestIdFilter` adds a correlation id to every request for tracing.
- Role-based access control: `ADMIN` endpoints reject non-admin users with `403`.

## Reporting vulnerabilities

Do not open a public issue for security problems. Contact the maintainers directly (see the project owner).