# CookFlow — Feature Expansion Plan

## What is this?
CookFlow is a TFG restaurant POS/SaaS. The core flow (login → select table → add dishes → send order → charge) already works. This plan fills in the gaps that make it feel like a real product: a working dashboard, a kitchen screen, a public menu, and small UX improvements that are currently missing or hardcoded.

---

## Phase 1 — Backend Foundations

### 1a — Add `available` column to products
**What:** New boolean column on the `products` table, default `true`.
**Why:** Right now there is no way to hide a product from the menu without deleting it. A restaurant needs to mark dishes as unavailable when they run out (e.g. daily special sold out) and bring them back later.

### 1b — Add `call_waiter` column to tables
**What:** New boolean column on the `tables` table, default `false`.
**Why:** The public menu page (Phase 4) will let customers tap a button to call the waiter. That button needs somewhere to write to so the TPV can show a visual alert on the affected table.

### 1c — Update Product model
**What:** Add `available` to `$fillable` and cast it as boolean.
**Why:** Without adding it to `$fillable`, Laravel will silently ignore it on mass assignment. The boolean cast ensures the value comes back as `true`/`false` in JSON instead of `1`/`0`.

### 1d — Update Table model
**What:** Add `call_waiter` to `$fillable` and cast it as boolean.
**Why:** Same reason as above — needed so the call-waiter endpoints can actually write the value.

### 1e — New CategoryController (CRUD)
**What:** A controller with `index`, `store`, `update`, and `destroy` methods for categories. Destroy will refuse if the category still has products.
**Why:** Right now categories only exist in the database — there is no way to manage them from the admin panel. Adding a new category (e.g. "Menú del día") requires going directly into the database. This makes it self-service.

### 1f — Enhance DashboardController
**What:** Add `orders_today`, `avg_ticket`, and `orders_by_hour` to the existing `/api/dashboard/stats` response.
**Why:** The dashboard currently shows `revenue_today`, `top_product`, and `occupied_tables` but the frontend hardcodes "432.50€" and "24" anyway because the page never actually called the API. Adding more fields makes the real data worth wiring up (Phase 2a).

### 1g — New ReportController
**What:** A `daily` endpoint that accepts `?date=YYYY-MM-DD` and returns revenue, order count, average ticket, and a breakdown of every product sold that day.
**Why:** The admin needs to know what was sold on any given day — not just today. This is the data source for the sales report page in Phase 2f.

### 1h — Add call-waiter endpoints to TableController
**What:** Two new POST routes: `call-waiter` (sets `call_waiter = true`) and `clear-waiter` (sets `call_waiter = false`).
**Why:** The public menu page needs to write the call, and the TPV needs to clear it when the waiter acknowledges. Without dedicated endpoints the client would have to send a full table update which is messy and would require auth the public page doesn't have.

### 1i — Fix the hardcoded QR URL
**What:** Replace `"https://cookflow.com/menu?table="` with `env('APP_PUBLIC_URL', 'http://localhost:4321') . '/menu?table='`.
**Why:** The QR code currently points to a domain that doesn't exist. Scanning it in development (or in a demo) produces a dead link. Using an env variable makes it work locally and lets it be configured correctly before a real deployment.

### 1j — Register all new routes
**What:** Add routes for categories (`apiResource`), reports (`GET /reports/daily`), and call-waiter (`POST /tables/{table}/call-waiter` and `/tables/{table}/clear-waiter`).
**Why:** None of the new controllers or methods above are reachable until they are wired in `api.php`.

---

## Phase 2 — Admin Panel Improvements

### 2a — Wire the dashboard to the real API
**What:** Replace the hardcoded "432.50€" and "24" in `admin/index.astro` with a `fetch('/api/dashboard/stats')` call that populates the stat cards dynamically.
**Why:** The controller already computes the right values — the frontend just never called it. A dashboard that always shows the same fake numbers is worse than no dashboard because it builds false confidence.

### 2b — Dynamic category dropdown in the products form
**What:** Remove the four hardcoded `<option>` tags in `admin/productos.astro` and replace them with options fetched from `/api/categories`.
**Why:** If a new category is created via Phase 2e, it will never appear in the product form because the options are baked into the HTML. Any category management is useless unless the product form reads from the same source of truth.

### 2c — Add "Disponible" toggle to the products table
**What:** Add a toggle switch column to the products list that calls `PATCH /api/products/{id}` with `{ available: !current }`.
**Why:** Without a UI for the `available` column added in Phase 1a, the column exists in the database but is unreachable. The waiter needs to be able to disable a sold-out dish in seconds, not via a full edit form.

### 2d — Add missing admin nav links
**What:** Add links to Categorías, Cocina, and Ventas in the admin sidebar (`AdminLayout.astro`).
**Why:** The new pages created in this plan are unreachable unless they are in the navigation. A page with no nav link effectively doesn't exist for the user.

