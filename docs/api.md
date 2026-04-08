# API Reference - CookFlow

Base URL: `http://localhost:8000/api`

---

## GET /api/products

Devuelve la lista completa de platos del catálogo, cada uno con su categoría incluida. Ruta pública, no requiere autenticación.

```http
GET /api/products
```

`200 OK`

```json
[
  {
    "id": 1,
    "category_id": 1,
    "name": "Patatas Bravas",
    "description": "Patatas crujientes acompañadas de salsa brava.",
    "price": "6.50",
    "image": "patatas_bravas.webp",
    "category": {
      "id": 1,
      "name": "Entrantes",
      "slug": "entrantes"
    }
  }
]
```

Campos del objeto `product`:

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `id` | integer | Identificador único del producto |
| `category_id` | integer | FK de la categoría |
| `name` | string | Nombre del plato |
| `description` | string \| null | Descripción del plato |
| `price` | string (decimal) | Precio en euros, formato `"6.50"` |
| `image` | string \| null | Nombre del archivo de imagen |
| `category` | object | Categoría anidada (eager loading) |

Notas para el frontend:

- `price` llega como string. Usar `parseFloat(product.price)` si necesitas operar con él.
- La categoría ya viene embebida, no hace falta una segunda petición a `/api/categories`.

```ts
const res = await fetch('http://localhost:8000/api/products');
const products = await res.json();

// Agrupar por categoría
const byCategory = products.reduce((acc, product) => {
  const key = product.category.name;
  if (!acc[key]) acc[key] = [];
  acc[key].push(product);
  return acc;
}, {});
```

---

## POST /api/products

Crea un nuevo producto. Requiere token de admin.

```http
POST /api/products
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "name": "Ensalada César",
  "description": "Lechuga romana, pollo a la plancha, parmesano y croutons.",
  "price": 9.50,
  "category_id": 1,
  "image": "ensalada_cesar.webp"
}
```

Campos del body:

| Campo | Tipo | Requerido | Descripción |
| --- | --- | --- | --- |
| `name` | string | Sí | Nombre del plato (máx. 255 caracteres) |
| `description` | string \| null | No | Descripción del plato |
| `price` | number | Sí | Precio en euros, mayor o igual a 0 |
| `category_id` | integer | Sí | Debe existir en la tabla `categories` |
| `image` | string \| null | No | Nombre del archivo de imagen |

`201 Created`

```json
{
  "id": 11,
  "name": "Ensalada César",
  "description": "Lechuga romana, pollo a la plancha, parmesano y croutons.",
  "price": "9.50",
  "image": "ensalada_cesar.webp",
  "category_id": 1,
  "category": {
    "id": 1,
    "name": "Entrantes",
    "slug": "entrantes"
  }
}
```

`422 Unprocessable Entity` (validación fallida)

```json
{
  "message": "The price field must be a number.",
  "errors": {
    "price": ["The price field must be a number."]
  }
}
```

---

## PUT /api/products/{id}

Actualiza un producto existente. Solo se envían los campos que cambian. Requiere token de admin.

```http
PUT /api/products/11
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "price": 10.00
}
```

`200 OK` — devuelve el producto completo con la categoría anidada.

`404 Not Found` — si el producto no existe.

---

## DELETE /api/products/{id}

Elimina un producto. Requiere token de admin.

```http
DELETE /api/products/11
Authorization: Bearer {token}
```

`204 No Content` — sin cuerpo de respuesta.

`404 Not Found` — si el producto no existe.

---

## GET /api/tables

Devuelve todas las mesas físicas del restaurante ordenadas por número, cada una con su comanda activa embebida. Ruta pública, no requiere autenticación.

```http
GET /api/tables
```

`200 OK`

```json
[
  {
    "id": 1,
    "number": 1,
    "capacity": 4,
    "status": "free",
    "active_order": null
  },
  {
    "id": 2,
    "number": 2,
    "capacity": 2,
    "status": "occupied",
    "active_order": {
      "id": 5,
      "table_id": 2,
      "status": "preparing",
      "total_price": "21.00"
    }
  }
]
```

Campos del objeto `table`:

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `id` | integer | Identificador único |
| `number` | integer | Número visible de la mesa (único) |
| `capacity` | integer | Número de comensales |
| `status` | string | Estado de la mesa: `free`, `occupied`, `pending` |
| `active_order` | object \| null | Comanda activa (status ≠ `paid`). `null` si la mesa está libre |

Notas para el frontend:

- Usar `active_order !== null` para determinar si la mesa está ocupada en el mapa visual.
- El campo `active_order.status` permite diferenciar si la cocina está preparando (`preparing`) o si está pendiente de cobro (`ready`).

---

## POST /api/tables

Crea una nueva mesa física. Requiere token de admin.

```http
POST /api/tables
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "number": 5,
  "capacity": 6,
  "status": "free"
}
```

Campos del body:

| Campo | Tipo | Requerido | Descripción |
| --- | --- | --- | --- |
| `number` | integer | Sí | Número de mesa, único, mínimo 1 |
| `capacity` | integer | Sí | Comensales, mínimo 1 |
| `status` | string | No | `free` (defecto), `occupied` o `pending` |

`201 Created`

```json
{
  "id": 5,
  "number": 5,
  "capacity": 6,
  "status": "free"
}
```

`422 Unprocessable Entity` (número de mesa ya existe)

```json
{
  "message": "The number has already been taken.",
  "errors": {
    "number": ["The number has already been taken."]
  }
}
```

---

## DELETE /api/tables/{id}

Elimina una mesa. Requiere token de admin.

```http
DELETE /api/tables/5
Authorization: Bearer {token}
```

`204 No Content` — sin cuerpo de respuesta.

`404 Not Found` — si la mesa no existe.

---

## Resumen de rutas

| Endpoint | Método | Autenticación | Rol |
| --- | --- | --- | --- |
| `/api/products` | GET | No | — |
| `/api/products` | POST | Sí | admin |
| `/api/products/{id}` | PUT | Sí | admin |
| `/api/products/{id}` | DELETE | Sí | admin |
| `/api/tables` | GET | No | — |
| `/api/tables` | POST | Sí | admin |
| `/api/tables/{id}` | DELETE | Sí | admin |
| `/api/orders` | POST | No | — |
| `/api/orders/{id}/status` | PATCH | No | cocina |
| `/api/login` | POST | No | — |
| `/api/logout` | POST | Sí | cualquiera |
| `/api/me` | GET | Sí | cualquiera |

---

Documentación actualizada el 2026-03-19
