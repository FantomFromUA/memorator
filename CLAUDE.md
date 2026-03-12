# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the application
mvn spring-boot:run

# Build
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=UserServiceTest

# Run a single test method
mvn test -Dtest=UserServiceTest#methodName
```

**Prerequisites:** Java 21, MySQL on `localhost:3306`. Create the database manually before first run:
```sql
CREATE DATABASE memorator;
```

Credentials and JWT secrets are configured in `src/main/resources/application.properties`. Flyway runs migrations automatically on startup.

## Architecture

Layered Spring Boot REST API:

```
controller → service → repository → entity → MySQL
```

- **controller** — REST endpoints. `UserController` handles auth (`/api/users/**`). `HelloController` is a protected test endpoint.
- **service** — Business logic. `UserService` handles registration/login/logout. `RefreshTokenService` manages refresh token lifecycle.
- **security** — `JwtService` issues and validates JWTs using JJWT (HMAC-SHA). `JwtAuthenticationFilter` extracts the Bearer token, validates it, and sets a `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`.
- **config** — `SecurityConfig` defines public vs. protected routes, CORS (allows `localhost:3000`), stateless session, and wires the JWT filter before `UsernamePasswordAuthenticationFilter`.
- **exception** — `GlobalExceptionHandler` (`@RestControllerAdvice`) maps custom exceptions to HTTP status codes (401, 409, 400).
- **entity** — JPA entities: `User`, `RefreshToken`, `Language`, `Word`, `UserWord`.
- **dto** — Request/response objects with Jakarta validation annotations.
- **repository** — Spring Data JPA interfaces, no custom query implementations needed yet.

## Auth Flow

1. `POST /api/users/register` or `POST /api/users/login` → returns `{ accessToken, refreshToken }`
2. Access token (15 min) sent as `Authorization: Bearer <token>` on protected requests.
3. `POST /api/users/refresh` with `{ refreshToken }` → new access token.
4. `POST /api/users/logout` with `{ refreshToken }` → revokes the refresh token.

`/api/users/**` is public. All other routes require a valid JWT.

## Database Migrations

Flyway migrations live in `src/main/resources/db/migration/` — this is the source of truth for the database schema. Always check these SQL files for authoritative table/column definitions rather than inferring from JPA entities. Current schema (V1–V7):

| Table | Purpose |
|---|---|
| `users` | User credentials |
| `refresh_tokens` | Tokens with expiry and revocation flag |
| `languages` | Available learning languages |
| `words` | Words per language |
| `word_translations` | N-N word translation mappings |
| `user_words` | User's learning progress (correct/wrong counts) |

Always add new migrations as `V{n}__description.sql`. Never modify existing migration files.
