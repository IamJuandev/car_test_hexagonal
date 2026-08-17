# Documentación del proceso — App de gestión de autos

> Cómo se construyó la aplicación: metodología, arquitectura hexagonal en el
> backend (Spring Boot + Java), el frontend (Angular), la persistencia en SQL
> Server y la dockerización con Apple `container`.
>
> Pensado para **entenderlo** y para **exponerlo** (cada sección equivale a una
> o dos diapositivas).

---

## 1. Qué es la aplicación

Una app web donde **cada usuario gestiona sus propios autos**: se registra,
inicia sesión, y crea / lista / edita / elimina autos que solo él puede ver.

| Capa | Tecnología |
|---|---|
| Frontend | Angular 18 (standalone components, signals) |
| Backend | Spring Boot 3.3 + Java 21 |
| Base de datos | SQL Server 2022 |
| Seguridad | JWT (HMAC-SHA) |
| Entorno local | Apple `container` (sin instalar nada en la máquina) |

**Regla de negocio central:** un usuario nunca puede tocar el auto de otro. Si
lo intenta, el sistema responde **404** (ni siquiera revela que el auto existe).

---

## 2. Metodología de trabajo

No se programó "todo de una". Se trabajó en **slices verticales**: cada slice
atraviesa todas las capas y deja algo funcionando de punta a punta.

```
Slice 1 → Infraestructura + base de datos (esqueleto + schema + contenedores)
Slice 2 → Backend hexagonal (auth + cars) con tests unitarios
Slice 3 → Frontend hexagonal (login, listado, formulario)
Slice 4 → Cierre: datos demo (seed), test de integración, funcionalidades plus
```

Principios que guiaron todo el trabajo:

1. **Conceptos antes que código.** Primero se definió la arquitectura
   (hexagonal) y recién después se escribió código.
2. **Contenedores primero ("container-first").** No se instaló Java, Maven,
   Node ni SQL Server en la máquina. Todo compila y corre dentro de
   contenedores. Esto hace el proyecto **reproducible** en cualquier máquina.
3. **Tests por capa.** El dominio y los casos de uso se testean sin base de
   datos; hay un test de integración que valida el flujo real contra SQL Server.
4. **Documentación viva.** `propuesta.md` (el plan), `PROGRESO.md` (el avance) y
   este documento.

---

## 3. Arquitectura hexagonal (el concepto)

También llamada **puertos y adaptadores**. La idea es simple y poderosa:

> El **núcleo de negocio** (dominio + casos de uso) no sabe NADA del mundo
> exterior. No sabe que hay una base de datos SQL Server, ni que la entrada
> llega por HTTP, ni que la seguridad es JWT.

```
                  ┌─────────────────────────────────────┐
   HTTP  ───▶ ┌── │  PUERTO IN        DOMINIO            │ ──┐ PUERTO OUT ──▶  BD
 (adapter in) │   │  (interface)   (reglas de negocio)   │   │ (interface)   (adapter out)
              └── │                  CASOS DE USO         │ ──┘
                  └─────────────────────────────────────┘
                          ↑ el centro no depende de nada ↑
```

- **Puerto** = una interfaz (un contrato). Define *qué* se puede hacer.
- **Adaptador** = una implementación concreta de ese contrato. Define *cómo*.
- **La regla de oro:** las dependencias apuntan **hacia adentro**. La
  infraestructura conoce al dominio, nunca al revés.

**¿Para qué sirve?** Podés cambiar SQL Server por PostgreSQL, o HTTP por una
cola de mensajes, **sin tocar una sola línea del negocio**. Solo cambiás el
adaptador. Y podés testear el negocio sin levantar nada.

---

## 4. Backend: hexagonal con Spring Boot + Java

### 4.1 Organización por módulos

El código se divide por **módulo de negocio** (no por capa técnica). Cada módulo
es un mini-hexágono:

```
com.example.cars
├── shared/      → cosas comunes (excepciones base, value objects)
├── users/       → el usuario
├── auth/        → registro, login, JWT
└── cars/        → el CRUD de autos
     ├── domain/                          ← el centro (Java puro, sin Spring)
     ├── application/
     │   ├── port/in/    (casos de uso)   ← qué puede pedir el mundo
     │   ├── port/out/   (repositorios)   ← qué necesita el negocio del mundo
     │   └── service/    (implementación de los casos de uso)
     └── infrastructure/
         ├── adapter/in/web/         (controllers REST, DTOs)
         └── adapter/out/persistence (JPA, entidades de BD)
```

