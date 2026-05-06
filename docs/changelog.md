# Changelog

## [PLAN-PHASE2] Admin Panel + TPV Fixes — 2026-05-06

### Phase 2 — Admin panel wired to real API

#### 2a — Dashboard wired to real API
- `admin/index.astro` now calls `GET /api/dashboard/stats` for `revenue_today` and `orders_today`
- Fixed wrong closing tag `</Layout>` → `</AdminLayout>`
- Added missing `backendUrl` const to the script block

#### 2b — Dynamic category dropdown in products form
- `admin/productos.astro` category `<select>` now fetches from `GET /api/categories`
- Removed 4 hardcoded `<option>` tags

#### 2c — "Disponible" toggle in products table
- New toggle switch column in `admin/productos.astro`
- Calls `PATCH /api/products/{id}` with `{ available }` — updates in place without reload
- **Bug fixed:** `available` was missing from `ProductController@update` validation rules — Laravel silently stripped it on every PATCH request

#### 2d — Admin sidebar links
- Added Categorías, Ventas, Cocina links to `AdminLayout.astro` sidebar

#### 2e — New page `/admin/categorias`
- Full CRUD: list, create, edit, delete
- Auto-generates slug from name client-side
- Delete blocked (button disabled + tooltip) if category has products

#### 2f — New page `/admin/ventas`
- Date picker (defaults to today), calls `GET /api/reports/daily?date=`
- 3 stat cards: ingresos, pedidos, ticket medio
- Product breakdown table
- Export Excel via `xlsx` library (client-side)
- Export PDF via `jspdf` + `jspdf-autotable` (client-side)
- Installed: `xlsx@0.18.5`, `jspdf@4.2.1`, `jspdf-autotable@5.0.7`

### Bugs fixed

#### TPV route broken (`/tpv/tpv`)
- `frontend/src/pages/tpv/tpv.astro` renamed to `tpv/index.astro` — route is now `/tpv`
- Login redirect fixed: `index.astro` was sending waiters to `/tpv/tpv` → now `/tpv`

#### Unavailable products showing in TPV
- `tpv/index.astro` now filters `allProducts.filter(p => p.available !== false)` before rendering
- Filter runs server-side at page load — unavailable products never reach the DOM

#### `available` flag not persisting
- `ProductController@update` validation was missing `'available' => ['sometimes', 'boolean']`
- Laravel discarded the field on every PATCH — toggle appeared to work but reset on refresh

#### Storage symlink missing — images not loading
- `php artisan storage:link` needed to create `public/storage → storage/app/public`
- Astro proxy for `/storage` was already correct in `astro.config.mjs`

#### OrderSidebar — comanda/cobrar flow broken
- `handleSendOrder` was calling `clearOrder()` (clears cart + deselects table) → kicked waiter back to table modal after every comanda sent
- Fixed: `handleSendOrder` now calls `clearCart()` — table stays selected
- Table modal now calls `loadTableSelection()` on open (was showing stale state)
- `loadTableSelection` hoisted to outer script scope so both `selectedTable.subscribe` and `setInterval` share the same function

#### Sent items tracking in cart
- Cart items now have a `sent: boolean` field
- `handleSendOrder` sends only `items.filter(i => !i.sent)` — already-sent items are not re-sent
- After send: items stay in cart, marked `sent: true`, shown with ✓ and dimmed styling
- COBRAR button grayed out + toast if any unsent items exist — prevents charging for unordered items
- Re-entering an occupied/pending table: loaded items from active-order come in as `sent: true`
- Adding same dish after it was sent: creates a new unsent line instead of incrementing the sent one

### New users added (UserSeeder)
| Name | Email | Password | Role |
|------|-------|----------|------|
| NizarAd | nizarad@cookflow.com | `admin` | admin |
| NizarCam | nizarcam@cookflow.com | `NizarCam` | waiter |

### Files modified
- `backend/app/Http/Controllers/ProductController.php` — `available` added to update validation
- `backend/database/seeders/UserSeeder.php` — NizarAd + NizarCam added
- `frontend/src/layouts/AdminLayout.astro` — Categorías, Ventas, Cocina nav links
- `frontend/src/pages/admin/index.astro` — real API call, closing tag fix, backendUrl fix
- `frontend/src/pages/admin/productos.astro` — dynamic categories, disponible toggle
- `frontend/src/pages/admin/categorias.astro` — **created**
- `frontend/src/pages/admin/ventas.astro` — **created**
- `frontend/src/pages/tpv/index.astro` — renamed from tpv.astro, unavailable filter, loadTableSelection hoisted, active-order items marked sent
- `frontend/src/pages/index.astro` — login redirect `/tpv/tpv` → `/tpv`
- `frontend/src/components/react/OrderSidebar.jsx` — sent items flow, COBRAR guard
- `frontend/src/store/cartStore.js` — `sent` flag, `markItemsAsSent()`, addToCart respects sent state
- `frontend/package.json` — xlsx, jspdf, jspdf-autotable added

---

## [PLAN-PHASE1] Backend Foundations — Columnas, Controladores y Rutas

**Fecha:** 2026-05-06

### Resumen

Implementación completa de la Fase 1 del plan de expansión. Añade soporte para disponibilidad de productos, llamada al camarero, gestión de categorías, reportes diarios y mejoras al dashboard.

### Cambios

#### Migraciones nuevas

