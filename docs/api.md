# API Reference - CookFlow

Base URL: `http://localhost:8000/api`

---

Actualizar el archivo docs/api.md con ejemplos reales del JSON que devuelve el catálogo para que Alejandro sepa cómo mapearlos.## GET /api/products

Devuelve la lista completa de platos del catálogo, cada uno con su categoría incluida.

### Request

```
GET /api/products
```

No requiere parámetros ni autenticación.

### Response

**Status:** `200 OK`
**Content-Type:** `application/json`

```json
[
  {
    "id": 1,
    "category_id": 1,
    "name": "Paella Valenciana",
    "description": "Paella tradicional con pollo, conejo y verduras de temporada.",
    "price": "12.50",
    "created_at": "2026-03-12T21:14:42.000000Z",
    "updated_at": "2026-03-12T21:14:42.000000Z",
    "category": {
      "id": 1,
      "name": "Arroces",
      "created_at": "2026-03-12T21:14:42.000000Z",
      "updated_at": "2026-03-12T21:14:42.000000Z"
    }
  },
  {
    "id": 2,
    "category_id": 1,
    "name": "Arroz Negro",
    "description": "Arroz con tinta de calamar y alioli casero.",
    "price": "13.00",
    "created_at": "2026-03-12T21:14:42.000000Z",
    "updated_at": "2026-03-12T21:14:42.000000Z",
    "category": {
      "id": 1,
      "name": "Arroces",
      "created_at": "2026-03-12T21:14:42.000000Z",
      "updated_at": "2026-03-12T21:14:42.000000Z"
    }
  },
  {
    "id": 3,
    "category_id": 2,
    "name": "Hamburguesa Clásica",
    "description": "Carne de ternera, lechuga, tomate y cheddar.",
    "price": "9.50",
    "created_at": "2026-03-12T21:14:42.000000Z",
    "updated_at": "2026-03-12T21:14:42.000000Z",
    "category": {
      "id": 2,
      "name": "Hamburguesas",
      "created_at": "2026-03-12T21:14:42.000000Z",
      "updated_at": "2026-03-12T21:14:42.000000Z"
    }
  }
]
```

### Campos del objeto `product`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | integer | Identificador único del producto |
| `category_id` | integer | FK de la categoría |
| `name` | string | Nombre del plato |
| `description` | string \| null | Descripción del plato |
| `price` | string (decimal) | Precio en euros, formato `"12.50"` |
| `category` | object | Categoría anidada (Eager Loading) |

### Campos del objeto `category`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | integer | Identificador único de la categoría |
| `name` | string | Nombre de la categoría |

---

## Notas para el frontend (Alejandro)

- **`price` llega como string**, no como número. Usar `parseFloat(product.price)` si necesitas operar con él.
- La **categoría ya viene embebida** en cada producto, no hace falta una segunda petición a `/api/categories`.
- Para agrupar platos por categoría en la UI, puedes reducir el array por `category.id` o `category.name`.

Ejemplo en JavaScript/TypeScript:

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

## Campos pendientes (próximas tareas)

Según el diseño de la BBDD (`docs/db.md`), estos campos se añadirán cuando estén implementados:

| Campo | Tabla | Descripción |
|---|---|---|
| `slug` | `categories` | Versión URL-friendly del nombre (ej: `"hamburguesas"`) |
| `image` | `products` | URL o ruta de la imagen del plato |

---

*Documentación generada el 2026-03-12*
