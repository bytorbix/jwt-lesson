# jwt-lesson

A REST API built with Spring Boot, Spring Security and JWT. Users register and log in with a username/password, receive a signed JWT, and use it to access endpoints protected by role (`ROLE_USER`, `ROLE_ADMIN`).

## Stack

- Java 21+
- Spring Boot 4.1.1 (Web, Security, Data JPA, Validation)
- PostgreSQL 16
- JJWT 0.13.0
- Lombok
- Maven

## Architecture

```
Controller  -> HTTP in, DTOs out
Service     -> login / register / user lookup logic
Security    -> JwtAuthFilter + SecurityConfig decide who's allowed to call what
Repository  -> Spring Data JPA
Entity      -> User <-> Role (many-to-many)
```

- `JwtAuthFilter` runs once per request, verifies the `Authorization: Bearer <token>` header, and reloads the user from the database on every request (so a disabled user or a role change takes effect immediately, not just on next login).
- `SecurityConfig` is stateless (no sessions) and defines the three security zones below, plus custom 401/403 handling.
- Entities never leave the service layer as JSON — controllers only return DTOs (`UserResponse`, `LoginResponse`), so the password hash and the back-reference side of the many-to-many relationship are never serialized.

## Security zones

| Path | Access |
|---|---|
| `/api/auth/**`, `/api/public/**` | Public |
| `/api/user/**` | `ROLE_USER` |
| `/api/admin/**` | `ROLE_ADMIN` |

## Running locally

### 1. Database

```bash
docker run --name pg-lesson -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=jwt_lesson -p 5432:5432 -d postgres:16
```

### 2. Application

```bash
mvn spring-boot:run
```

Or run `JwtLessonApplication` directly from your IDE.

On first start, `DataInitializer` seeds two accounts:

| Username | Password | Roles |
|---|---|---|
| `admin` | `Admin123!` | `ROLE_ADMIN`, `ROLE_USER` |
| `user` | `User123!` | `ROLE_USER` |

Override any of this via environment variables (`DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_PASSWORD`, `USER_PASSWORD`) — see `src/main/resources/application.properties`.

### 3. Try it

```bash
curl http://localhost:8080/api/public/hello

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}'

curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <token from the login response>"
```

## Endpoints

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Returns a JWT |
| POST | `/api/auth/register` | Public | Creates a user with `ROLE_USER` only |
| GET | `/api/public/hello` | Public | Health check |
| GET | `/api/user/me` | `ROLE_USER` | Current user's profile |
| GET | `/api/admin/users` | `ROLE_ADMIN` | List all users |

## Tests

`http/security-tests.http` is a 19-case suite covering role enforcement, JWT signature verification (including a forged-token attack), login error messages, and registration validation (including a mass-assignment attempt on `roles`). Run it from IntelliJ's HTTP Client, selecting the `dev` environment from `http/http-client.env.json`.