### 4.2 La capa de DOMINIO — Java puro, cero frameworks

El dominio modela el negocio y **protege sus propias reglas (invariantes)**.
Fijate que `Car` no tiene anotaciones de Spring ni de JPA — es Java puro:

```java
public class Car {
    private final CarId id;
    private final UserId ownerId;   // un auto SIEMPRE sabe de quién es
    private String brand, model, plateNumber, color, photoUrl;
    private int year;

    // Constructor privado + factories: no podés crear un Car inválido
    public static Car create(UserId ownerId, String brand, ...) { ... }

    // LA regla de propiedad vive acá, en el dominio:
    public boolean belongsTo(UserId userId) {
        return ownerId.equals(userId);
    }

    // Validación de invariantes (se re-valida en cada cambio):
    private void applyDetails(...) {
        this.brand = requireText(brand, "Brand");
        if (year < 1886 || year > Year.now().getValue() + 1)
            throw new InvalidInputException("Year must be between ...");
        ...
    }
}
```

**Por qué importa:** es imposible que exista un `Car` en un estado inválido. La
validación no está dispersa en controllers; está donde corresponde, en el objeto.

### 4.3 Los PUERTOS — interfaces, los contratos

**Puerto de entrada** (lo que el mundo puede pedirle al negocio):

```java
public interface CreateCarUseCase {
    Car create(UserId owner, CreateCarCommand command);
}
```

**Puerto de salida** (lo que el negocio necesita del mundo, p. ej. persistencia):

```java
public interface CarRepositoryPort {
    Car save(Car car);
    Optional<Car> findById(CarId carId);
    List<Car> findAllByOwner(UserId owner);
    void deleteById(CarId carId);
    boolean existsByOwnerAndPlate(UserId owner, String plateNumber);
}
```

Notá: el puerto habla en términos del **dominio** (`Car`, `UserId`), no de tablas
ni de SQL.

### 4.4 El CASO DE USO — orquesta, aplica reglas, no sabe de BD

`CarService` implementa los puertos de entrada y usa los de salida. Acá se
**aplica la regla de propiedad** y se **coordina** la operación:

```java
@Service
public class CarService implements CreateCarUseCase, UpdateCarUseCase, ... {

    private final CarRepositoryPort carRepository;   // ← depende del PUERTO, no de JPA

    @Override @Transactional
    public Car create(UserId owner, CreateCarCommand cmd) {
        if (carRepository.existsByOwnerAndPlate(owner, cmd.plateNumber()))
            throw new DuplicatePlateException(cmd.plateNumber());   // placa única por dueño
        Car car = Car.create(owner, cmd.brand(), ...);
        return carRepository.save(car);
    }

    @Override @Transactional
    public Car update(UserId owner, long carId, UpdateCarCommand cmd) {
        Car car = requireOwnedCar(owner, carId);   // ← carga y verifica propiedad → si no, 404
        ...
        return carRepository.save(car);
    }
}
```

El servicio **no sabe** que detrás hay SQL Server. Solo conoce `CarRepositoryPort`.

### 4.5 Los ADAPTADORES — el mundo real enchufado a los puertos

**Adapter de entrada (web):** traduce HTTP ↔ casos de uso.

```java
@RestController @RequestMapping("/api/cars")
public class CarController {
    @PostMapping
    public CarResponse create(@AuthenticationPrincipal Long userId,
                              @Valid @RequestBody CarRequest request) { ... }
    // GET, GET/{id}, PUT/{id}, DELETE/{id}
}
```

**Adapter de salida (persistencia):** implementa el puerto usando JPA y
**traduce entre el dominio y la entidad de base de datos**.

```java
@Component
public class CarPersistenceAdapter implements CarRepositoryPort {
    private final CarJpaRepository jpa;   // Spring Data JPA

    @Override
    public Car save(Car car) {
        CarJpaEntity saved = jpa.save(new CarJpaEntity(...));  // dominio → entidad
        return toDomain(saved);                                 // entidad → dominio
    }

    private Car toDomain(CarJpaEntity e) {   // ← traducción explícita
        return Car.reconstitute(CarId.of(e.getId()), UserId.of(e.getUserId()), ...);
    }
}
```

