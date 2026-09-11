# Esquema PostgreSQL propuesto (CookFlow)

## Enums

```sql
CREATE TYPE user_role AS ENUM ('admin', 'waiter', 'kitchen', 'manager');
CREATE TYPE ingredient_unit AS ENUM ('G', 'KG', 'ML', 'L', 'PIECE');
CREATE TYPE table_status AS ENUM ('free', 'occupied', 'pending');
CREATE TYPE order_status AS ENUM ('pending', 'preparing', 'served', 'paid');
```

## Tablas

### tenants

```sql
CREATE TABLE tenants (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  slug TEXT NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### users

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  name TEXT NOT NULL,
  email CITEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role user_role NOT NULL DEFAULT 'waiter',
  email_verified_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### categories

```sql
CREATE TABLE categories (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  name TEXT NOT NULL,
  slug TEXT NOT NULL
);
CREATE UNIQUE INDEX ux_categories_tenant_slug ON categories(tenant_id, slug);
```

### dishes

```sql
CREATE TABLE dishes (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
  name TEXT NOT NULL,
  description TEXT,
  image_url TEXT,
  price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
  available BOOLEAN NOT NULL DEFAULT true
);
```

### ingredients

```sql
CREATE TABLE ingredients (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  name TEXT NOT NULL,
  default_unit ingredient_unit NOT NULL
);
```

### dish_ingredients

```sql
CREATE TABLE dish_ingredients (
  dish_id BIGINT NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
  ingredient_id BIGINT NOT NULL REFERENCES ingredients(id),
  quantity NUMERIC(10,3) NOT NULL CHECK (quantity > 0),
  unit ingredient_unit NOT NULL,
  PRIMARY KEY (dish_id, ingredient_id)
);
```

### dining_tables

```sql
CREATE TABLE dining_tables (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  number INT NOT NULL,
  capacity INT NOT NULL CHECK (capacity > 0),
  status table_status NOT NULL DEFAULT 'free',
  waiter_called BOOLEAN NOT NULL DEFAULT false,
  waiter_called_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX ux_tables_tenant_number ON dining_tables(tenant_id, number);
```

### orders

```sql
CREATE TABLE orders (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  dining_table_id BIGINT NOT NULL REFERENCES dining_tables(id),
  waiter_id BIGINT NOT NULL REFERENCES users(id),
  status order_status NOT NULL DEFAULT 'pending',
  total NUMERIC(10,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  closed_at TIMESTAMPTZ
);
```

### order_items

```sql
CREATE TABLE order_items (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  dish_id BIGINT NOT NULL REFERENCES dishes(id),
  quantity INT NOT NULL CHECK (quantity > 0),
  unit_price NUMERIC(10,2) NOT NULL,
  notes TEXT
);
CREATE INDEX ix_order_items_order ON order_items(order_id);
```

## Índices multi-tenant

```sql
CREATE INDEX ix_dishes_tenant ON dishes(tenant_id);
CREATE INDEX ix_orders_tenant_created ON orders(tenant_id, created_at DESC);
```

## Seed inicial

```sql
INSERT INTO tenants (id, name, slug, created_at)
VALUES (1, 'Restaurante Piloto', 'piloto', now());
```

## Notas

- Todas las tablas llevan `tenant_id` para multi-tenant futuro.
- `dish_ingredients` modela la composición de un plato: ingredientes con cantidad + unidad.
- Se usan `TIMESTAMPTZ`, `CITEXT`, `NUMERIC`, `CHECK constraints` y enums tipados de PostgreSQL.