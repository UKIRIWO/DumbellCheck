# DumbellCheck

Social network for sports training (Instagram-like simplified model), built with Angular (frontend) and Spring Boot (backend).

## Stack

- Frontend: Angular (Standalone Components), Tailwind CSS, Signals, RxJS.
- Backend: Spring Boot, Security + JWT, JPA, MySQL, MapStruct.
- Database: MySQL (`dumbellcheck`).

## Project Structure

- `Frontend/`: Angular SPA with feature-first structure (`core`, `shared`, `features`, `layouts`).
- `Backend/`: Spring Boot API with package-by-layer clean architecture (`controllers`, `services`, `repositories`, `entities`, `dto`, `mapper`, `security`, `exceptions`, `config`).
- `Context/`: Product and architecture reference docs used as source of truth.

## Quick Start

### 1) Frontend

```bash
cd Frontend
npm install
npm start
```

App runs by default at `http://localhost:4200`.

### 2) Backend

Requirements:
- JDK 21+
- MySQL running locally

Create database:

```sql
CREATE DATABASE dumbellcheck;
```

Run API (PowerShell):

```powershell
cd Backend
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
.\mvnw.cmd spring-boot:run
```

API base URL: `http://localhost:8080/api`

## Environment Configuration

Backend values are loaded from `Backend/src/main/resources/application-dev.yml` with env variable overrides:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION_SECONDS`
- `CORS_ALLOWED_ORIGINS`
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`

Frontend API URL is configured in:
- `Frontend/src/environments/environment.ts` (dev)
- `Frontend/src/environments/environment.prod.ts` (prod)

## API Contract (Global)

All REST responses must use this wrapper:

Success:

```json
{
  "success": true,
  "data": {}
}
```

Error:

```json
{
  "success": false,
  "error": "message",
  "errorCode": "OPTIONAL_ERROR_CODE"
}
```

## Technical Rules Applied

- Controllers remain thin (`Controller -> Service -> Repository`).
- DTOs in backend are Java `record`.
- Entity/DTO mapping uses MapStruct.
- DB-mapped fields follow Spanish names from schema.
- Dates/timestamps are handled in UTC; frontend converts to local timezone.
- Counter fields managed by DB triggers are not manually updated in service logic.
- Snapshot behavior for publicaciones/rutinas is a required rule for feature implementation.

## Current MVP Scope

Included now:
- Full technical bootstrap for Angular and Spring Boot.
- Routing and feature skeletons on frontend.
- Security/JWT base, error handling, and sample endpoints on backend.

Deferred to next phase:
- Full auth flow (register/login with persisted passwords and refresh tokens).
- Feed/publicaciones business logic and snapshot writes.
- Likes/comments/notifications domain features.
- Real-time chat (WebSocket) and support incidencias workflow.
