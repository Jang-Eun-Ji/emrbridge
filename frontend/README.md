# EMR Bridge frontend

Angular console for the analytics star schema. The browser talks to the Spring API on port 8080. It does not connect to Postgres.

## Run

Node `22.22.3` or newer is required. From this directory:

```bash
nvm use
npm start
```

Open http://localhost:4200. `/api` is proxied to http://localhost:8080.

## Screens

- `/dashboard` — clinical statistics
- `/patients` and `/patients/:id` — patient list and detail
- `/conditions` — condition / disease analysis
- `/pipeline` — EMR → FHIR → Analytics processing status

Read endpoints the screens call:

- `GET /api/analytics/dashboard`
- `GET /api/analytics/patients`
- `GET /api/analytics/patients/{id}`
- `GET /api/analytics/conditions`
- `GET /api/analytics/pipeline`

## Docker

From the repository root:

```bash
docker compose up --build frontend
```

The container is served at http://localhost:8082 and proxies `/api` to the backend on the host at port 8080.
