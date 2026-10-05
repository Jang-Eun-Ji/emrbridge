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

- `/dashboard` — 의료 데이터 통계
- `/patients` and `/patients/:id` — 환자 목록과 상세
- `/conditions` — 질병/의료상태 분석
- `/pipeline` — EMR → FHIR → Analytics 처리 상태

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
