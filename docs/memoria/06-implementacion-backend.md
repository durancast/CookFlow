# Capítulo 6. Implementación del backend

## 6.1. Introducción

Este capítulo detalla la implementación del backend Spring Boot de CookFlow:
estructura de paquetes, flujo de una petición, seguridad JWT + RBAC,
gestión de errores y ejemplos representativos de código por capa. El código
completo vive en `backend-spring/src/main/java/com/cookflow/`.

## 6.2. Estructura del módulo

El artefacto Maven es `com.cookflow:cookflow-backend:0.1.0-SNAPSHOT`,
empaquetado como *fat JAR* `target/cookflow-backend-0.1.0-SNAPSHOT.jar`
(plantilla `spring-boot-starter-parent` 3.5.16, Java 21).

```
com.cookflow
├── config        SecurityConfig, OpenApiConfig, JpaAuditingConfig
├── controller    10 @RestController (Auth, Menu, Dish, Category,
│                 Ingredient, Table, Order, User, Report, Dashboard)
├── dto           records agrupados por recurso (dto/auth, dto/dish, ...)
├── domain        13 tipos (entidades JPA + enums + ID compuesta
│                 DishIngredientId)
├── exception     GlobalExceptionHandler (@RestControllerAdvice)
├── repository    interfaces Spring Data JPA
├── security      JwtService, JwtAuthenticationFilter
└── service       10 servicios de negocio
```

## 6.3. Capa de presentación (controllers)

Los controladores son delgados: validan la petición, resuelven el actor y el
tenant, y delegan en un servicio. Ejemplo real del menú público:

```java
@RestController
@RequestMapping("/api/public")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menu")
    public MenuDto menu(@RequestParam(required = false) String tenant) {
        return menuService.publicMenu(tenant);
    }
}
```

Observaciones:
- Inyección de dependencias por constructor (inmutable y probable con
  constructor mocking).
- Sin `@RequestBody` → sin deserialización de peticiones GET.
- El param `tenant` es opcional: si no se pasa, el servicio decide el tenant
  por defecto o devuelve error 400 coherente.

## 6.4. Capa de servicio

Los servicios contienen las anotaciones transaccionales y las validaciones
de negocio. Patrón repetido para operaciones de pedido:

```java
@Service
public class OrderService {
    private final OrderRepository orders;
    private final DiningTableRepository tables;

    @Transactional
    public OrderDto transition(Long id, OrderStatus target, Principal p) {
        Order order = orders.findByIdAndTenantId(id, p.tenantId())
              .orElseThrow(() -> new NotFound("order", id));
        if (!order.getStatus().canTransitionTo(target)) {
            throw new InvalidTransition(order.getStatus(), target);
        }
        order.setStatus(target);
        if (target == OrderStatus.paid) order.setClosedAt(Instant.now());
        return OrderDto.from(order);
    }
}
```

Notas de diseño:
- `@Transactional` en el servicio (no en el controller).
- Las excepciones de dominio (`NotFound`, `InvalidTransition`) son inmutables
  y llevan el contexto suficiente para el `handler` traducirlas a HTTP.
- El tenant se resuelve de la `Principal` autenticado, nunca de un query
  param confiable (evita cross-tenant access).

## 6.5. Capa de persistencia

Interfaces Spring Data JPA con consultas derivadas o `@Query` con filtros
multi-tenant:

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);
    List<Order> findByTenantIdAndStatus(Long tenantId, OrderStatus status,
                                        Pageable page);
}
```

La relación `dish_ingredients` se modela con ID compuesta:

```java
@Embeddable
public record DishIngredientId(@NonNull Long dishId,
                               @NonNull Long ingredientId) {}

@Entity
@Table(name = "dish_ingredients")
public class DishIngredient {
    @Embedded
    @Id
    private DishIngredientId id;
    // quantity, unit, essential ...
}
```

Esto refleja exactamente el PK compuesta definida en el esquema
(cap. 5) y evita entidades "hija" con identidad artificial.

## 6.6. Seguridad JWT + RBAC

### 6.6.1. Emisión del token
El servicio `JwtService` (jjwt 0.12) firma HS256 con `JWT_SECRET`:

- `sub`: email del usuario
- `tenant`: `tenantId` (claim propio)
- `role`: `admin` | `manager` | `kitchen` | `waiter`
- `exp`: expiración configurada

### 6.6.2. Filtro de autenticación

`JwtAuthenticationFilter` implementa `OncePerRequestFilter`:

1. Extrae el encabezado `Authorization`.
2. Si el token parsea correctamente, publica
   `UsernamePasswordAuthenticationToken(email, null,
       List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())))`
   en el `SecurityContext`.
3. Si falla, la cadena de filtros sigue y el endpoint protegido responderá
   401.

### 6.6.3. Reglas de acceso

`SecurityConfig` declara, resumen:

```
GET  /api/public/**       -> permitAll
POST /api/auth/login      -> permitAll
GET  /api/auth/**         -> authenticated
*    /api/**              -> hasRole según operación
```

Las restricciones por rol (admin-only para `user`, `kitchen` para
transiciones de pedido, etc.) se declaran en `SecurityConfig` y se
refuerzan en el servicio (doble verificación ante peticiones de APIs internas).

## 6.7. Gestión de errores

`GlobalExceptionHandler` traduce:

| Excepción | HTTP | Cuerpo |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `{errors: [...]}` |
| `NotFound` (dominio) | 404 | `{error: "not_found"}` |
| `InvalidTransition` | 409 | `{error: "invalid_transition", from, to}` |
| `AuthenticationException` | 401 | `{error: "unauthorized"}` |
| `AccessDeniedException` | 403 | `{error: "forbidden"}` |
| `Exception` (genérico) | 500 | registro en logs, cuerpo genérico |

El formato de error es estable para que el frontend pueda hacer `switch`
sobre `error` sin acoplarse a mensajes humanos.

## 6.8. Validación y DTOs

Los DTO son `record` con anotaciones `jakarta.validation`:

```java
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 128) String password) {}
```

- Inmutables → sin estado interno accidental.
- `@Valid` en el parámetro del controller dispara la validación.
- Separación DTO ↔ entidad evita exponer `password_hash`, `tenant_id`, etc.

## 6.9. Configuración

`application.yml` centraliza:

- `spring.datasource.url` → variable `SPRING_DATASOURCE_URL`
  (inyectable por el entorno de Docker).
- `spring.jpa.hibernate.ddl-auto=validate` (en producción) /
  `update` (en dev).
- `app.jwt.secret` → `JWT_SECRET`.
- Actuator básico para health.

## 6.10. Ejemplo de flujo completo (login)

1. `POST /api/auth/login` con `{email, password}`.
2. `AuthController.login` valida el `LoginRequest` (`@Valid`).
3. `AuthService.login` busca el usuario por email en el tenant default,
   compara BCrypt y emite `JwtService.createToken(user)`.
4. Respuesta `{token, user: {id, email, role}}`.
5. Peticiones subsiguientes envían `Authorization: Bearer <token>`;
   `JwtAuthenticationFilter` lo valida y publica el principal.
6. El controller protegido (p. ej. `OrderController`) usa la `Principal`
   para extraer `tenantId` y aplicar filtros.

## 6.11. Conclusión del capítulo

La implementación queda organizada en capas con reglas simples
(controller = HTTP, service = negocio, repository = SQL) y con decisiones
explícitas que facilitan el mantenimiento: records inmutables,
transacciones en el servicio, seguridad declarativa, errores de dominio
consistentes y un esquema de datos aprovechado al máximo (cap. 5).
