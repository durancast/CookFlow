# Project Memory
Last updated: 2026-05-06 | Session 7 | Branch: develop
Memory health: 10/10

## Project Overview
CookFlow — TFG restaurant POS/SaaS. Core flow (login → table → dishes → order → charge) works. Expanding with dashboard, kitchen screen, public menu, and UX polish per the plan in `docs/CookFlow Plan.md`.

## Where We Left Off
- **Current task:** Phase 5 not started. RBAC applied.
- **Status:** Phase 1 ✓ | Phase 2 ✓ | Phase 3 ✓ | Phase 4 ✓ | Phase 5 pending | RBAC ✓
- **Next immediate step:** Phase 5a (ConfirmDialog) + Phase 5b (print receipt)
- **Open question:** None

## Completed
- 2026-05-06 **Phase 1 (1a–1j)** — all backend foundations.
- 2026-05-06 **Phase 2 (2a–2f)** — admin panel wired + new pages.
- 2026-05-06 **Phase 3 (3a–3c)** — TPV search bar, notes modal. 3d skipped.
- 2026-05-06 **Phase 4 (4a–4c)** — kitchen display `/cocina`, public menu `/menu`, cocina nav link.
- 2026-05-06 **Extras** — NavSidebar live table list + status dots, OrderSidebar manual status buttons, selectedTable atom carries status.
- 2026-05-06 **RBAC** — CheckRole middleware, role migration (string), Astro SSR middleware, 403 page, empleados page.
- 2026-05-06 **Bugs fixed** — TPV route, unavailable products, available flag, storage symlink, comanda/cobrar flow, sent items tracking, category delete toast, product delete FK violation, KitchenDisplay auth header (401 silent fail), stale route cache (optimize:clear), import.meta SyntaxError in productos, AdminMiddleware phantom alias, multi-round cart (getActiveOrder fetches all non-paid orders), checkout bulk-paid all rounds.
- 2026-05-06 **Session 7 bugs** — Kitchen 500 (`route[login]` crash → `redirectGuestsTo(null)` + orders status made public), dashboard 500 (`strftime` → `HOUR()` for MySQL), product images 403 (storage symlink missing → `storage:link`).

## Active Work
- [ ] **Phase 5a** — `ConfirmDialog.jsx` for COBRAR + admin deletes
- [ ] **Phase 5b** — Wire print receipt after COBRAR (function exists in `printer.js`)

## Blockers
- None

## Key Decisions
| Date | Decision | Reasoning | Affects |
|------|----------|-----------|---------|
| 2026-05-06 | `orders_by_hour` uses MySQL `HOUR()` | DB is MySQL — `strftime` was SQLite-only, fixed in Session 7 | `DashboardController.php` |
| 2026-05-06 | `PATCH /orders/{id}/status` is public (no auth) | Kitchen display has no login — trusted internal screen | `api.php` |
| 2026-05-06 | `call-waiter` endpoint is public (no auth) | Public menu page calls it without a token | `api.php`, `TableController.php` |
| 2026-05-06 | `clear-waiter` requires Sanctum auth | Only waiters (logged in) should clear the alert | `api.php` |
| 2026-05-06 | CategoryController destroy returns 422 if category has products | Prevent orphaned products | `CategoryController.php` |

