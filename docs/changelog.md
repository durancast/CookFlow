# Changelog

## [SESSION-9] Admin & UX Polish — Dashboard, Tables Manager, Number Inputs — 2026-05-10

### Features added

#### Admin panel link in TPV nav (role-gated)
- New hidden `<a id="admin-panel-btn">` link added to the bottom section of `NavSidebar.astro`
- Becomes visible client-side only for `admin` or `manager` roles by reading the `user_role` cookie on `DOMContentLoaded`
- Uses the grid icon SVG; styled with `tpv-accent` tones to distinguish it from the other nav items
- No flash for non-admin roles — element starts hidden in HTML

#### Spinner arrows removed from all number inputs
- Default browser up/down arrows on `<input type="number">` clashed with the custom dark UI
- Applied Tailwind arbitrary CSS to every number input across the app:
  - `[appearance:textfield]` — Firefox / standards
  - `[&::-webkit-outer-spin-button]:appearance-none` — Chrome / Safari outer arrow
  - `[&::-webkit-inner-spin-button]:appearance-none` — Chrome / Safari inner arrow
- Affected inputs: cash calculator in `OrderSidebar.jsx`, price in `admin/productos.astro`, number + capacity in `admin/mesas.astro`

#### Mesas admin page — live status cards
- Full rewrite of `admin/mesas.astro` replacing the plain table list
- Status summary bar shows free / occupied / pending counts with colored dots
- Grid of table cards with: color-coded 2px border, status badge, capacity icon, large monospace number
- `call_waiter` flag renders a pulsing red 🔔 badge on the card
- "Ver TPV →" hover link appears on occupied/pending cards
- Edit / Delete buttons appear on card hover
- Auto-refresh every 30s; manual refresh button in header
- Status constants (`STATUS_BORDER`, `STATUS_BADGE`, `STATUS_DOT`) for consistent styling

#### Admin dashboard (index.astro) — improved content
- Personalized greeting with name extracted from JWT payload (`atob(token.split('.')[1])`)
- "Abrir TPV" shortcut button in the dashboard header
- 4 KPI cards (Ventas hoy, Pedidos hoy, Productos, Empleados) in a 2-col / 4-col responsive grid
- Quick links with inline SVG icons (replacing prior emoji links); added Categorías, Mesas, Ventas sections
- Live "Sala ahora" mini table grid (1/3 column, right side) — color-coded squares per status with bell indicator
- `Promise.all` parallel fetch for products, users, stats, and tables

### Files modified
- `frontend/src/components/NavSidebar.astro` — admin panel button + cookie role check
- `frontend/src/components/react/OrderSidebar.jsx` — no-spinner classes on cash input
- `frontend/src/pages/admin/productos.astro` — no-spinner classes on price input
- `frontend/src/pages/admin/mesas.astro` — full rewrite (live status cards)
- `frontend/src/pages/admin/index.astro` — full rewrite (improved dashboard)

---

## [SESSION-8] Feature Expansion — Call-Waiter Badge, Range Reports, Self-Ordering, Split Bill — 2026-05-06

### Features added

#### Feature 1: Call-waiter badge in TPV
- `NavSidebar.astro` table buttons now show a pulsing red 🔔 badge when `table.call_waiter === true`
- Button wrapper changed to `relative` so the badge (`absolute -top-1 -right-1`) positions correctly
- Module-level `prevCallWaiterMap` tracks previous poll state — fires a `showToast` only on state transition (new call, not on every poll)
- `tpv/index.astro` `selectTable()` auto-fires `POST /api/tables/{id}/clear-waiter` when the selected table has an active call — badge disappears on next 30s poll

#### Feature 2: Weekly/monthly reports
- **Backend** — `ReportController::range()` added: accepts `from`/`to` date params, queries paid orders in range, returns `revenue`, `order_count`, `avg_ticket`, `products[]`, `daily[]`; `daily[]` groups by `DATE(created_at)` for per-day breakdown
- **Backend** — `GET /api/reports/range` route registered inside `auth:sanctum` group
- **Frontend** — `admin/ventas.astro` fully redesigned:
  - Quick selector buttons: Hoy / Esta semana / Este mes / Personalizado
  - "Personalizado" reveals two `<input type="date">` fields
  - `loadReport(from, to)` — routes to `/api/reports/daily?date=` for single day, `/api/reports/range?from=&to=` for ranges
  - Chart.js bar chart (`<canvas id="revenue-chart">`) shown above the product table for multi-day ranges; hidden for single-day
  - Donut chart for top-product breakdown
  - PDF/Excel exports prepend date range to report header
