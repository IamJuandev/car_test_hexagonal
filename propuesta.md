# Propuesta técnica: Gestión de autos por usuario

Esta propuesta define un plan para construir una aplicación web donde cada usuario pueda registrarse, iniciar sesión y administrar sus autos. La implementación usará **Angular**, **Spring Boot con Java** y **SQL Server**, ejecutados con contenedores para evitar instalar dependencias directamente en la Mac.

## Decisión principal

| Área | Decisión |
|---|---|
| Frontend | Angular |
| Backend | Spring Boot + Java |
| Seguridad | JWT con Spring Security |
| Base de datos | SQL Server |
| Persistencia | JPA/Hibernate |
| Arquitectura | Hexagonal / Ports and Adapters |
| Entorno local | Apple `container` CLI |
| Orquestación local | Scripts del proyecto, no Docker Compose |
| Entrega | Repositorio con frontend, backend, scripts SQL y documentación |

> Nota: `idea_inicial.md` menciona React, pero esta propuesta lo reemplaza por **Angular** porque es la tecnología requerida para este proyecto.

## Alcance funcional

### Autenticación

- Registro de usuario.
- Inicio de sesión.
- Generación de token JWT.
- Protección de endpoints privados.
- Asociación de autos al usuario autenticado.

### Gestión de autos

El usuario autenticado podrá:

- Crear un auto.
- Listar sus autos.
- Editar un auto propio.
- Eliminar un auto propio.

Campos del auto:

- Marca.
- Modelo.
- Año.
- Número de placa.
- Color.

### Funcionalidades opcionales

- Búsqueda por placa o modelo.
- Filtro por año o marca.
- Campo simulado para foto del auto.
- Diseño responsive.

## Arquitectura propuesta

```text
Angular App
    |
    | HTTP + JWT
    v
Spring Boot REST API
    |
    | JPA/Hibernate
    v
SQL Server
```

La aplicación se organizará con **arquitectura hexagonal**. La regla central es que el dominio y los casos de uso no dependen de Angular, Spring Boot, SQL Server, JWT ni ningún framework.

```text
Presentation / Frameworks
        |
        v
Application / Use Cases
        |
        v
Domain
        ^
        |
Infrastructure / Adapters
```

### Regla de dependencia

| Capa | Responsabilidad | Puede depender de |
|---|---|---|
| Dominio | Modelos, reglas de negocio y errores del negocio | Nada externo |
| Aplicación | Casos de uso y puertos | Dominio |
| Infraestructura | Base de datos, JWT, hashing, HTTP clients | Aplicación y dominio |
| Presentación | Controllers REST o componentes Angular | Casos de uso / servicios de aplicación |

La separación es importante porque evita que la lógica principal quede atrapada en controllers, entidades JPA o componentes Angular. Eso es arquitectura de verdad, no decoración de carpetas.

## Hexagonal en el backend Spring Boot

El backend tendrá módulos por capacidad funcional:

- `auth`: registro, login, generación de JWT.
- `users`: usuario autenticado.
- `cars`: gestión de autos.
- `shared`: configuración común, seguridad y errores transversales.

Estructura sugerida por módulo:

```text
backend/src/main/java/com/example/cars/
├── auth/
│   ├── domain/
│   ├── application/
│   │   ├── port/in/
│   │   ├── port/out/
│   │   └── service/
│   └── infrastructure/
│       ├── adapter/in/web/
│       ├── adapter/out/security/
│       └── adapter/out/persistence/
├── cars/
│   ├── domain/
│   ├── application/
│   │   ├── port/in/
│   │   ├── port/out/
│   │   └── service/
│   └── infrastructure/
│       ├── adapter/in/web/
│       └── adapter/out/persistence/
└── shared/
    └── infrastructure/
```

Ejemplo aplicado a autos:

| Pieza | Ejemplo |
|---|---|
| Dominio | `Car`, `CarId`, reglas de propiedad del auto |
| Puerto de entrada | `CreateCarUseCase`, `ListCarsUseCase`, `UpdateCarUseCase`, `DeleteCarUseCase` |
| Puerto de salida | `CarRepositoryPort` |
| Servicio de aplicación | `CreateCarService`, `ListCarsService` |
| Adaptador REST | `CarController` |
| Adaptador JPA | `CarPersistenceAdapter`, `CarJpaRepository`, `CarJpaEntity` |

