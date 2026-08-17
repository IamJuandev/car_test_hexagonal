# Progreso del proyecto

Estado de avance de la aplicación de gestión de autos por usuario
(Angular + Spring Boot + SQL Server, contenedores con Apple `container`).

Ver [`propuesta.md`](./propuesta.md) para el plan completo y la arquitectura.

## Resumen

La aplicación está **funcional de punta a punta**: el stack completo
(base de datos + backend + frontend) se levanta con un solo
`./scripts/container-up.sh`, sin instalar Java, Maven, Node, Angular CLI ni
SQL Server en la máquina.

| Capa | Estado | Verificación |
|---|---|---|
| Infra / contenedores | ✅ Completo | `down` + `up` reproducible |
| Base de datos (SQL Server) | ✅ Completo | schema aplicado, corriendo |
| Backend (Spring Boot) | ✅ Completo | 21 unit tests + test de integración (DB real) + smoke HTTP |
| Frontend (Angular) | ✅ Completo | build + 8 unit tests + sirve en `:4200` |
| Datos demo (seed) | ✅ Completo | login verificado contra la API |

## Slice 1 — Infraestructura y base de datos

- Esqueleto del repo: `backend/`, `frontend/`, `database/`, `scripts/`.
- `database/init.sql`: tablas `users` y `cars` (placa única por usuario, FK).
- Scripts idempotentes: `container-up.sh`, `container-down.sh`, `container-logs.sh`.
- `.env.local` para credenciales (gitignored), `.env.local.example` versionado.

**SQL Server en Apple Silicon — validado en vivo.** Trampas resueltas en el script:

| Problema | Solución |
|---|---|
| Imagen amd64 en host arm64 | `--platform linux/amd64` (emulación) |
| `requires 2000 MB memory` | `--memory 4g` |
| `/.system Permission denied` | `-u root` |
| Imagen sin `sqlcmd` + ODBC 13 no negocia TLS de SQL 2022 | imagen propia con `go-sqlcmd` (`scripts/tools/`) |
| `no route to host` entre arquitecturas distintas | tools también amd64 |
| `container build` rompe la red de contenedores vivos | buildear ANTES del runtime |
| Sin DNS por nombre | resolver IP dinámica e inyectarla al backend |

## Slice 2 — Backend Spring Boot (hexagonal)

Java 21, Spring Boot 3.3.6, JPA, Spring Security, JWT (jjwt), BCrypt.
Compilado y testeado dentro de contenedor Maven.

Módulos `shared` / `users` / `auth` / `cars`, cada uno con
`domain` / `application` (`port/in`, `port/out`, `service`) / `infrastructure`
(`adapter/in/web`, `adapter/out/...`).

- Dominio sin dependencias de frameworks; la regla de propiedad del auto
  (`Car.belongsTo`) se aplica EN el caso de uso.
- Acceso a auto ajeno → 404 (no filtra existencia). `ddl-auto: none`
  (el schema lo manda `init.sql`).

**Tests (21, verdes):** `CarTest` (6), `CarServiceTest` (9, ownership),
`RegisterUserServiceTest` (3), `LoginServiceTest` (3).

**Smoke test HTTP end-to-end (todo OK):** register 201 · login 200 ·
create 201 · list 200 · auto ajeno GET/DELETE → **404** · sin token → **401** ·
placa duplicada → **409** · año inválido → **400** · update 200 · delete 204.

## Slice 3 — Frontend Angular (hexagonal)

Angular 18.2 standalone, generado dentro de contenedor Node.
Misma arquitectura: `core/domain`, `core/application` (`ports`, `use-cases`),
`core/infrastructure` (`http`, `auth`), `features/*/presentation`,
`shared/presentation`.

- Puertos abstractos bindeados a adaptadores HTTP en `app.config.ts`.
- `jwtInterceptor` agrega el Bearer token; sesión en `localStorage`.
- `authGuard` protege las rutas privadas. Rutas lazy por componente.
- Servido con nginx en `:4200` (multi-stage Dockerfile, `try_files` para routing).

**Tests (8, verdes):** `LoginUseCase` (puerto mockeado) +
`CarHttpAdapter` (`HttpTestingController`) + `CarListComponent` (lógica de
filtro/búsqueda con signals), con Chrome headless.

Funcionalidades plus en `car-list`: búsqueda/filtro (marca/modelo/placa/color,
case-insensitive vía `computed` signal), thumbnail de foto con fallback `🚗`,
layout responsive (tabla → cards apiladas en `max-width:640px`).

Decisión: el frontend se construye **arm64 nativo** (build rápido); backend y
db son amd64. No chocan porque el browser le pega al backend vía host, no
container-to-container.

## Cómo correr

```bash
cp .env.local.example .env.local
container system start
./scripts/container-up.sh
```

- Frontend: http://localhost:4200
- Backend: http://localhost:8080
- SQL Server: `localhost:1433`

Tests: ver sección "Tests" en [`README.md`](./README.md).

## Slice 4 — Cierre (seed, integración, plus)

- **`database/seed.sql`** — usuario demo `demo@carapp.test` / `password123`
  (hash BCrypt real, `$2b$`, aceptado por `BCryptPasswordEncoder`) + 2 autos.
  Idempotente. Login verificado contra `POST /api/auth/login` (200 + token;
  contraseña incorrecta → 401).
- **Test de integración** `AuthCarFlowIntegrationTest` (`@Tag("integration")`).
  Apunta a la **SQL Server real** local (Apple `container` no expone la API de
  Docker que requiere Testcontainers). Flujo completo verde: register · login ·
  401 sin token · create · list · get · ownership 404 · placa duplicada 409 ·
  año inválido 400 · update · delete.
  - `mvn test` corre solo los 21 unit tests (`<excludedGroups>integration</excludedGroups>`).
  - `mvn test -P it` corre el de integración (perfil `it` con `combine.self="override"`).
- **Funcionalidades plus** del frontend: ver Slice 3.

## Pendientes

- [ ] (Opcional) e2e del flujo de UI en browser con un driver (Playwright/Cypress).
      Alta fricción en entorno solo-contenedores; el test de integración ya
      cubre el flujo de API extremo a extremo.