- **Frontend** — `frontend/src/middleware.ts` updated: `/admin/ventas` guard extended from `['admin']` to `['admin', 'manager']`
- **Installed:** `chart.js` via `pnpm add chart.js`

#### Feature 3: Customer self-ordering from QR menu
- `menu/index.astro` — vanilla JS cart state `const cart = {}` keyed by product ID
- Each product card gains `+` / `−` buttons; quantity badge overlays the card when `qty > 0`
- Sticky bottom cart bar (hidden when cart empty) shows item count + total; "Enviar pedido →" button
- Confirmation modal lists cart contents with totals; "Confirmar" button POSTs to `/api/orders`
- Success state replaces cart bar with "✓ Pedido enviado — el equipo lo está preparando"; cart cleared
- Error state shows inline error, keeps cart intact for retry
- `tableId` sourced from URL query param (already available server-side)

#### Feature 4: Split bill
- `OrderSidebar.jsx` — new state: `splitMode`, `splitSide` `{ itemKey: 'A'|'B' }`, `paidTickets` (Set), `splitPayingTicket` (`'A'|'B'|null`)
- "Dividir cuenta" button rendered in the main action bar alongside COBRAR (only when sent items exist)
- Split UI activates when `splitMode === true`: lists sent items, each with an A/B pill toggle
- Live "Ticket A: €X" and "Ticket B: €Y" totals update on every assignment change
- "Pagar A" / "Pagar B" route into the existing cash/card flow for that ticket's total; on complete calls `printCustomerReceipt()` and marks the ticket in `paidTickets`
- Both tickets paid → "Cerrar mesa" → calls existing `handleCheckout()` which marks all paid and frees the table
- "Cancelar" exits split mode and resets all split state; existing payment flow (`paymentMode`, `cashMode`, `handleCheckout`) unchanged

### Files modified
- `backend/app/Http/Controllers/ReportController.php` — `range()` method added
- `backend/routes/api.php` — `GET /reports/range` registered
- `frontend/src/components/NavSidebar.astro` — badge, toast, `prevCallWaiterMap`
- `frontend/src/pages/tpv/index.astro` — auto-clear `call_waiter` in `selectTable()`
- `frontend/src/pages/admin/ventas.astro` — full redesign (quick selectors, chart.js, range API)
- `frontend/src/middleware.ts` — `/admin/ventas` opens to `manager` role
- `frontend/src/pages/menu/index.astro` — cart state, +/− buttons, sticky bar, confirm modal, order submission
- `frontend/src/components/react/OrderSidebar.jsx` — split bill state and UI
- `frontend/package.json` — `chart.js` added

---

## [SESSION-7] Bug Fixes — Kitchen 500, dashboard MySQL, storage symlink — 2026-05-06

### Bugs fixed

#### Kitchen display — PATCH /orders/{id}/status returned 500 (Route [login] not defined)
- `auth:sanctum` middleware rejected unauthenticated requests by trying to call `route('login')`, which doesn't exist in this API-only backend → `RouteNotFoundException` → 500
- Root cause 1: `bootstrap/app.php` had no `redirectGuestsTo` override, so Laravel's `Authenticate` middleware always tried to redirect to the `login` named route
- Root cause 2: The kitchen display (`/cocina`) is an always-on screen with no login — it has no token by design
- Fix 1: Added `$middleware->redirectGuestsTo(fn () => null)` in `bootstrap/app.php` — any protected API route now returns a clean 401 JSON instead of crashing
- Fix 2: Removed `->middleware('auth:sanctum')` from `PATCH /api/orders/{order}/status` — kitchen is a trusted internal screen, no auth needed
- Fix 3: Removed the `Authorization` header from `KitchenDisplay.jsx::updateStatus()` — no longer needed

#### Dashboard stats — 500 on `GET /api/dashboard/stats`
- `DashboardController::stats()` used `strftime("%H", created_at)` — SQLite syntax
- DB had been migrated to MySQL (`cookflow` database, 127.0.0.1:3306) → `FUNCTION cookflow.strftime does not exist`
- Fix: Replaced `strftime("%H", created_at)` with `HOUR(created_at)` in both `selectRaw` and `groupByRaw`

#### Product images — 403 Forbidden on all `/storage/products/*.webp`
- `public/storage` symlink did not exist — git does not track symlinks created by `php artisan storage:link`
- Fix: Ran `php artisan storage:link` → `public/storage → storage/app/public` symlink created
- Note: This must be re-run after every fresh clone or `migrate:fresh`

