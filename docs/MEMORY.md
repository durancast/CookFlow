# Project Memory
Last updated: 2026-05-06 | Session 1 | Branch: develop
Memory health: 9/10

## Project Overview
CookFlow — TFG restaurant POS/SaaS. Core flow (login → table → dishes → order → charge) works. Expanding with dashboard, kitchen screen, public menu, and UX polish per the plan in `docs/CookFlow Plan.md`.

## Where We Left Off
- **Current task:** Phase 2 complete. Phase 3 not started.
- **Status:** Phase 1 ✓ | Phase 2 ✓ | Phase 3–5 pending
- **Next immediate step:** Phase 3a — product search bar in `tpv/index.astro`
- **Open question:** None

## Completed
- 2026-05-06 **Phase 1 (1a–1j)** — all backend foundations. See `docs/changelog.md`.
- 2026-05-06 **Phase 2 (2a–2f)** — admin panel wired + new pages. See `docs/changelog.md`.
- 2026-05-06 **Bugs fixed** — TPV route, unavailable products, available flag not persisting, storage symlink, OrderSidebar comanda/cobrar flow, sent items tracking.

## Active Work
- [ ] **Phase 3a** — Product search bar in TPV
- [ ] **Phase 3b** — Hide unavailable products in TPV grid
- [ ] **Phase 3c** — Quick notes modal on dish click
- [ ] **Phase 3d** — Call-waiter bell on table cards
- [ ] **Phase 4a** — Kitchen display page `/cocina`
- [ ] **Phase 4b** — Public customer menu `/menu?table=X`
- [ ] **Phase 4c** — Cocina link in `NavSidebar.astro`
- [ ] **Phase 5a** — `ConfirmDialog.jsx` for COBRAR + deletes
- [ ] **Phase 5b** — Wire print receipt after COBRAR

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
