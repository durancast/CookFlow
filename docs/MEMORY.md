# Project Memory
Last updated: 2026-05-06 | Session 1 | Branch: develop
Memory health: 9/10

## Project Overview
CookFlow — TFG restaurant POS/SaaS. Core flow (login → table → dishes → order → charge) works. Expanding with dashboard, kitchen screen, public menu, and UX polish per the plan in `docs/CookFlow Plan.md`.

## Where We Left Off
- **Current task:** Phase 5 not started. RBAC plan written, not applied yet.
- **Status:** Phase 1 ✓ | Phase 2 ✓ | Phase 3 ✓ | Phase 4 ✓ | Phase 5 pending | RBAC planned
- **Next immediate step:** Apply RBAC plan (`docs/rbac-plan.md`) OR do Phase 5
- **Open question:** None

## Completed
- 2026-05-06 **Phase 1 (1a–1j)** — all backend foundations.
- 2026-05-06 **Phase 2 (2a–2f)** — admin panel wired + new pages.
- 2026-05-06 **Phase 3 (3a–3c)** — TPV search bar, notes modal. 3d skipped.
- 2026-05-06 **Phase 4 (4a–4c)** — kitchen display `/cocina`, public menu `/menu`, cocina nav link.
- 2026-05-06 **Extras** — NavSidebar live table list + status dots, OrderSidebar manual status buttons, selectedTable atom carries status.
- 2026-05-06 **Bugs fixed** — TPV route, unavailable products, available flag, storage symlink, comanda/cobrar flow, sent items tracking, category delete toast, product delete FK violation (returns 422 with message instead of 500).

## Active Work
- [ ] **RBAC** — full plan at `docs/rbac-plan.md`. 4 roles: admin/manager/waiter/cook. SSR + middleware. See plan for all files.
- [ ] **Phase 5a** — `ConfirmDialog.jsx` for COBRAR + admin deletes
- [ ] **Phase 5b** — Wire print receipt after COBRAR (function exists in `printer.js`)

## Blockers
- None

## Key Decisions
| Date | Decision | Reasoning | Affects |
|------|----------|-----------|---------|
| 2026-05-06 | `orders_by_hour` uses SQLite `strftime` | DB is SQLite locally; switch to `HOUR()` if migrating to MySQL | `DashboardController.php` |
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
- **Stack:** Laravel 11 backend (SQLite locally) + Astro frontend with React islands
- **Auth:** Laravel Sanctum (Bearer token). Public routes: `/public/menu`, `call-waiter`, `/users-list`
- **Frontend port:** 4321 (Astro default). Backend port: 8000. `APP_PUBLIC_URL=http://localhost:4321` in `.env`
- **DB:** SQLite for local dev (`backend/database/database.sqlite`). Phase 1 ran 2 new migrations: `available` on products, `call_waiter` on tables
- **QR:** `TableController@generateQr` now reads `APP_PUBLIC_URL` env var — no longer hardcoded to `cookflow.com`
- **`orders_by_hour`** uses SQLite `strftime("%H", created_at)` — must change to `HOUR()` for MySQL

## Session Log
| Session | Date | Summary |
|---------|------|---------|
| 1 | 2026-05-06 | Phase 1 (1a–1j) complete. 2 migrations, 2 model updates, 2 new controllers, 2 enhanced controllers, routes wired, QR URL fixed. |
| 2 | 2026-05-06 | Phase 2 (2a–2f) complete. Dashboard wired, dynamic categories, disponible toggle, new categorias/ventas pages, Excel+PDF export. Multiple TPV bugs fixed (route, unavailable products, available flag, storage symlink, comanda/cobrar flow, sent items tracking). NizarAd + NizarCam users added. |
| 3 | 2026-05-06 | Phase 3 (3a–3c) complete. Search bar in TPV header, notes modal on dish click (DishCard → CustomEvent → modal → addToCart with note), cartStore addToCart accepts note param. 3d skipped (call-waiter bell needs public menu page first). |
| 4 | 2026-05-06 | Phase 4 (4a–4c) complete. Kitchen display `/cocina` (React island, 10s poll, timers, status buttons). Public menu `/menu?table={id}` (server-side fetch, call-waiter button). Cocina link in NavSidebar. Backend: OrderController::index, TableController::show, MenuController available filter, Order created_at cast, QR URL now uses table ID. |
| 5 | 2026-05-06 | NavSidebar live table list + status dots. OrderSidebar manual status buttons (Libre/En Servicio/Cobrando). selectedTable atom now carries status field. Category delete toast feedback. Product delete 422 guard (FK violation → friendly error). RBAC plan written at docs/rbac-plan.md (not applied). |
