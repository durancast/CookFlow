# Capítulo 7. Calidad y pruebas

## 7.1. Introducción

La calidad de un backend TPV no se demuestra con el código que "compila",
sino con pruebas que ejecutan el sistema completo contra una base de datos
real. Este capítulo describe la estrategia de pruebas adoptada en la
migración a Spring Boot: tipos de prueba, harness basado en Testcontainers,
cobertura de los casos más críticos (RBAC, machine de estados del pedido,
aislamiento multi-tenant) y los umbrales de calidad exigidos antes de
considerar la migración completa.

## 7.2. Pirámide de pruebas

| Capa | Herramienta | Objetivo |
|---|---|---|
| Unitarias | JUnit 5 + Mockito | Lógica de servicios aislados (p. ej. `OrderStatus.canTransitionTo`) |
| De repositario | Spring Data + Testcontainers | Consultas derivadas y `@Query` contra Postgres real |
| De API (integración) | `MockMvc` + perfil `test` | End-to-end de endpoints, validación, estado HTTP |
| De seguridad | MockMvc + claims JWT | Verifica RBAC y filtrado por tenant |

La suite actual consta de **38 tests en verde** sobre el backend Spring Boot
(ver §7.7).

## 7.3. Testcontainers con PostgreSQL real

La migración de MySQL a PostgreSQL introduce riesgos de compatibilidad a
nivel de dialecto SQL, tipos (`ENUM`, `CITEXT`) y comportamiento de
constraints. Se resuelven con **Testcontainers 1.21.x**: cada clase de
test de integración arranca un contenedor efímero de
`postgres:16`, aplica las migraciones (`db/migration/V1__baseline.sql`) con
Flyway, y apunta `spring.datasource.*` al puerto mapeado.

Ajustes concretos:
- `@Testcontainers` + `@ServiceConnection` (Spring Boot 3) inyectan la
  conexión automáticamente.
- La migración se ejecuta **antes** de cada clase de test (no por método),
  para contener el overhead.
- Se valida también la existencia de tipos `ENUM` nativos
  (`order_status`, `user_role`, `table_status`) como sanity-check del
  dialecto.

## 7.4. Casos de prueba representativos

A continuación, los casos más relevantes por área:

### 7.4.1. Autenticación y JWT
| Caso | Aserción |
|---|---|
| Login correcto devuelve token | HTTP 200, `token` presente, `user.role` esperado |
| Login con contraseña errónea | HTTP 401, `error = unauthorized` |
| Login con email malformed | HTTP 400, `errors` no vacío |
| `/api/auth/me` sin token | HTTP 401 |
| `/api/auth/me` con token expirado | HTTP 401 |

### 7.4.2. Menú multi-tenant (público y privado)
| Caso | Aserción |
|---|---|
| `GET /api/public/menu?tenant=X` | Devuelve platos de X y NO de Y |
| Sin param tenant | Respuesta acotada según política de tenant default |
| CRUD en platos con rol inadecuado | HTTP 403 |
| `available=false` no aparece en menú público | Filtro activo |

### 7.4.3. Máquina de estados del pedido
| Caso | Aserción |
|---|---|
| `pending → preparing` | HTTP 200, estado actualizado |
| `pending → paid` | HTTP 409 `invalid_transition` |
| `paid → preparing` | HTTP 409 |
| Transición correcta completa (`pending→paid` vía pasos) | Estado final `paid`, `closed_at` set |

### 7.4.4. Aislamiento multi-tenant (seguridad)
| Caso | Aserción |
|---|---|
| Usuario tenant A no lee pedido de tenant B | HTTP 404 (no 403, evita enumeración) |
| Usuario tenant A no crea plato en tenant B | HTTP 404/403 coherente, sin efecto colateral en DB |
| Admin crea usuario con rol no permitido | Validación de rol en DTO |

### 7.4.5. Reportes y dashboard
| Caso | Aserción |
|---|---|
| Totales por estado coherentes con `orders` | Igual suma de `total_cents` |
| Top N platos por cantidad vendida | Orden correcto |

## 7.5. Cobertura y métricas

Se establecen umbrales mínimos por paquete:

| Paquete | Cobertura línea | Cobertura rama |
|---|---|---|
| `service` | ≥ 80 % | ≥ 70 % |
| `security` | ≥ 90 % | ≥ 80 % |
| `controller` | ≥ 60 % | n/a (delgado) |
| `domain` (enums + lógica) | 100 % | 100 % |

Herramienta: JaCoCo. La cobertura se integra en el build Maven
(`mvn verify`) y se falla el CI si un paquete desciende de umbral.

## 7.6. Lint y calidad estática

- **Checkstyle** (o `spring-javaformat`) sobre el backend para estilo.
- **SpotBugs** (o PMD) para patrones peligrosos (SQL interpolado,
  deserialización insegura).
- **Dependabot / DependTrack** para dependencias con CVE.

Estos checks no sustituyen a las pruebas, pero acotan deuda técnica en
refactorizaciones futuras.

## 7.7. Resultado actual de la suite

- **38 tests pasando** (JUnit 5) en la rama actual.
- Build de Maven exitosa (`mvn clean package -DskipTests` +
  `mvn verify` con tests).
- JAR `target/cookflow-backend-0.1.0-SNAPSHOT.jar` generado.
- No se detectan leaks de datos entre tenants en los tests de
  §7.4.4.

## 7.8. Limitaciones y trabajo futuro

- **Pruebas de carga** (Gatling/k6) no incluidas en esta iteración.
- **Contrat-test del frontend** (Pacto) pendiente de incorporar.
- **Profilado de memoria** del JAR bajo carga sostenida.
- **Golden files** para la salida de `ReportService` para detectar
  regresiones de formato sin re-derivar las aserciones.

## 7.9. Conclusión del capítulo

La estrategia adoptada —tests de unidad + integración con Testcontainers +
suites de seguridad y multi-tenancy— cubre los riesgos propios de la
migración (dialecto SQL, RBAC, máquina de estados, tenancy) y permite
considerar la migración completa solo cuando la suite está en verde. El
resultado (38 tests en verde, build OK) es la evidencia objetiva que
sostiene los capítulos 6 e 8.
