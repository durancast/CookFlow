# Endpoints REST propuestos (CookFlow Spring Boot)

## Auth

```http
POST   /api/auth/login
Body: { "email": "...", "password": "..." }
Response: { "token": "...", "expiresIn": 86400, "user": { "id": 1, "email": "...", "role": "ADMIN", "tenantId": 1 } }

POST   /api/auth/logout
Headers: Authorization: Bearer <token>

GET    /api/auth/me
Headers: Authorization: Bearer <token>
Response: { "id": 1, "email": "...", "name": "...", "role": "ADMIN", "tenantId": 1 }
```

## Users (admin)

```http
GET    /api/users
Headers: Authorization: Bearer <token>
Params: ?page=0&size=20
Response: [{ "id": 1, "email": "...", "name": "...", "role": "ADMIN" }, ...]

GET    /api/users/{id}
Headers: Authorization: Bearer <token>
Response: { "id": 1, "email": "...", "name": "...", "role": "ADMIN", "createdAt": "...", "updatedAt": "..." }

POST   /api/users
Headers: Authorization: Bearer <token> (ADMIN)
Body: { "email": "...", "password": "...", "name": "...", "role": "WAITER" }

PATCH  /api/users/{id}
Headers: Authorization: Bearer <token> (ADMIN)
Body: { "name": "...", "role": "MANAGER" }

DELETE /api/users/{id}
Headers: Authorization: Bearer <token> (ADMIN)
```

## Catálogo público

```http
GET    /api/public/menu
Response: {
  "categories": [
    { "id": 1, "name": "Entrantes", "slug": "entrantes", "dishes": [ ... ] }
  ]
}
```

## Dishes

```http
GET    /api/dishes
Params: ?available=true&categoryId=1
Response: [{ "id": 1, "name": "...", "price": "6.50", "available": true, "category": { "id": 1, "name": "Entrantes" } }, ...]

GET    /api/dishes/{id}
Response: { "id": 1, "name": "...", "description": "...", "price": "6.50", "available": true, "category": { ... }, "ingredients": [ { "ingredientId": 1, "quantity": 150, "unit": "G" } ] }

POST   /api/dishes
Headers: Authorization: Bearer <token> (ADMIN)
Body: { "name": "...", "description": "...", "categoryId": 1, "price": "6.50", "available": true }

PUT    /api/dishes/{id}
Headers: Authorization: Bearer <token> (ADMIN)
Body: { "name": "...", "description": "...", "categoryId": 1, "price": "7.00", "available": true }

DELETE /api/dishes/{id}
Headers: Authorization: Bearer <token> (ADMIN)

GET    /api/dishes/{id}/ingredients
Response: [{ "ingredientId": 1, "ingredientName": "Tomate", "quantity": 150, "unit": "G" }, ...]

PUT    /api/dishes/{id}/ingredients
Headers: Authorization: Bearer <token> (ADMIN)
Body: [{ "ingredientId": 1, "quantity": 200, "unit": "G" }, { "ingredientId": 2, "quantity": 100, "unit": "G" }]
```

## Ingredients (admin)

```http
GET    /api/ingredients
Response: [{ "id": 1, "name": "Tomate", "defaultUnit": "G" }, ...]

POST   /api/ingredients
Headers: Authorization: Bearer <token> (ADMIN)
Body: { "name": "...", "defaultUnit": "G" }

PUT    /api/ingredients/{id}
Headers: Authorization: Bearer <token> (ADMIN)
Body: { "name": "...", "defaultUnit": "KG" }

DELETE /api/ingredients/{id}
Headers: Authorization: Bearer <token> (ADMIN)
```

## Categories (admin)

```http
GET    /api/categories
Response: [{ "id": 1, "name": "Entrantes", "slug": "entrantes" }, ...]

POST   /api/categories
Headers: Authorization: Bearer <token> (ADMIN)
Body: { "name": "...", "slug": "..." }

PUT    /api/categories/{id}
Headers: Authorization: Bearer <token> (ADMIN)
Body: { "name": "...", "slug": "..." }

DELETE /api/categories/{id}
Headers: Authorization: Bearer <token> (ADMIN)
```

## Tables

```http
GET    /api/tables
Response: [{ "id": 1, "number": 1, "capacity": 4, "status": "FREE", "waiterCalled": false }, ...]

GET    /api/tables/{id}
Response: { "id": 1, "number": 1, "capacity": 4, "status": "OCCUPIED", "waiterCalled": true, "waiterCalledAt": "..." }

PATCH  /api/tables/{id}/status
Headers: Authorization: Bearer <token>
Body: { "status": "PENDING" }

POST   /api/tables/{id}/call-waiter
Headers: Authorization: Bearer <token>

POST   /api/tables/{id}/clear-waiter
Headers: Authorization: Bearer <token>

GET    /api/tables/{id}/active-order
Headers: Authorization: Bearer <token>
Response: { "orderId": 10, "status": "PREPARING", "total": "25.50", "items": [ ... ] }

POST   /api/tables/{id}/checkout
Headers: Authorization: Bearer <token>
```

## Orders (comandas)

```http
GET    /api/orders
Headers: Authorization: Bearer <token>
Params: ?status=PENDING&page=0&size=20
Response: [{ "id": 10, "tableNumber": 1, "status": "PREPARING", "total": "25.50", "createdAt": "..." }, ...]

POST   /api/orders
Headers: Authorization: Bearer <token>
Body: { "tableId": 1, "items": [{ "dishId": 1, "quantity": 2, "notes": "Sin sal" }, { "dishId": 3, "quantity": 1 }] }
Response: { "id": 11, "status": "PENDING", "total": "19.00", "items": [ ... ] }

PATCH  /api/orders/{id}/status
Headers: Authorization: Bearer <token>
Body: { "status": "SERVED" }
```

## Dashboard / Reports

```http
GET    /api/dashboard/stats
Headers: Authorization: Bearer <token>
Response: { "todayOrders": 45, "todayRevenue": "1250.00", "averageTicket": "27.78", ... }

GET    /api/reports/daily
Headers: Authorization: Bearer <token>
Params: ?date=2026-09-11
Response: [{ "hour": 13, "orders": 10, "revenue": "320.00" }, ...]

GET    /api/reports/range
Headers: Authorization: Bearer <token>
Params: ?from=2026-09-01&to=2026-09-10
Response: [{ "date": "2026-09-01", "orders": 120, "revenue": "3450.00" }, ...]
```

## Notas

- Todos los endpoints (salvo `/api/public/**` y `/api/auth/login`) requieren `Authorization: Bearer <JWT>`.
- Roles:
  - `ADMIN`: acceso total.
  - `MANAGER`: similar a ADMIN, salvo gestión de usuarios.
  - `WAITER`: operaciones de mesas y pedidos.
  - `KITCHEN`: solo ver pedidos en estado `PENDING`/`PREPARING`.
- Multi-tenant:
  - Hoy: todos los datos usan `tenant_id = 1`.
  - Mañana: el `tenant_id` se extrae del JWT o de un header `X-Tenant-Id`.