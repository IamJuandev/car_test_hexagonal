# Car Management App

Per-user car management: register, log in, and manage your own cars.
Angular + Spring Boot (Java) + SQL Server, run locally with **Apple `container`** —
no Java, Maven, Node, Angular CLI, or SQL Server installed on the host.

See [`propuesta.md`](./propuesta.md) for the full technical plan and architecture.

## Requirements

- macOS on Apple Silicon
- [Apple `container`](https://github.com/apple/container) 1.0.0+ (`container --version`)

## Quick start

```bash
cp .env.local.example .env.local   # adjust secrets if you want
container system start
./scripts/container-up.sh
```

`container-up.sh` is idempotent and incremental: it brings up the full stack
(database, backend, frontend).

| Service | Local URL | Notes |
|---|---|---|
| Frontend (Angular) | http://localhost:4200 | served by nginx |
| Backend (Spring Boot) | http://localhost:8080 | REST API under `/api`; Swagger UI at `/swagger-ui/index.html` |
| SQL Server | `localhost:1433` | user `sa`, password from `.env.local` |

**Demo login** (from `database/seed.sql`): `demo@carapp.test` / `password123`.

Tear down (data is kept in the `sqlserver-data` volume):

```bash
./scripts/container-down.sh           # remove containers, keep data
./scripts/container-down.sh --purge   # also drop the volume and network
```

Tail logs:

```bash
./scripts/container-logs.sh db        # or: backend | frontend
```

## How the local environment works (Apple `container` notes)

Apple `container` is **not** Docker Compose. Each container runs in its own
lightweight Linux VM. Several quirks are handled by the scripts so you don't
have to think about them:

- **SQL Server is amd64-only** and runs via emulation (`--platform linux/amd64`).
- It needs **≥ 2 GB RAM** (`--memory 4g`) and runs **as root** (otherwise it
  can't create `/.system`).
- The server image **ships no `sqlcmd`**, and the legacy ODBC 13 client can't
  negotiate TLS with SQL Server 2022. We use a tiny **go-sqlcmd** image
  (`scripts/tools/Dockerfile`) to bootstrap the schema.
- Containers only network with **peers of the same architecture**, so the tools
  image is also amd64.
- Running `container build` **disrupts the network of running containers**, so
  the script builds every image *before* starting any networked container.
- There is **no name-based DNS** without an admin-created domain, so the script
  resolves the database IP dynamically and injects it into the backend.

## Database

`database/init.sql` creates the `CarApp` database with `users` and `cars`
(applied automatically by `container-up.sh`). `database/seed.sql` is applied
right after: it inserts the demo user `demo@carapp.test` / `password123`
(real BCrypt hash, verified against the login endpoint) and two sample cars.
Both scripts are idempotent.

## Tests

### Backend — unit tests (21, no DB)

Domain + use cases, in a Maven container with a cached dependency volume:

```bash
container volume create maven-repo   # once
container run --rm -v "$PWD/backend":/app -w /app -v maven-repo:/root/.m2 \
  maven:3.9-eclipse-temurin-21 mvn -B test
```

### Backend — integration test (full auth → CRUD flow)

Runs against the **live local SQL Server** (Apple `container` has no Docker API
for Testcontainers). The DB must be up (`container-up.sh`), and the test
container must join `car-app-net` and reach the amd64 DB, so it runs as amd64:

```bash
DB_IP=$(container ls | awk '/car-db/{print $6}' | cut -d/ -f1)
container run --rm --platform linux/amd64 --network car-app-net \
  -e DB_HOST="$DB_IP" -e DB_PASSWORD="<sa password>" \
  -v "$PWD/backend":/app -w /app -v maven-repo:/root/.m2 \
  maven:3.9-eclipse-temurin-21 mvn -B test -P it
```

The `it` profile flips the surefire group filter to run only `@Tag("integration")`
tests; plain `mvn test` keeps excluding them.

### Frontend — unit tests (8)

Build a small reusable test image once (Node 20 + headless Chromium baked in,
so repeat runs skip the slow apt download):

```bash
container build -t car-app-fe-test:dev -f scripts/tools/fe-test.Dockerfile scripts/tools
container run --rm -e HOME=/tmp -v "$PWD/frontend":/app -w /app \
  car-app-fe-test:dev sh -c 'npm install && npm test -- --watch=false'
```

(The Karma config uses a sandbox-less headless Chrome launcher for containers.)