### Files modified
- `backend/bootstrap/app.php` — `redirectGuestsTo(fn () => null)` added
- `backend/routes/api.php` — `auth:sanctum` removed from `PATCH /orders/{order}/status`
- `backend/app/Http/Controllers/DashboardController.php` — `strftime` → `HOUR()` for MySQL
- `frontend/src/components/react/KitchenDisplay.jsx` — `Authorization` header removed from `updateStatus`

---

## [SESSION-6] Bug Fixes — Multi-round orders, kitchen auth, route cache — 2026-05-06

### Bugs fixed

#### KitchenDisplay — "Listo ✓" silently did nothing
- `PATCH /api/orders/{order}/status` is protected by `auth:sanctum`
- `updateStatus()` in `KitchenDisplay.jsx` was sending the request with no `Authorization` header → 401 → order status never changed, card stayed on screen forever
- Fix: Added `'Authorization': \`Bearer ${localStorage.getItem('auth_token')}\`` to the fetch headers

#### Stale Laravel route cache — categories 404, orders 405
- `php artisan optimize` had been run previously, generating a `CompiledRouteCollection`
- Routes added after that (RBAC routes, categories, `active-order`) were invisible to the router
- Symptom: `GET /api/categories` → `NotFoundHttpException` from `CompiledRouteCollection`; `GET /api/orders` → 405
- Fix: `php artisan optimize:clear` — cleared routes, config, compiled, events, views caches

#### `import.meta` SyntaxError on productos page
- `<script define:vars={{ canEdit }}>` makes the block inline (non-ES module) — `import.meta` is unavailable
- Line `const backendUrl = import.meta.env.PUBLIC_BACKEND_URL || '...'` caused `Uncaught SyntaxError: Cannot use 'import.meta' outside a module`
- The variable was also completely unused (all fetches use relative `/api/` paths)
- Fix: Removed the line entirely from `admin/productos.astro`

#### AdminMiddleware phantom alias
- `bootstrap/app.php` had `'admin' => \App\Http\Middleware\AdminMiddleware::class`
- The file `AdminMiddleware.php` does not exist — latent fatal if any route ever used `middleware('admin')`
- No routes referenced it, but it was a ticking time bomb
- Fix: Removed the `'admin'` alias; kept only `'role' => CheckRole::class`

#### Cart shows only last round of orders on occupied table re-entry
- `Table::activeOrder()` is a `HasOne` with `latestOfMany()` — always returns the single most recent non-paid order
- `TableController::getActiveOrder()` fetched only that one order's items
- Every new kitchen round creates a new `Order` record → returning to an occupied table showed only the last batch sent
- Fix: `getActiveOrder()` now queries ALL non-paid orders for the table (`$table->orders()->where('status', '!=', 'paid')->get()`), flatMaps all their items, and merges by `(product_id . '|' . notes)` key — quantities are summed for identical items, different notes stay separate

#### Checkout only marked last order as paid
- `checkout()` was using `$table->activeOrder()->first()` → updated only the newest non-paid order to `paid`
- Previous rounds remained with their original status — table was "freed" but orders were still non-paid in the DB
- Fix: `$table->orders()->where('status', '!=', 'paid')->update(['status' => 'paid'])` — bulk-updates every round in a single query

### RBAC applied
- `CheckRole` middleware created at `backend/app/Http/Middleware/CheckRole.php`
- Migration `2026_05_06_100001_change_role_to_string.php` — changes `role` column from integer to string enum (`admin`, `manager`, `waiter`, `cook`)
- `bootstrap/app.php` — `role` alias registered
- `frontend/src/middleware.ts` — Astro SSR middleware guards pages by cookie `user_role`
- `frontend/src/pages/403.astro` — Forbidden page for unauthorized access
- `frontend/src/pages/admin/empleados.astro` — Employee management page (admin-only)
- `UserSeeder.php` — roles updated to string values

### Files modified
- `backend/bootstrap/app.php` — phantom `admin` alias removed, `role` alias confirmed
- `backend/routes/api.php` — RBAC middleware applied to relevant route groups
- `backend/database/seeders/UserSeeder.php` — string roles
- `backend/app/Http/Controllers/TableController.php` — `getActiveOrder()` multi-round merge + `checkout()` bulk-paid fix
- `backend/app/Http/Middleware/CheckRole.php` — **created**
- `backend/database/migrations/2026_05_06_100001_change_role_to_string.php` — **created**
- `frontend/src/components/react/KitchenDisplay.jsx` — auth header added to `updateStatus`
- `frontend/src/pages/admin/productos.astro` — `import.meta` line removed
- `frontend/src/middleware.ts` — **created**
- `frontend/src/pages/403.astro` — **created**
- `frontend/src/pages/admin/empleados.astro` — **created**

