# Capítulo 5. Base de datos

## 5.1. Introducción

El esquema de datos es uno de los activos de mayor peso en un sistema TPV:
debe garantizar la integridad operativa (un pedido no puede existir sin mesa,
un plato no puede existir sin tenant) y permitir consultas agregadas de
informe sin degradar la escritura de ventas. En este capítulo se describe
el esquema final sobre PostgreSQL 16, se comparan las diferencias clave con
esquema MySQL de origen y se documentan los tipos especializados
(`ENUM`, `CITEXT`, `GENERATED`) que se aprovechan tras la migración.

## 5.2. Diagrama de entidades

```
Tenant (1) ----< User (N)
   |                    ^
   |                    | tenant_id
   v                    |
Category (N) ----< Dish (N)
                      +----< DishIngredient (N) ----> Ingredient (N)
   +----< DiningTable (N)
   +----< Order (N) ----< OrderItem (N) ----> Dish (N)
```

### Tabla `tenants`
| Col | Tipo | Notas |
|---|---|---|
| `id` | `BIGSERIAL PK` | |
| `name` | `TEXT NOT NULL` | |
| `slug` | `CITEXT UNIQUE` | Para el query `?tenant=` |
| `created_at` | `TIMESTAMPTZ` | default `now()` |

Nota: se emplea el tipo `CITEXT` (extensión disponible en Postgres) para
igualdad de tenencia sin depender de `COLLATE` por consulta.

### Tabla `users`
| Col | Tipo | Notas |
|---|---|---|
| `id` | `BIGSERIAL PK` | |
| `tenant_id` | `BIGINT NOT NULL FK→tenants` | Multi-tenancy |
| `email` | `CITEXT NOT NULL UNIQUE` | login |
| `password_hash` | `TEXT NOT NULL` | BCrypt |
| `role` | `user_role` (ENUM) | `admin`, `manager`, `kitchen`, `waiter` |
| `email_verified_at` | `TIMESTAMPTZ` | nullable |

Constraint de unicidad por tenant en el email si se quiere permitir el mismo
email en tenants distintos:
`UNIQUE (tenant_id, email)` — se adopta esta forma en lugar del `UNIQUE
también a nivel global para poder migrar desde la tabla global de Laravel.

### Tabla `dishes` (con `dish_ingredients`)
| Col | Tipo | Notas |
|---|---|---|
| `id` | `BIGSERIAL PK` | |
| `tenant_id` | `BIGINT NOT NULL FK` | |
| `category_id` | `BIGINT NOT NULL FK→categories` | |
| `name` | `TEXT NOT NULL` | |
| `description` | `TEXT` | nullable |
| `price_cents` | `INT NOT NULL CHECK (price_cents >= 0)` | Evitar `DOUBLE` |
| `available` | `BOOLEAN NOT NULL DEFAULT true` | |

La relación **plato-ingredientes** usa tabla intermedia `dish_ingredients`
con PK compuesta `(dish_id, ingredient_id)` y campos `quantity`,
`unit`, `essential`:

```
dish_ingredients (
  dish_id       BIGINT NOT NULL FK → dishes,
  ingredient_id BIGINT NOT NULL FK → ingredients,
  quantity      NUMERIC(12,3) NOT NULL CHECK (quantity >= 0),
  unit          ingredient_unit NOT NULL,      -- ENUM: 'g','kg','ml','l','unit'
  essential     BOOLEAN NOT NULL DEFAULT true,
  PRIMARY KEY (dish_id, ingredient_id)
)
```

### Tabla `orders`
| Col | Tipo | Notas |
|---|---|---|
| `id` | `BIGSERIAL PK` | |
| `tenant_id` | `BIGINT NOT NULL` | |
| `table_id` | `BIGINT NOT NULL FK→tables` | |
| `status` | `order_status NOT NULL` | ENUM: `pending`, `preparing`, `served`, `paid` |
| `total_cents` | `INT NOT NULL` | denormalizado para informe |
| `opened_by` | `BIGINT FK→users` | quien abre |
| `closed_at` | `TIMESTAMPTZ` | nullable |

Constraint de integridad de flujo (a nivel de aplicación por ahora; ver
cap. 6 para la implementación):

```
-- ejemplo de invariantes (aplicación)
-- pendiente -> preparando -> servido -> pagado
```

### Tabla `tables` (mesas)
`tenant_id`, `name`, `seats`, `status table_status ENUM (free, occupied, pending)`.

## 5.3. Tipado de datos: decisiones

1. **Dinero.** `price_cents`/`total_cents` como `INT` centavos. Evita el
   error de representación de `NUMERIC(10,2)` y de `DOUBLE PRECISION`.
2. **Cantidad en ingredientes.** `NUMERIC(12,3)` para admitir `0.25 kg`,
   `1.5 l`, etc.
3. **Enumeraciones.** PostgreSQL `CREATE TYPE order_status AS ENUM (...)`
   mapeado con `@Enumerated(EnumType.STRING)` en JPA, manteniendo nombres
   en minúscula para evitar discrepancias.
4. **Emails / slugs.** `CITEXT` (extensión citext) en vez de `VARCHAR` con
   `WHERE LOWER(email)=` en cada constraint.
5. **Timestamps.** `TIMESTAMPTZ` en toda fecha; evita bugs de zona horaria
   al reportar ventas.

## 5.4. Índices y particionamiento

- Índices compuestos de cobertura para consultas multi-tenant frecuentes:
  - `orders (tenant_id, status)` (informes por estado)
  - `orders (tenant_id, table_id, closed_at DESC)` (historial de mesa)
  - `dishes (tenant_id, category_id, available)` (menú público)
- No se aplica particionamiento: el volumen esperado de pedidos/día por
  tenant no lo justifica; queda como trabajo futuro (particionamiento por
  mes en `orders`).

## 5.5. Migración de esquema: MySQL → PostgreSQL

Diferencias aplicadas durante la migración:

| Tema | MySQL (origen) | PostgreSQL (final) |
|---|---|---|
| Enumeraciones | Columna `VARCHAR` + app check | Tipo `ENUM` nativo, constraint DB |
| Emails | `VARCHAR`, `WHERE LOWER()` | `CITEXT` |
| Tabla intermedia plato-ingrediente | Varios `VARCHAR` + app logic | PK compuesta + `NUMERIC` + `ENUM unit` |
| Timestamps | `DATETIME` (naive) | `TIMESTAMPTZ` |
| UUIDs | `UUID` (si existían) | `BIGSERIAL` (más simple); UUID en trabajo futuro |
| Dialecto SQL | MySQL 8 | PostgreSQL 16, con `CITEXT` |

## 5.6. Seguridad de datos

- Contraseñas: hash **BCrypt** (coste 10) en la columna `password_hash`; la
  app **nunca** expone esta columna en DTOs.
- Multi-tenancy: constraint de integridad `FK` + índice compuesto. En
  servicios se fuerza el filtro por `tenant_id` del usuario autenticado
  (ver cap. 6 y test de aislamiento en cap. 7).
- `pg_hba.conf` (Docker): solo accesos internos por red del compose.

## 5.7. Conclusión del capítulo

El esquema final aprovecha tipos nativos de PostgreSQL que el esquema MySQL
no exponía (ENUM, CITEXT), centraliza invariantes en la base de datos, y
conserva una forma tabular sencilla que las consultas de informe pueden
resolver sin denormalizaciones costosas. La decisión de denormalizar
`total_cents` en `orders` es un intercambio consciente entre consistencia
y simplicidad de consulta, justificable en el contexto TPV.
