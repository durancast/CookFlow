# API Reference - Pedidos (Orders)

Base URL: `http://localhost:8000/api`

---

## POST /api/orders

Recibe el carrito del frontend y lo persiste como pedido. El backend **recalcula el total** usando los precios de la base de datos, ignorando cualquier total enviado por el cliente.

### Request

```
POST /api/orders
Content-Type: application/json
```

```json
{
  "table_id": 4,
  "items": [
    {
      "product_id": 1,
      "quantity": 2,
      "notes": "Sin cebolla"
    },
    {
      "product_id": 3,
      "quantity": 1,
      "notes": null
    }
  ]
}
```

### Campos del Request

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `table_id` | integer | Sí | Número de mesa (mínimo 1) |
| `items` | array | Sí | Lista de líneas del pedido (mínimo 1 item) |
| `items[].product_id` | integer | Sí | ID del producto — debe existir en `products` |
| `items[].quantity` | integer | Sí | Cantidad (mínimo 1) |
| `items[].notes` | string \| null | No | Notas especiales del plato (ej: alérgenos, sin salsa) |

> **Seguridad:** El frontend NO debe enviar precio ni total. El backend los ignora y los recalcula desde la BD.

---

### Response 201 Created

```json
{
  "id": 7,
  "table_id": 4,
  "total": "34.50",
  "status": "pending",
  "created_at": "2026-04-08T20:15:00.000000Z",
  "updated_at": "2026-04-08T20:15:00.000000Z",
  "items": [
    {
      "id": 12,
      "order_id": 7,
      "product_id": 1,
      "quantity": 2,
      "unit_price": "12.50",
      "notes": "Sin cebolla",
      "created_at": "2026-04-08T20:15:00.000000Z",
      "updated_at": "2026-04-08T20:15:00.000000Z",
      "product": {
        "id": 1,
        "name": "Paella Valenciana",
        "price": "12.50",
        "category_id": 1
      }
    },
    {
      "id": 13,
      "order_id": 7,
      "product_id": 3,
      "quantity": 1,
      "unit_price": "9.50",
      "notes": null,
      "created_at": "2026-04-08T20:15:00.000000Z",
      "updated_at": "2026-04-08T20:15:00.000000Z",
      "product": {
        "id": 3,
        "name": "Hamburguesa Clásica",
        "price": "9.50",
        "category_id": 2
      }
    }
  ]
}
```

### Campos de la Response

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | integer | ID del pedido creado |
| `table_id` | integer | Mesa que realizó el pedido |
| `total` | string (decimal) | Total recalculado por el backend en euros |
| `status` | string | Estado inicial: siempre `"pending"` |
| `items` | array | Líneas del pedido con producto embebido |
| `items[].unit_price` | string (decimal) | Precio unitario tomado de la BD en el momento del pedido |

---

### Response 422 Unprocessable Entity

Se devuelve cuando la validación falla (campo faltante, producto inexistente, etc.).

```json
{
  "message": "The table_id field is required. (and 1 more error)",
  "errors": {
    "table_id": ["The table_id field is required."],
    "items.0.product_id": ["The selected items.0.product_id is invalid."]
  }
}
```

---

## Probar con Postman

### Configuración

- **Method:** `POST`
- **URL:** `http://localhost:8000/api/orders`
- **Headers:**
  - `Content-Type: application/json`
  - `Accept: application/json`

### Body (JSON) — caso de éxito

```json
{
  "table_id": 4,
  "items": [
    { "product_id": 1, "quantity": 2, "notes": "Sin cebolla" },
    { "product_id": 3, "quantity": 1, "notes": null }
  ]
}
```

### Body (JSON) — probar validación (producto inválido)

```json
{
  "table_id": 4,
  "items": [
    { "product_id": 9999, "quantity": 1 }
  ]
}
```

Debe devolver `422` con el error `"The selected items.0.product_id is invalid."`.

### Body (JSON) — probar que el total no viene del cliente

```json
{
  "table_id": 4,
  "items": [
    { "product_id": 1, "quantity": 1, "notes": null }
  ]
}
```

El `total` en la respuesta debe ser `"12.50"` (precio real de la BD), independientemente de lo que el frontend haya enviado.

---

## Ejemplo desde el frontend (TypeScript/Astro)

```ts
async function enviarPedido(tableId: number, carrito: CartItem[]) {
  const res = await fetch('http://localhost:8000/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      table_id: tableId,
      items: carrito.map(item => ({
        product_id: item.product.id,
        quantity: item.quantity,
        notes: item.notes ?? null,
      })),
    }),
  });

  if (!res.ok) {
    const error = await res.json();
    throw new Error(error.message);
  }

  return res.json(); // Order con items embebidos
}
```

> **Nota:** No envíes `price` ni `total` desde el frontend — el backend los ignora y recalcula desde la BD por seguridad.

---

## Estructura de tablas involucradas

### `orders`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | bigint PK | Identificador del pedido |
| `table_id` | int unsigned | Número de mesa |
| `total` | decimal(8,2) | Total recalculado por backend |
| `status` | enum | `pending`, `in_progress`, `done` |
| `created_at` | timestamp | Hora del pedido |

### `order_items`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | bigint PK | Identificador de línea |
| `order_id` | bigint FK | Referencia a `orders.id` (cascade delete) |
| `product_id` | bigint FK | Referencia a `products.id` |
| `quantity` | int unsigned | Cantidad pedida |
| `unit_price` | decimal(8,2) | Precio snapshot en el momento del pedido |
| `notes` | varchar(255) | Notas opcionales del plato |

---

*Documentación generada el 2026-04-08*
