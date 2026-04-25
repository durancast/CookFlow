# Changelog

## Bugfixes — Status endpoint y frontend env

**Fecha:** 2026-04-25

### Bugs corregidos

**`PATCH /api/tables/{id}/status` devolvía `{}`**

El parámetro de ruta era `{id}` pero el método esperaba `Table $table`. Laravel no hace model binding cuando los nombres no coinciden e inyectaba un modelo vacío. Renombrado a `{table}` en `api.php`.

**Frontend apuntaba al puerto 8001**

`PUBLIC_BACKEND_URL` en `frontend/.env` tenía el puerto 8001 en lugar de 8000, causando `ERR_CONNECTION_REFUSED` en todas las páginas admin.

### Archivos modificados

- `backend/routes/api.php` — `{id}` → `{table}` en la ruta de status
- `frontend/.env` — puerto corregido a 8000

---

## [BACK-403] Database Seeders — Datos de prueba reales (#40)

**Fecha:** 2026-04-25

### Resumen

Seeders ampliados para poder hacer `migrate:fresh --seed` y tener el proyecto listo para probar de inmediato.

### Datos generados

| Seeder | Datos |
|--------|-------|
| `UserSeeder` | 2 usuarios: `admin@cookflow.com` / `admin` · `camarero@cookflow.com` / `camarero` |
| `TableSeeder` | 11 mesas: 1–5 (capacidad 2) · 6–10 (capacidad 4) · 11 (capacidad 8) |
| `DatabaseSeeder` | 7 categorías · **16 productos** (10 con imagen, 6 sin imagen aún) |
| `OrderSeeder` | 5 pedidos históricos con estados variados (paid, served, preparing, pending) |

### Categorías y productos

| Categoría | Productos |
|-----------|-----------|
| Entrantes | Patatas Bravas · Tequeños |
| Hamburguesas | La Jefa · The Goat · Trufada |
| Bebidas | Cerveza · Coca-Cola · Agua |
| Postres | Coulant · Cheesecake |
| Ensaladas *(nueva)* | Ensalada César · Ensalada de la Casa |
| Principales *(nueva)* | Costillas BBQ · Pollo a la Parrilla |
| Cafés *(nueva)* | Café Solo · Café con Leche |

### Pedidos de prueba

| Mesa | Estado | Items |
|------|--------|-------|
| 3 | `paid` | Patatas Bravas x2 · Cerveza x2 |
| 7 | `paid` | La Jefa x1 · Coca-Cola x1 · Coulant x1 |
| 1 | `served` | Trufada x2 · Tequeños x1 · Agua x2 |
| 5 | `preparing` | The Goat x1 · Cerveza x2 |
| 9 | `pending` | La Jefa x2 · Patatas Bravas x1 · Coca-Cola x2 |

### Archivos modificados

- `database/seeders/DatabaseSeeder.php` — 3 categorías nuevas + 6 productos nuevos + llama a OrderSeeder
- `database/seeders/OrderSeeder.php` — **creado** (idempotente: no inserta si ya hay pedidos)

### Uso

```bash
php artisan migrate:fresh --seed
```

---


## [BACK-402] Endpoint para actualizar el estado de la Mesa/Comanda (#39)

**Fecha:** 2026-04-25

### PATCH /api/tables/{id}/status

Nuevo endpoint. Permite al camarero cambiar el estado de una mesa sin enviar `number` ni `capacity`.

**Auth:** Bearer token (Sanctum)

**Request:**
```json
{ "status": "free" }
```
Valores válidos: `free` · `occupied` · `pending`

**Response 200:**
```json
{ "id": 3, "number": 5, "capacity": 4, "status": "free" }
```

**Archivos modificados:**
- `backend/app/Http/Controllers/TableController.php` — método `updateStatus` añadido
- `backend/routes/api.php` — ruta registrada con middleware `auth:sanctum`

---

### PATCH /api/orders/{id}/status

Sin cambios. El endpoint ya existía y ya aceptaba el valor `paid`.

**Auth:** Bearer token (Sanctum)

**Request:**
```json
{ "status": "paid" }
```
Valores válidos: `pending` · `preparing` · `served` · `paid`

**Response 200:**
```json
{ "id": 7, "table_id": 4, "status": "paid", "total_price": "34.50" }
```

**Flujo de estados:**
```
pending → preparing → served → paid
```

---

## [BACK-401] Endpoint Público para la Carta

**Fecha:** 2026-04-21

### GET /api/public/menu

Devuelve todas las categorías con sus productos anidados. No requiere token (accesible desde QR de mesa).

**Response 200:**
```json
[
  {
    "id": 1,
    "name": "Hamburguesas",
    "slug": "hamburguesas",
    "products": [
      { "id": 3, "name": "Hamburguesa Bacon", "description": "...", "price": "9.50", "image": "...", "category_id": 1 }
    ]
  }
]
```

**Archivos:**
- `backend/app/Http/Controllers/MenuController.php` — creado
- `backend/routes/api.php` — ruta pública registrada (`/public/menu`)