## Hexagonal en el frontend Angular

En Angular se aplicará el mismo criterio: separar presentación, casos de uso, dominio e infraestructura HTTP.

```text
frontend/src/app/
├── core/
│   ├── domain/
│   ├── application/
│   │   ├── ports/
│   │   └── use-cases/
│   └── infrastructure/
│       ├── http/
│       └── auth/
├── features/
│   ├── auth/
│   │   └── presentation/
│   └── cars/
│       └── presentation/
└── shared/
    └── presentation/
```

Los componentes Angular no deberían contener reglas de negocio. Su trabajo será capturar eventos, mostrar estado y delegar en servicios/casos de uso.

## Estrategia con contenedores

La idea es no instalar Node, Angular CLI, Java, Maven ni SQL Server directamente en la Mac.

Se usará **Apple `container`** como runtime local. Esta herramienta trabaja con imágenes OCI y puede construir imágenes desde `Dockerfile` o `Containerfile`, pero no debe asumirse como un reemplazo directo de Docker Compose.

> **SQL Server en Apple Silicon (arm64) — verificado end-to-end.** La imagen oficial `mcr.microsoft.com/mssql/server:2022` solo publica binarios `amd64`; corre por emulación con `--platform linux/amd64`. Para que arranque hace falta además `--memory 4g` (requiere ≥2 GB) y `-u root` (si no, no puede crear `/.system`). La imagen no trae `sqlcmd` y el cliente ODBC 13 legacy no negocia el TLS de SQL Server 2022, así que el schema se aplica con una imagen chica de **go-sqlcmd** (`scripts/tools/`). Dos detalles de Apple `container` 1.0.0 que el script maneja: (1) `container build` perturba la red de los contenedores corriendo, así que se buildea ANTES de levantar runtime; (2) no hay DNS por nombre sin dominio admin, así que el script resuelve la IP de `car-db` dinámicamente y se la inyecta al backend. Todo esto está validado y automatizado en `scripts/container-up.sh`. No se requiere infraestructura remota (Dokploy u otra): la base de datos corre 100% local.

Se propone usar:

- Scripts del proyecto para levantar y apagar el entorno.
- Un contenedor para SQL Server.
- Un contenedor para el backend Spring Boot.
- Un contenedor para el frontend Angular.
- Volúmenes de `container` para persistir datos de SQL Server.
- Redes de `container` para comunicar frontend, backend y base de datos.
- Variables de entorno para credenciales y configuración.

Comandos esperados:

```bash
container system start
./scripts/container-up.sh
```

Comando de apagado:

```bash
./scripts/container-down.sh
```

Ejemplo de flujo interno de los scripts:

```bash
container network create car-app-net
container volume create sqlserver-data

container build -t car-app-backend:dev ./backend
container build -t car-app-frontend:dev ./frontend

container run -d --name car-db \
  --platform linux/amd64 \
  --cpus 2 --memory 4g \
  -u root \
  --network car-app-net \
  --env-file .env.local \
  -p 1433:1433 \
  -v sqlserver-data:/var/opt/mssql \
  mcr.microsoft.com/mssql/server:2022-latest

container run -d --name car-backend \
  --network car-app-net \
  --env-file .env.local \
  -p 8080:8080 \
  car-app-backend:dev

container run -d --name car-frontend \
  --network car-app-net \
  -p 4200:4200 \
  car-app-frontend:dev
```

Servicios esperados:

| Servicio | Puerto local | Descripción |
|---|---:|---|
| Frontend Angular | `4200` | Aplicación web |
| Backend Spring Boot | `8080` | API REST |
| SQL Server | `1433` | Base de datos |

## Estructura sugerida del repositorio

```text
.
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── database/
│   ├── init.sql
│   └── seed.sql
├── scripts/
│   ├── container-up.sh
│   ├── container-down.sh
│   └── container-logs.sh
├── idea_inicial.md
└── propuesta.md
```

## Modelo de datos inicial

### `users`