## Key Files
| File | Purpose |
|------|---------|
| `backend/routes/api.php` | All API routes |
| `backend/app/Http/Controllers/DashboardController.php` | Dashboard stats (revenue, orders, avg ticket, by-hour) |
| `backend/app/Http/Controllers/CategoryController.php` | Category CRUD — created Phase 1 |
| `backend/app/Http/Controllers/ReportController.php` | Daily sales report — created Phase 1 |
| `backend/app/Http/Controllers/TableController.php` | Tables + QR + checkout + call-waiter |
| `backend/app/Models/Product.php` | `available` field added Phase 1 |
| `backend/app/Models/Table.php` | `call_waiter` field added Phase 1 |
| `frontend/src/pages/admin/index.astro` | Dashboard (hardcoded — Phase 2a target) |
| `frontend/src/pages/admin/productos.astro` | Products admin (hardcoded categories — Phase 2b target) |
| `frontend/src/layouts/AdminLayout.astro` | Admin nav (missing links — Phase 2d target) |
| `frontend/src/pages/tpv/tpv.astro` | Main TPV screen |
| `frontend/src/components/react/OrderSidebar.jsx` | Cart sidebar (supports notes already) |
| `frontend/src/components/NavSidebar.astro` | TPV sidebar nav |
| `frontend/src/utils/printer.js` | Print receipt — exists but never called |
| `docs/CookFlow Plan.md` | Full feature plan with all phases |
| `docs/changelog.md` | Change log — update after each phase |

## Architecture Notes
- **Stack:** Laravel 11 backend (MySQL) + Astro frontend with React islands
- **Auth:** Laravel Sanctum (Bearer token). Public routes: `/public/menu`, `call-waiter`, `/users-list`, `PATCH /orders/{id}/status`
- **Frontend port:** 4321 (Astro default). Backend port: 8000. `APP_PUBLIC_URL=http://localhost:4321` in `.env`
- **DB:** MySQL (`cookflow` database, host 127.0.0.1:3306). Migrated from SQLite.
- **QR:** `TableController@generateQr` now reads `APP_PUBLIC_URL` env var — no longer hardcoded to `cookflow.com`
- **`orders_by_hour`** uses MySQL `HOUR(created_at)` — fixed from SQLite `strftime`

## Session Log
| Session | Date | Summary |
|---------|------|---------|
| 1 | 2026-05-06 | Phase 1 (1a–1j) complete. 2 migrations, 2 model updates, 2 new controllers, 2 enhanced controllers, routes wired, QR URL fixed. |
| 2 | 2026-05-06 | Phase 2 (2a–2f) complete. Dashboard wired, dynamic categories, disponible toggle, new categorias/ventas pages, Excel+PDF export. Multiple TPV bugs fixed (route, unavailable products, available flag, storage symlink, comanda/cobrar flow, sent items tracking). NizarAd + NizarCam users added. |
| 3 | 2026-05-06 | Phase 3 (3a–3c) complete. Search bar in TPV header, notes modal on dish click (DishCard → CustomEvent → modal → addToCart with note), cartStore addToCart accepts note param. 3d skipped (call-waiter bell needs public menu page first). |
| 4 | 2026-05-06 | Phase 4 (4a–4c) complete. Kitchen display `/cocina` (React island, 10s poll, timers, status buttons). Public menu `/menu?table={id}` (server-side fetch, call-waiter button). Cocina link in NavSidebar. Backend: OrderController::index, TableController::show, MenuController available filter, Order created_at cast, QR URL now uses table ID. |
| 5 | 2026-05-06 | NavSidebar live table list + status dots. OrderSidebar manual status buttons (Libre/En Servicio/Cobrando). selectedTable atom now carries status field. Category delete toast feedback. Product delete 422 guard (FK violation → friendly error). RBAC plan written at docs/rbac-plan.md. |
| 6 | 2026-05-06 | RBAC applied (CheckRole, role migration, Astro middleware, 403 page, empleados page). Bugs: KitchenDisplay auth header (orders never updated), stale route cache (categories/orders 404/405), import.meta SyntaxError in productos, phantom AdminMiddleware alias, multi-round cart shows all items (getActiveOrder now fetches ALL non-paid orders + merges), checkout now bulk-marks all rounds paid. |
| 7 | 2026-05-06 | Kitchen 500 fixed (redirectGuestsTo null + orders status public). Dashboard 500 fixed (strftime→HOUR for MySQL). Product images 403 fixed (storage:link). DB confirmed MySQL. |