| Archivo | Qué hace |
|---------|----------|
| `2026_05_06_000001_add_available_to_products_table.php` | Añade `available BOOLEAN DEFAULT true` a `products` |
| `2026_05_06_000002_add_call_waiter_to_tables_table.php` | Añade `call_waiter BOOLEAN DEFAULT false` a `tables` |

#### Modelos actualizados

- **`Product`** — `available` añadido a `$fillable` + cast `boolean`
- **`Table`** — `call_waiter` añadido a `$fillable` + cast `boolean`

#### Controladores nuevos

**`CategoryController`** — CRUD completo para categorías:
- `GET /api/categories` — lista con conteo de productos
- `POST /api/categories` — crea categoría, auto-genera `slug`
- `PATCH /api/categories/{id}` — edita nombre y slug
- `DELETE /api/categories/{id}` — devuelve 422 si la categoría tiene productos

**`ReportController`** — informe de ventas diarias:
- `GET /api/reports/daily?date=YYYY-MM-DD` — devuelve `revenue`, `order_count`, `avg_ticket` y desglose por producto

#### Controladores modificados

**`DashboardController`** — `GET /api/dashboard/stats` ahora incluye:
- `orders_today` — pedidos pagados hoy
- `avg_ticket` — ticket medio del día
- `orders_by_hour` — array `[{ hour, count }]` (usa `strftime` de SQLite — cambiar a `HOUR()` si se migra a MySQL)

**`TableController`** — dos endpoints nuevos:
- `POST /api/tables/{table}/call-waiter` — escribe `call_waiter = true` (público, sin auth)
- `POST /api/tables/{table}/clear-waiter` — escribe `call_waiter = false` (requiere Sanctum)
- QR URL corregida: ya no es hardcoded a `cookflow.com`, lee `APP_PUBLIC_URL` del `.env`

#### Entorno

- `.env` — añadida `APP_PUBLIC_URL=http://localhost:4321`

### Archivos modificados

- `database/migrations/2026_05_06_000001_add_available_to_products_table.php` — **creado**
- `database/migrations/2026_05_06_000002_add_call_waiter_to_tables_table.php` — **creado**
- `app/Models/Product.php` — `$fillable` + `$casts`
- `app/Models/Table.php` — `$fillable` + `$casts`
- `app/Http/Controllers/CategoryController.php` — **creado**
- `app/Http/Controllers/ReportController.php` — **creado**
- `app/Http/Controllers/DashboardController.php` — stats ampliadas
- `app/Http/Controllers/TableController.php` — `callWaiter`, `clearWaiter`, QR URL fix
- `routes/api.php` — rutas de categorías, reports y call-waiter registradas
- `.env` — `APP_PUBLIC_URL` añadida

---

## [BACK-502] Mesa pasa a free automáticamente al pagar

**Fecha:** 2026-04-25

### Cambio

Cuando `PATCH /api/orders/{id}/status` recibe `{ "status": "paid" }`, el controlador ahora actualiza la mesa asociada a `free` de forma automática.

```php
if ($validated['status'] === 'paid') {
    Table::where('id', $order->table_id)->update(['status' => 'free']);
}
```

No se usó Observer — la lógica es puntual y el controlador es el lugar más directo para el alcance del TFG.

### Archivos modificados

- `app/Http/Controllers/OrderController.php` — import `Table` + bloque `if paid` en `updateStatus()`

---

## [BACK-503] CORS de producción + optimización

**Fecha:** 2026-04-25

### Cambios

**CORS restrictivo por entorno**

`config/cors.php` ya no tiene la URL del frontend hardcodeada. Ahora lee la variable de entorno `FRONTEND_URL`, con fallback a `http://localhost:4321` para desarrollo local.

```php
'allowed_origins' => [env('FRONTEND_URL', 'http://localhost:4321')],
```

En producción solo hay que añadir al `.env` del backend:

```
FRONTEND_URL=https://<dominio-de-alejandro>.vercel.app
```

**`php artisan optimize`**

Caché generada para config, eventos, rutas y vistas lista para despliegue.

### Archivos modificados

- `config/cors.php` — `allowed_origins` lee `FRONTEND_URL` del entorno
- `.env` — añadida `FRONTEND_URL=http://localhost:4321`
- `.env.example` — añadida `FRONTEND_URL=http://localhost:4321`

---

## [BACK-501] Endpoint de estadísticas del dashboard

**Fecha:** 2026-04-25

### GET /api/dashboard/stats

Nuevo endpoint protegido con Sanctum. Devuelve tres métricas en tiempo real para el panel de administración.

**Auth:** Bearer token (Sanctum)

**Response 200:**
```json
{
  "revenue_today": 39.9,
  "top_product": { "id": 6, "name": "Cerveza", "total_quantity": "4" },
  "occupied_tables": 1
}
```

| Campo | Descripción |
|-------|-------------|
| `revenue_today` | Suma de `total_price` de comandas con `status = paid` creadas hoy |
| `top_product` | Producto con mayor `SUM(quantity)` en `order_items` (histórico) |
| `occupied_tables` | Conteo de mesas con `status = occupied` en tiempo real |

### Archivos modificados

- `database/migrations/2026_04_25_155013_add_created_at_to_orders_table.php` — añade `created_at` a `orders` con `DEFAULT CURRENT_TIMESTAMP`
- `app/Http/Controllers/DashboardController.php` — **creado**
- `routes/api.php` — ruta registrada dentro del grupo `auth:sanctum`
- `bootstrap/app.php` — `shouldRenderJsonWhen` para que rutas `/api/*` devuelvan 401 JSON en lugar de redirigir al login web

---

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