| Campo | Tipo sugerido | Nota |
|---|---|---|
| `id` | `BIGINT` | Primary key |
| `name` | `VARCHAR(120)` | Nombre del usuario |
| `email` | `VARCHAR(180)` | Único |
| `password_hash` | `VARCHAR(255)` | Contraseña encriptada |
| `created_at` | `DATETIME2` | Fecha de creación |

### `cars`

| Campo | Tipo sugerido | Nota |
|---|---|---|
| `id` | `BIGINT` | Primary key |
| `user_id` | `BIGINT` | Foreign key a `users` |
| `brand` | `VARCHAR(80)` | Marca |
| `model` | `VARCHAR(80)` | Modelo |
| `year` | `INT` | Año |
| `plate_number` | `VARCHAR(20)` | Placa |
| `color` | `VARCHAR(50)` | Color |
| `photo_url` | `VARCHAR(255)` | Opcional/simulado |
| `created_at` | `DATETIME2` | Fecha de creación |
| `updated_at` | `DATETIME2` | Última actualización |

## API REST propuesta

### Autenticación

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/auth/register` | Registrar usuario |
| `POST` | `/api/auth/login` | Iniciar sesión y obtener JWT |

### Autos

Todos los endpoints requieren JWT.

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/cars` | Listar autos del usuario autenticado |
| `POST` | `/api/cars` | Crear auto |
| `GET` | `/api/cars/{id}` | Ver detalle de un auto propio |
| `PUT` | `/api/cars/{id}` | Editar auto propio |
| `DELETE` | `/api/cars/{id}` | Eliminar auto propio |

Regla importante: un usuario no debe poder ver, editar ni eliminar autos de otro usuario.

## Pantallas Angular

### Públicas

- Login.
- Registro.

### Privadas

- Listado de autos.
- Formulario para crear auto.
- Formulario para editar auto.
- Acción para eliminar auto.

## Estrategia de testing

La arquitectura hexagonal existe, entre otras razones, para poder probar el dominio y los casos de uso sin levantar base de datos, JWT ni HTTP. La estrategia aprovecha esa separación: lo que tiene reglas de negocio se prueba aislado y rápido; lo que es adaptador se prueba contra su tecnología real.

### Backend (JUnit 5 + Mockito + Spring Boot Test)

| Nivel | Qué prueba | Dependencias | Ejemplo |
|---|---|---|---|
| Dominio | Reglas de negocio puras | Ninguna (POJOs) | Un `Car` no acepta año inválido; un usuario solo es dueño de sus autos |
| Aplicación / casos de uso | Orquestación de puertos | Puertos de salida *mockeados* | `CreateCarService` asocia el auto al usuario autenticado; `UpdateCarService` rechaza editar auto ajeno |
| Adaptador de persistencia | Mapeo JPA y queries | SQL Server real vía contenedor (Testcontainers) | `CarPersistenceAdapter` guarda y recupera correctamente |
| Adaptador web | Serialización, status codes, seguridad | `@WebMvcTest` / `MockMvc` | `POST /api/cars` sin token → `401`; con token → `201` |
| Integración / e2e | Flujo completo | Aplicación arriba + DB en contenedor | Registro → login → crear → listar → editar → eliminar |

Regla de propiedad del auto (un usuario no toca autos de otro): se prueba en el **caso de uso** (mockeando el puerto), no solo en el controller. Ahí vive la regla, ahí se valida.

### Frontend (Jasmine/Karma o Jest)

| Nivel | Qué prueba | Ejemplo |
|---|---|---|
| Casos de uso / servicios de aplicación | Lógica de la capa application | El caso de uso de login guarda el token vía el puerto correspondiente |
| Adaptadores HTTP | Llamadas a la API con `HttpTestingController` | El adaptador de autos pega al endpoint correcto y mapea la respuesta |
| Componentes | Render y eventos, sin reglas de negocio | El formulario emite el evento de crear auto con los datos correctos |

### Criterio mínimo de la prueba

No se busca cobertura del 100%, sino demostrar buenas prácticas: dominio y casos de uso con tests unitarios, la regla de ownership cubierta, y al menos un test de integración del flujo auth → CRUD. Sin tests, la arquitectura hexagonal es solo carpetas.

## Plan de implementación