### 2e — New page: Category management (`/admin/categorias`)
**What:** A page with a table of all categories, an inline form to add new ones (name → auto-generates slug), inline editing, and a delete button (disabled with tooltip if the category has products).
**Why:** Categories are currently seeded manually. Adding a "Menú del día" or renaming "Postres" to "Desserts" requires direct database access. This makes it a 10-second admin task.

### 2f — New page: Daily sales report (`/admin/ventas`)
**What:** A page with a date picker (defaults to today), summary cards (revenue, order count, avg ticket), and a breakdown table of every product sold. Includes a "Export CSV" button that generates the file client-side.
**Why:** A restaurant owner needs to reconcile daily takings, know which dishes performed best, and hand data to an accountant. None of that is possible without a sales report.

---

## Phase 3 — TPV Improvements

### 3a — Product search bar
**What:** A search input above the product grid that filters visible cards live as the waiter types. Works alongside the existing category filter.
**Why:** A restaurant with 40+ dishes makes the waiter scroll to find items. A search bar cuts that to under 2 seconds. It is the single highest-impact UX improvement for the waiter screen.

### 3b — Hide unavailable products
**What:** Filter out products where `available === false` before rendering the grid.
**Why:** If a dish is marked as unavailable (sold out), it should not appear in the TPV. Showing it and then having the waiter try to order it only to find out it's unavailable creates confusion and slows down service.

### 3c — Quick notes modal on dish click
**What:** Instead of adding a dish to the cart immediately on click, show a small modal with an optional text field ("Note: e.g. no onion"). Confirming adds the item with the note attached.
**Why:** The OrderSidebar already supports notes per item, but there is no way to enter them at the moment of adding. Waiters currently have to add the item first and then find and edit the note field — this makes it one step.

### 3d — Call-waiter bell indicator on table cards
**What:** When rendering the table selection modal, show a bell icon on tables where `call_waiter === true`. Selecting such a table automatically calls `POST /api/tables/{id}/clear-waiter`.
**Why:** Without a visual indicator, the waiter has no idea a customer is calling. The whole call-waiter feature is pointless unless the alert is visible where the waiter already looks — the table selection screen.

---

## Phase 4 — Kitchen Display & Public Menu

### 4a — Kitchen display page (`/cocina`)
**What:** A standalone page (React island) that polls `/api/orders?status=pending,preparing` every 10 seconds. Each order card shows the table number, a live elapsed timer (colour-coded: green < 5 min, yellow 5–10 min, red > 10 min), and the list of items. Two action buttons: "Preparando" and "Listo" to bump the order status.
**Why:** The kitchen currently has no screen. The cook either has to look over the waiter's shoulder or wait for a printed ticket. A kitchen display is standard in any real POS and lets the kitchen manage their own queue without depending on paper.

### 4b — Public customer menu page (`/menu?table=X`)
**What:** A read-only page that reads `?table=NUMBER` from the URL, fetches the public menu, and renders it grouped by category. Includes a "Llamar al camarero" button that calls `POST /api/tables/{table}/call-waiter` and shows a confirmation toast.
**Why:** This is what the QR code on the table should point to. It gives the customer a way to browse the menu on their phone and call the waiter without the waiter having to visit the table first. It also makes the QR code feature actually useful.

### 4c — Add Cocina link to the TPV sidebar
**What:** Add a link to `/cocina` in `NavSidebar.astro`.
**Why:** The kitchen page will be used constantly during service and needs to be reachable in one click from the TPV screen, not by typing the URL manually.

---

## Phase 5 — Polish

### 5a — Reusable confirmation dialog
**What:** A `ConfirmDialog.jsx` component used before the COBRAR (charge) action in the OrderSidebar and before any delete action in the admin.
**Why:** Right now clicking COBRAR charges the table immediately with no confirmation. A mis-tap during a busy service charges the wrong table or amount. Deleting a product or category is also instant and irreversible — a confirmation prevents accidents.

### 5b — Wire print receipt after COBRAR
**What:** After a successful COBRAR API response, call `printCustomerReceipt(items, tableNumber, total)` (already implemented in `printer.js`). Add an "Imprimir ticket" checkbox (default checked) so the waiter can skip printing if the customer doesn't want one.
**Why:** The print function exists but is never called. The receipt is the last step of every transaction and currently requires the waiter to manually trigger it from somewhere else. Wiring it here completes the checkout flow.

---

## Implementation order
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5

Each phase is independently testable once the previous one is complete. Phase 1 is a hard prerequisite for everything else because the frontend phases depend on the new columns and endpoints.

---

> **Note:** Migration files for `available` and `call_waiter` were already created in `backend/database/migrations/` but have not been run yet.