**Este mapeo explícito es la clave del hexágono:** el `Car` del dominio y el
`CarJpaEntity` (que tiene `@Entity`, `@Table`, etc.) son **dos clases distintas**.
Así, las anotaciones de JPA nunca "contaminan" el dominio.

---

## 5. Persistencia: cómo guardamos en la base de datos

### 5.1 El schema lo manda la BD, no Hibernate

Decisión importante: en `application.yml` está `ddl-auto: none`. **Hibernate NO
crea ni modifica tablas.** El schema es responsabilidad de `database/init.sql`:

```sql
USE CarApp;

CREATE TABLE dbo.users (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE dbo.cars (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    brand        VARCHAR(80)  NOT NULL,
    ...
    CONSTRAINT fk_cars_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
);

-- Una placa es única POR USUARIO (no global):
CREATE UNIQUE INDEX uq_cars_user_plate ON dbo.cars(user_id, plate_number);
```

**Por qué `ddl-auto: none`:** en producción no querés que el ORM toque el schema
por su cuenta. El SQL es explícito, versionado y revisable. El script es
**idempotente** (`IF OBJECT_ID(...) IS NULL`), se puede correr mil veces sin romper.

### 5.2 La entidad JPA es solo del adapter

```java
@Entity @Table(name = "cars")
public class CarJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id") private Long userId;
    @Column(name = "plate_number") private String plateNumber;
    ...
}
```

Vive en `infrastructure/adapter/out/persistence`. El dominio jamás la importa.

### 5.3 El camino de un dato

```
Car (dominio)  ──CarPersistenceAdapter──▶  CarJpaEntity  ──Spring Data JPA──▶  tabla dbo.cars
     ▲                                                                              │
     └───────────────  toDomain()  ◀── CarJpaEntity ◀── SELECT ◀────────────────────┘
```

---

## 6. Seguridad: cómo funciona el JWT

### 6.1 El concepto en una frase

El usuario y la contraseña sirven para **entrar una vez**. A partir de ahí, el
**token JWT** es la "pulsera" que prueba quién sos en cada pedido.

### 6.2 El flujo

```
1. POST /api/auth/register  (name, email, password)  ──▶  devuelve un token
   POST /api/auth/login      (email, password)        ──▶  devuelve un token

2. Cada pedido protegido lleva el token:
   GET /api/cars   con header  →  Authorization: Bearer <token>

3. El token vence en 1 hora. Cuando vence → 401 → volvés a hacer login.
```

**Solo mandás email+password en el paso 1.** Nunca más, hasta que el token expire.

### 6.3 Por qué se repite en cada pedido: es *stateless*

El backend **no guarda sesiones en memoria**. No se "acuerda" de vos entre
pedidos. Por eso cada request tiene que probar su identidad, y esa prueba es el
token firmado. Esto permite escalar horizontalmente (cualquier instancia puede
atender cualquier pedido).

### 6.4 También es hexagonal

```java
// PUERTO (dominio/aplicación): el negocio solo sabe que "alguien genera tokens"
public interface TokenProviderPort {
    String generateToken(UserId userId);
    Optional<UserId> validateAndExtractUserId(String token);
}

// ADAPTER (infraestructura): la implementación concreta con jjwt
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {
    public String generateToken(UserId userId) {
        return Jwts.builder()
            .subject(String.valueOf(userId.value()))  // el "sub" lleva el userId
            .issuedAt(now).expiration(now + 1h)
            .signWith(key)                              // firma HMAC-SHA
            .compact();
    }
}
```

Estructura del token generado:
- **header** → `{"alg":"HS384"}` (HMAC con SHA-384)
- **payload** → `{ sub: <userId>, iat: <emitido>, exp: <+1h> }`
- **firma** → HMAC sobre header+payload con el secret. Sin el secret no se puede
  falsificar ni alterar.

En cada request, el `JwtAuthenticationFilter` (Spring Security) valida la firma,
extrae el `userId` del `sub` y lo inyecta como `@AuthenticationPrincipal`.

> **Nota de producción:** el `JWT_SECRET` por defecto es de desarrollo. Al
> desplegar, hay que setearlo como variable de entorno con un valor random largo.

