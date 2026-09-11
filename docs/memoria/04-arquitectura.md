# Capítulo 4. Arquitectura

## 4.1. Introducción

Este capítulo describe la arquitectura de alto nivel y la arquitectura
interna del backend Spring Boot. Se parte de una visión "monolito modular":
un único despliegue con capas bien delimitadas, sin microservicios ni
buses, y se detalla el flujo de una petición típica de principio a fin.

## 4.2. Arquitectura de alto nivel

```
+----------------+        HTTPS/JSON         +---------------------+
|   Frontend     |  <---------------------> |   Ingress / Proxy   |
|   (SPA)        |                           |   (opcional)        |
+----------------+                           +----------+----------+
                                                      |
                                                      v
                                              +-------+----------+
                                              |  Backend Spring  |
                                              |  Boot (8080)     |
                                              +-------+----------+
                                                       |
                                    SQL/JDBC           |
                                                       v
                                              +-------+----------+
                                              | PostgreSQL 16    |
                                              | (5432)           |
                                              +------------------+
```

El backend es un servicio HTTP sin estado: la identidad viaja por cabecera
`Authorization: Bearer <JWT>` y el contexto de tenant se deriva del usuario
autenticado (campo `tenant_id` del user).

## 4.3. Arquitectura interna del backend

El paquete base `com.cookflow` se organiza por responsabilidad (no por "técnica"):

```
com.cookflow
├── config/        # Configuración Security, OpenAPI, JPA
├── controller/    # Capa HTTP: @RestController, mapeo /api/*
├── dto/           # Records de entrada/salida (validados con jakarta.validation)
├── domain/        # Entidades JPA (Dish, Order, User, Tenant, ...)
├── exception/     # @ControllerAdvice + tipos excepcionales de dominio
├── repository/    # Spring Data JPA (interfaces extendiendo JpaRepository)
├── security/      # JwtService, JwtAuthenticationFilter, SecurityConfig
└── service/       # Lógica de negocio, transacciones, orquestación de repos
```

Reglas de flujo:

1. `controller` **solo** valida input (DTO) y delega en `service`.
2. `service` **solo** orquesta `repository` para mantener invariantes de
   dominio. Contiene las anotaciones `@Transactional`.
3. `repository` **solo** persistencia.
4. `domain` contiene las entidades JPA y los enums compartidos
   (`OrderStatus`, `TableStatus`, `UserRole`).

```
HTTP
  |
  v
[ SecurityFilterChain: JWT filter, CORS, RBAC ]
  |
  v
[ Controller: valida DTO, extrae principal/tenant ]
  |
  v
[ Service: @Transactional, validaciones de negocio, máquina de estados ]
  |
  v
[ Repository (Spring Data JPA) ]
  |
  v
[ PostgreSQL ]
```

## 4.4. Decisiones de diseño

### 4.4.1. Monolito modular vs microservicios
Decisión: **monolito modular**. CookFlow tiene un único dominio (restaurante),
un único team y volumen de tráfico acotado. La modularización por paquetes y
las pruebas independientes son suficientes; microservicios aportarían
complejidad operativa sin benefício demostrable.

### 4.4.2. DTO con `records` y validación jakarta
Cada endpoint recibe y devuelve `record`s inmutables. `@Valid` +
`jakarta.validation` en el controlador, y el servicio trabaja sobre los DTOs
ya validados, evitando objetos de entidad expuestos a red.

### 4.4.3. Seguridad por capas
Tres capas de seguridad:
- **Transporte:** en producción TLS por parte del proxy de frontend;
  en desarrollo HTTP local.
- **Autenticación:** `JwtAuthenticationFilter` resuelve el token, carga el
  usuario y publica el `Authentication` en el `SecurityContext`.
- **Autorización (RBAC):** `SecurityConfig` declara `requestMatchers`.
  Ej.:
  - `GET /api/public/**` → `permitAll()`.
  - `POST /api/auth/**` → `permitAll()`.
  - resto → `authenticated()`, y restricciones por `hasRole("ADMIN")` etc.

### 4.4.4. Multi-tenancy en modo "tenant por fila"
Se adopta el modelo más simple y suficiente: cada tabla de negocio lleva
una columna `tenant_id`, con índice compuesto y constraint de integridad.
El valor del tenant para cada petición se extrae del usuario autenticado y
se inyecta en la consulta (patrón visible en las interfaces `repository`).
No se usa esquemas PostgreSQL separados: la granularidad de "tenant por
fila" encaja con el tamaño esperado de la instalación.

### 4.4.5. Máquina de estados del pedido
`OrderStatus.canTransitionTo(next)` centraliza la validación de flujo
(`pending → preparing → served → paid`). El servicio la invoca antes de
escribir, de modo que una transición inválida produce una excepción de
dominio y no deja la base de datos en estado intermedio.

### 4.4.6. Errores HTTP
El `@ControllerAdvice` mapea las excepciones de dominio a códigos HTTP
coherentes (`400` para validación, `401` auth, `403` RBAC, `404` resource
no encontrada, `409` conflicto de estado). El cuerpo JSON incluye un código
estable (`error`) y un mensaje humano (`message`).

## 4.5. Diagrama de componentes

```
        +---------------------------------------------+
        |                HTTP / JSON                   |
        +---------------------------------------------+
              v                 v               v
   +----------------+  +-----------------+  +----------------+
   | /api/public/*  |  | /api/auth/*     |  | /api/admin/*   |
   | (permiso)      |  | (auth)          |  | (roles)        |
   +-------+--------+  +--------+--------+  +-------+--------+
           |                  |                    |
           +------------------+-+------------------+
                              v
                    +---------+---------+
                    |  MenuService      |
                    |  OrderService     |
                    |  AuthService      |
                    |  ReportService    |
                    +---------+---------+
                              |
                              v
                    +---------+---------+
                    |  Repos (JPA)      |
                    +---------+---------+
                              |
                              v
                    +---------+---------+
                    |  PostgreSQL 16    |
                    +-------------------+
```

## 4.6. Conclusión del capítulo

La arquitectura resultante es un monolito bien acotado, con separación estricta
entre capas, seguridad declarativa por rol y un mecanismo de inmutabilidad
(DTOs + records) que reduce la superficie de bugs de validación. El modelo de
tenancy por fila y la máquina de estados del pedido son las dos decisiones que
más impacto tienen en el resto de capítulos: modelado (cap. 5),
implementación (cap. 6) y pruebas (cap. 7).