---

## [PLAN-PHASE4] Kitchen Display & Public Menu — 2026-05-06

### Phase 4a — Kitchen display `/cocina`
- New `KitchenDisplay.jsx` React island — polls `GET /api/orders?status=pending,preparing` every 10s
- Per-card elapsed timer: green < 5 min, yellow 5–10 min, red > 10 min
- "Preparando" button (pending → preparing), "Listo ✓" button (preparing → served)
- Order card shows table number, items + notes, status badge
- New `cocina/index.astro` shell page — no auth guard (kitchen staff don't need login)

### Phase 4b — Public customer menu `/menu?table={id}`
- New `menu/index.astro` — fetches `/api/public/menu` server-side + table details via `GET /api/tables/{id}`
- Products grouped by category with image, name, description, price
- Sticky category nav pills for quick scroll
- "Llamar al camarero" floating button — calls `POST /api/tables/{id}/call-waiter`, shows confirmation state for 5s

### Phase 4c — Cocina link in NavSidebar
- Chef hat icon added above the table list in `NavSidebar.astro`

### Backend changes

#### `OrderController::index` (new)
- `GET /api/orders?status=pending,preparing` — returns orders with items.product + table, filtered by comma-separated status list
- No auth required (kitchen display is always-on)

#### `TableController::show` (new)
- `GET /api/tables/{id}` — returns single table (public menu needs number for display)

#### `TableController::generateQr` — QR URL now uses `$table->id`
- Was: `?table={number}` → Now: `?table={id}`
- Public menu uses table ID directly for call-waiter + table lookup

#### `MenuController::index`
- Products now filtered `where available = true`
- Categories with zero available products are removed from response

#### `Order` model
- Added `protected $casts = ['created_at' => 'datetime']` — ensures ISO 8601 in JSON for kitchen timer

### Files modified
- `backend/app/Models/Order.php` — created_at cast
- `backend/app/Http/Controllers/OrderController.php` — index() added
- `backend/app/Http/Controllers/MenuController.php` — available filter + empty category filter
- `backend/app/Http/Controllers/TableController.php` — show() added, QR URL → id
- `backend/routes/api.php` — `GET /api/orders` registered
- `frontend/src/components/react/KitchenDisplay.jsx` — **created**
- `frontend/src/pages/cocina/index.astro` — **created**
- `frontend/src/pages/menu/index.astro` — **created**
- `frontend/src/components/NavSidebar.astro` — Cocina link added

---

## [PLAN-PHASE3] TPV Improvements — 2026-05-06

### Phase 3a — Product search bar
- Added search input to TPV header (next to category pills, before logout button)
- Category filter refactored into shared `applyFilters()` function — search and category filters combine correctly
- Search filters by `h3` text content (product name), case-insensitive, live on `input` event

### Phase 3b — Hide unavailable products *(already done in Phase 2 bugfix)*
- `tpv/index.astro` SSR filter `allProducts.filter(p => p.available !== false)` was in place — no change needed

### Phase 3c — Quick notes modal on dish click
- `DishCard.astro` click handler no longer calls `addToCart` directly — dispatches `CustomEvent('dish-add', { detail: product, bubbles: true })` instead
- New notes modal added to `tpv/index.astro`: shows product name, optional text input, Cancelar / Añadir buttons
- Enter key confirms, Escape cancels
- `cartStore.js` `addToCart(product, note = '')` now accepts optional note — if note present, always creates new line even if same product exists unsent

### Phase 3d — Call-waiter bell on table cards *(skipped)*
- Deferred: no consumer of `call_waiter` flag exists until Phase 4b (public menu page) is built
- Backend infrastructure already in place (column + endpoints) — trivial to add later

### Files modified
- `frontend/src/pages/tpv/index.astro` — search input HTML, notes modal HTML, refactored filter JS, addToCart import, modal JS
- `frontend/src/components/DishCard.astro` — click dispatches `dish-add` CustomEvent instead of direct addToCart
- `frontend/src/store/cartStore.js` — `addToCart` accepts optional `note` param

---

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