---

## 7. Frontend: hexagonal con Angular

Sí — **el frontend también es hexagonal**. La misma idea: la lógica de la app no
sabe que los datos vienen por HTTP.

### 7.1 Estructura

```
src/app/
├── core/
│   ├── domain/            → modelos (Car, AuthSession) — TypeScript puro
│   ├── application/
│   │   ├── ports/         → interfaces (CarRepositoryPort, SessionStoragePort)
│   │   └── use-cases/     → ListCars, CreateCar, Login, ... (lógica)
│   └── infrastructure/
│       ├── http/          → adapters HTTP (CarHttpAdapter), interceptor JWT
│       └── auth/          → adapter de almacenamiento (localStorage)
├── features/
│   ├── auth/presentation/ → login, register (componentes)
│   └── cars/presentation/ → listado (cards), formulario/modal
└── shared/presentation/   → authGuard
```

### 7.2 Los puertos se "enchufan" a los adapters en `app.config.ts`

Esto es el equivalente Angular del cableado hexagonal. Un componente pide
`CarRepositoryPort` (la interfaz) y Angular le inyecta `CarHttpAdapter` (la impl):

```typescript
providers: [
  provideHttpClient(withInterceptors([jwtInterceptor])),

  // Cableado hexagonal: puertos del dominio → adapters de infraestructura
  { provide: AuthRepositoryPort, useClass: AuthHttpAdapter },
  { provide: CarRepositoryPort,  useClass: CarHttpAdapter },
  { provide: SessionStoragePort, useClass: LocalStorageSessionAdapter },
],
```

Si mañana quisieras leer de un mock o de otra API, cambiás **una línea** acá.

### 7.3 El adapter HTTP

```typescript
@Injectable()
export class AuthHttpAdapter extends AuthRepositoryPort {
  private readonly http = inject(HttpClient);
  override login(creds: Credentials): Observable<AuthSession> {
    return this.http.post<AuthSession>(`${this.baseUrl}/api/auth/login`, creds);
  }
}
```

### 7.4 El JWT viaja solo (interceptor)

Acá se ve por qué en el frontend **no copiás el token a mano**: un interceptor lo
agrega automáticamente a cada request si hay sesión.

```typescript
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(SessionStoragePort).token();
  if (!token) return next(req);
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
```

### 7.5 El guard protege las rutas privadas

```typescript
export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (inject(CurrentSessionUseCase).isAuthenticated()) return true;
  return router.createUrlTree(['/login']);   // sin sesión → al login
};
```

Las rutas son **lazy** (`loadComponent`): cada pantalla se descarga solo cuando
se visita, así el bundle inicial es chico.

---

## 8. Dockerización con Apple `container`

### 8.1 Por qué contenedores

Para no instalar Java, Maven, Node ni SQL Server en la máquina. Todo se levanta
con un comando: `./scripts/container-up.sh`. Tres contenedores:

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│ car-frontend│     │ car-backend  │     │ car-db          │
│ Angular     │     │ Spring Boot  │     │ SQL Server 2022 │
│ nginx :4200 │     │ :8080        │     │ :1433           │
│ arm64       │     │ amd64        │     │ amd64 (emulado) │
└─────────────┘     └──────────────┘     └─────────────────┘
        red interna car-app-net + puertos publicados al host
