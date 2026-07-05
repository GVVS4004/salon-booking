# Luxe Salon — Booking System

A full-stack booking system for a local salon.

- **Frontend:** React (Vite + TypeScript)
- **Backend:** Spring Boot (Java 21)
- **Database:** PostgreSQL (via Docker)

Customers browse services, pick a stylist, choose an open time slot, and book — signing
in with **Google** (frictionless, no account creation) or continuing as a **guest**.
Staff use an **admin panel** to manage services, stylists, working hours, and the schedule.

## Features

- Services with duration & price
- Multiple stylists, each offering a subset of services
- Weekly working hours per stylist; slots computed at a 15-minute interval
- **Double-booking impossible** — enforced by a PostgreSQL exclusion constraint
- Google Sign-In for customers (+ guest fallback)
- Email notifications (confirmation, cancellation, reminders) — console-logged in dev, real SMTP in prod
- Admin panel: schedule view, service/stylist CRUD, availability editor, mark complete / cancel

## Prerequisites

- **Java 21**
- **Node.js 20+**
- **Docker** (for PostgreSQL)
- **Maven** — not required; the backend ships with the Maven Wrapper (`mvnw`)

## Quick start

### 1. Start PostgreSQL

```bash
docker compose up -d
```

This runs Postgres on `localhost:5432` (db `salon`, user `salon`, password `salon`).

### 2. Start the backend

```bash
cd backend
cp .env.example .env          # then edit .env with your values (Windows: copy .env.example .env)
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`. On first run it applies the Flyway schema and
seeds demo data (4 services, 3 stylists, working hours).

Configuration is read from a local **`.env`** file (loaded automatically via `spring-dotenv`).
`.env` is git-ignored, so your secrets stay local. Copy `.env.example`, fill it in, and any
value you leave unset falls back to the defaults in `application.yml`. In production you can
skip `.env` entirely and use real environment variables — the same keys apply.

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The dev server proxies `/api` to the backend, so no CORS
setup is needed.

## Default logins

- **Admin panel:** go to `/admin/login` — email `admin@salon.local`, password `admin123`
  (override with `ADMIN_EMAIL` / `ADMIN_PASSWORD`).
- **Customers:** Google Sign-In (see below) or "continue as guest" at checkout.

## Configuration

All settings have sensible defaults (see `backend/src/main/resources/application.yml`) and
can be overridden via `backend/.env` (or real environment variables) using these keys:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | local Postgres | Database connection |
| `SALON_TIMEZONE` | `Asia/Kolkata` | Timezone business hours are interpreted in |
| `JWT_SECRET` | dev placeholder | **Change in production** (≥ 32 chars) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@salon.local` / `admin123` | Admin account |
| `GOOGLE_CLIENT_ID` | *(empty)* | Enables Google Sign-In |
| `NOTIFICATIONS_MODE` | `console` | `console` (log) or `email` (SMTP) |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | localhost:1025 | SMTP server |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed SPA origins |

### Enabling Google Sign-In

1. In [Google Cloud Console](https://console.cloud.google.com/apis/credentials), create an
   **OAuth 2.0 Client ID** (type: Web application).
2. Add `http://localhost:5173` to **Authorized JavaScript origins**.
3. Put the client id in `.env`: `GOOGLE_CLIENT_ID=<your-client-id>.apps.googleusercontent.com`, then restart the backend.

The frontend fetches the client id from `/api/auth/config` and renders the Google button
automatically. When it's not set, customers simply book as guests.

### Enabling real email

Set `NOTIFICATIONS_MODE=email` and the `MAIL_*` variables. For local testing you can run a
fake SMTP server such as [MailHog](https://github.com/mailhog/MailHog) on port 1025.

## API overview

Public:
- `GET /api/services` — active services
- `GET /api/staff?serviceId=` — stylists (optionally filtered by service)
- `GET /api/availability?staffId=&serviceId=&date=YYYY-MM-DD` — open slots
- `POST /api/bookings` — create a booking (guest or signed-in)
- `POST /api/auth/google` · `POST /api/auth/admin/login` · `GET /api/auth/config`

Customer (JWT):
- `GET /api/bookings/me` · `POST /api/bookings/{id}/cancel`

Admin (JWT, role ADMIN):
- `GET/POST/PUT/DELETE /api/admin/services` · `.../staff`
- `GET/PUT /api/admin/staff/{id}/availability`
- `GET /api/admin/appointments?from=&to=&staffId=`
- `POST /api/admin/appointments/{id}/status` · `.../cancel`

## Project structure

```
backend/    Spring Boot API (domain, repository, service, web, security, config)
frontend/   React SPA (api client, auth, pages: booking + admin)
docker-compose.yml   PostgreSQL
tasks.md    Plan & progress tracker
```

## Deployment

Split hosting (Vercel + Render + Neon) is fully prepared — production `Dockerfile`,
`render.yaml`, and `vercel.json` are in the repo, and config is env-driven. See
**[DEPLOYMENT.md](DEPLOYMENT.md)** for the step-by-step guide.

## Tests

```bash
cd backend && ./mvnw test
```