### Fase 1: Base del proyecto

- Crear estructura `backend/`, `frontend/` y `database/`.
- Crear scripts para Apple `container`.
- Definir red local `car-app-net`.
- Definir volumen persistente `sqlserver-data`.
- Configurar SQL Server en contenedor.
- Verificar que todos los servicios levanten.

### Fase 2: Backend

- Crear proyecto Spring Boot.
- Configurar conexión a SQL Server.
- Definir estructura hexagonal por módulos.
- Crear modelos de dominio `User` y `Car`.
- Crear puertos de entrada para autenticación y autos.
- Crear puertos de salida para persistencia, hashing y JWT.
- Implementar casos de uso de registro y login.
- Configurar JWT con Spring Security.
- Implementar adaptadores JPA para usuarios y autos.
- Implementar adaptadores REST para autenticación y autos.
- Implementar CRUD de autos protegido por usuario autenticado.
- Agregar validaciones básicas.
- Tests unitarios de dominio y casos de uso (incluida la regla de ownership).
- Tests de adaptador web (`MockMvc`) y de persistencia (Testcontainers).

### Fase 3: Frontend

- Crear proyecto Angular.
- Definir estructura por dominio, aplicación, infraestructura y presentación.
- Crear rutas públicas y privadas.
- Crear formularios de login y registro.
- Crear puertos y casos de uso frontend para autenticación y autos.
- Crear adaptadores HTTP para consumir la API.
- Guardar JWT de forma controlada en infraestructura.
- Implementar pantalla de autos.
- Implementar crear, editar y eliminar autos.
- Tests de casos de uso, adaptadores HTTP y componentes clave.

### Fase 4: Integración

- Conectar Angular con Spring Boot.
- Configurar CORS.
- Validar flujo completo:
  - Registro.
  - Login.
  - Crear auto.
  - Listar autos.
  - Editar auto.
  - Eliminar auto.

### Fase 5: Calidad y entrega

- Agregar README con pasos de ejecución.
- Agregar colección Postman opcional.
- Revisar manejo de errores.
- Revisar que no haya credenciales sensibles hardcodeadas.
- Validar que el proyecto corra con `container system start` y `./scripts/container-up.sh`.

## Criterios de aceptación

- El proyecto se puede levantar sin instalar Java, Maven, Node, Angular CLI ni SQL Server localmente.
- El entorno local usa Apple `container` y scripts propios del proyecto.
- Un usuario puede registrarse e iniciar sesión.
- El backend devuelve un JWT válido al iniciar sesión.
- Los endpoints privados rechazan requests sin token.
- Cada usuario solo puede administrar sus propios autos.
- Angular permite listar, crear, editar y eliminar autos.
- La base de datos mantiene relación entre usuarios y autos.
- El dominio no depende de Spring Boot, JPA, JWT, SQL Server ni Angular.
- Los controllers y componentes no contienen reglas de negocio.
- La persistencia y seguridad están implementadas como adaptadores.
- El dominio y los casos de uso tienen tests unitarios, incluida la regla de propiedad del auto.
- Existe al menos un test de integración del flujo auth → CRUD.
- La documentación explica cómo ejecutar el proyecto y cómo correr los tests.

## Riesgos y decisiones pendientes

| Tema | Riesgo | Decisión sugerida |
|---|---|---|
| SQL Server en Apple `container` | La imagen oficial solo es `amd64`; en Mac `arm64` corre por emulación | Resuelto: lanzar el contenedor con `--platform linux/amd64`. Verificado que la emulación funciona en `arm64` |
| Sin Docker Compose | Apple `container` no debe tratarse como Compose | Usar scripts `container-up.sh` y `container-down.sh` |
| Almacenamiento del JWT | `localStorage` es simple pero expone más superficie ante XSS | Usarlo para prueba técnica; documentar el tradeoff |
| Refresh tokens | Agrega complejidad | Dejar fuera del alcance inicial |
| Fotos de autos | Subida real implica storage y multipart | Usar campo simulado `photo_url` |

## Siguiente paso

Crear la base del repositorio con `backend/`, `frontend/`, `database/` y `scripts/`, y después implementar primero el backend de autenticación antes de construir las pantallas Angular.