```

> Apple `container` **no es Docker Compose**: cada contenedor corre en su propia
> mini-VM Linux. Eso trae varias trampas, todas resueltas en el script.

### 8.2 Las trampas que hubo que resolver (¡oro para la expo!)

| Problema | Solución |
|---|---|
| SQL Server solo existe en amd64, el Mac es arm64 | `--platform linux/amd64` (emulación) |
| SQL Server necesita ≥2 GB de RAM | `--memory 4g` |
| Como no-root no puede crear `/.system` | corre `-u root` |
| La imagen no trae `sqlcmd` y el cliente viejo no negocia TLS con SQL 2022 | imagen propia con `go-sqlcmd` |
| Los contenedores solo se enrutan entre **misma arquitectura** | la imagen de tools también es amd64 |
| `container build` **rompe la red** de los contenedores ya levantados | se buildea TODO **antes** de arrancar cualquier contenedor |
| No hay DNS por nombre | se resuelve la **IP dinámica** de la BD y se le inyecta al backend |

### 8.3 La secuencia del script

```
1. BUILD PHASE   → buildear todas las imágenes ANTES (los builds rompen la red)
2. RED + VOLUMEN → crear car-app-net y el volumen sqlserver-data (datos persisten)
3. BASE DE DATOS → levantar SQL Server (amd64, root, 4g), esperar a que acepte conexiones
4. SCHEMA        → aplicar init.sql y seed.sql con go-sqlcmd
5. BACKEND       → resolver IP de la BD, inyectarla como DB_HOST, levantar
6. FRONTEND      → levantar nginx con el bundle de Angular
```

El frontend se buildea **arm64 nativo** (build rápido); backend y BD son amd64.
No chocan porque el browser le pega al backend **vía el host** (`localhost:8080`),
no contenedor-a-contenedor.

### 8.4 Imágenes (multi-stage)

El frontend usa un Dockerfile multi-stage: una etapa Node compila Angular, y el
resultado estático se sirve con nginx (imagen final chica, sin Node).

```dockerfile
FROM node:20 AS build
RUN npm install && npm run build
FROM nginx:alpine
COPY --from=build /app/dist/.../browser /usr/share/nginx/html
```

---

## 9. El flujo completo, de punta a punta

Un ejemplo: el usuario hace clic en "Add car" y guarda.

```
[Browser / Angular]
  Componente → CreateCarUseCase → CarRepositoryPort (puerto)
                                       │
                                       ▼ CarHttpAdapter
  jwtInterceptor agrega  Authorization: Bearer <token>
                                       │  POST /api/cars
                                       ▼
[Backend / Spring Boot]
  JwtAuthenticationFilter valida el token → extrae userId
  CarController (adapter in) → CreateCarUseCase (puerto)
                                       │
                                       ▼ CarService (caso de uso)
  valida placa única, crea Car (dominio valida invariantes)
  CarRepositoryPort.save() (puerto out)
                                       │
                                       ▼ CarPersistenceAdapter
  Car → CarJpaEntity → Spring Data JPA → INSERT
                                       │
                                       ▼
[SQL Server]   tabla dbo.cars
```

Fijate que el **dominio y los casos de uso** (el centro) son los mismos sin
importar que la entrada sea HTTP o que la salida sea SQL Server. Eso es hexagonal.

---

## 10. Testing

| Tipo | Qué prueba | Cómo corre |
|---|---|---|
| **Unitarios backend (21)** | Dominio + casos de uso, sin BD (puertos mockeados) | `mvn test` en contenedor Maven |
| **Integración backend** | Flujo real auth → CRUD contra SQL Server vivo | `mvn test -P it` |
| **Unitarios frontend (10)** | Casos de uso, adapter HTTP, filtro, modal | Karma + Chrome headless en contenedor |

La regla de propiedad se testea explícitamente: pedir el auto de otro usuario
debe dar 404; placa duplicada, 409; año inválido, 400; sin token, 401.

---

## 11. Glosario rápido (para la expo)

- **Hexagonal / Puertos y Adaptadores**: el negocio en el centro, aislado del
  mundo por interfaces (puertos); el mundo se enchufa con implementaciones
  (adaptadores).
- **Puerto IN**: lo que el mundo le puede pedir al negocio (casos de uso).
- **Puerto OUT**: lo que el negocio necesita del mundo (p. ej. persistir).
- **Adapter**: implementación concreta de un puerto (HTTP, JPA, JWT, localStorage).
- **Dominio**: las reglas de negocio en código puro, sin frameworks.
- **JWT**: token firmado que prueba la identidad en cada request (stateless).
- **ddl-auto: none**: el ORM no toca el schema; lo manda el SQL versionado.
- **Container-first**: nada se instala en la máquina; todo corre en contenedores.
- **Slice vertical**: una porción de trabajo que atraviesa todas las capas.

---

### Resumen en una frase

> Construimos una app donde **el negocio está en el centro y aislado**
> (hexagonal), igual en el backend (Spring Boot) que en el frontend (Angular);
> guardamos los datos en SQL Server con un **schema explícito** y un mapeo
> dominio↔entidad; protegemos cada pedido con **JWT stateless**; y corremos todo
> en **contenedores reproducibles** sin instalar nada en la máquina.
