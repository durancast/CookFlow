# CookFlow Feature Expansion Plan

## Context

Four features are being added to close gaps in the current product:

1. **Call-waiter badge** — the `call_waiter` DB flag fires from the public QR menu but nothing alerts the waiter in the TPV.
2. **Customer self-ordering from QR** — the public `/menu` page shows products but customers can only browse, not order.
3. **Split bill** — no way to divide a table's bill between guests; currently checkout always pays everything at once.
4. **Weekly/monthly reports** — the ventas page is daily-only with no charts; managers need range views.

---

## Feature 1: Call-Waiter Badge in TPV

**No backend changes needed.** `call_waiter` is already returned by `GET /api/tables`. Clear endpoint exists at `POST /api/tables/{table}/clear-waiter`.

### File to modify
- `frontend/src/components/NavSidebar.astro`

### Changes

**1. Badge in `tableButtonHTML()`**

Add a red bell icon badge when `table.call_waiter === true`:
```html
${table.call_waiter
  ? `<span class="absolute -top-1 -right-1 w-4 h-4 bg-red-500 rounded-full flex items-center justify-center text-[9px] animate-pulse">🔔</span>`
  : ''}
```
Wrap the button container in `relative` so the badge positions correctly.

**2. Detect new calls in polling (30s interval)**

Track previous call_waiter states across poll cycles:
```js
let prevCallWaiterMap = {}; // { tableId: boolean }

// After fetching tables in loadTables():
tables.forEach(t => {
  if (t.call_waiter && !prevCallWaiterMap[t.id]) {
    window.showToast?.(`🔔 Mesa ${t.number} llama al camarero`, 'success');
  }
});
prevCallWaiterMap = Object.fromEntries(tables.map(t => [t.id, t.call_waiter]));
```

**3. Auto-clear on table select**

In `selectTable()` in `tpv/index.astro`, after loading the active order, if the table has `call_waiter === true`, fire `POST /api/tables/{id}/clear-waiter` with Bearer token. The badge disappears on the next 30s poll.

---

## Feature 2: Customer Self-Ordering from QR Menu

**No backend changes needed.** `POST /api/orders` is already public and accepts `{table_id, items[]}`.

### File to modify
- `frontend/src/pages/menu/index.astro`

### Changes

**1. Cart state (vanilla JS)**
```js
const cart = {}; // { [productId]: { product, qty } }
```

**2. Product cards** — add `+` button per product. When qty > 0 show `-` and a quantity badge.

**3. Sticky bottom cart bar** (hidden when cart empty):
```html
<div id="cart-bar" class="fixed bottom-0 left-0 right-0 bg-tpv-surface border-t ...">
  <span id="cart-summary">X artículos — €XX.XX</span>
  <button id="btn-send-order">Enviar pedido →</button>
</div>
```

**4. Order confirmation modal** — shows item list + "Confirmar" button. On confirm:
```js
fetch('/api/orders', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ table_id: tableId, items: [...] })
})
```

**5. Success state** — replace cart bar with "✓ Pedido enviado — el equipo lo está preparando". Clear cart.

**6. Error handling** — show inline error, keep cart intact for retry.

`tableId` is already available from the URL query param loaded server-side.

---

## Feature 3: Split Bill

**No backend changes needed.** Split is calculated client-side; final checkout uses existing `POST /api/tables/{id}/checkout`.

### Files to modify
- `frontend/src/components/react/OrderSidebar.jsx`

### New state
```js
const [splitMode, setSplitMode] = useState(false);
const [splitSide, setSplitSide] = useState({}); // { itemKey: 'A' | 'B' }
const [paidTickets, setPaidTickets] = useState(new Set()); // 'A', 'B'
const [splitPayingTicket, setSplitPayingTicket] = useState(null); // 'A' | 'B' | null
```

`itemKey` = `${item.id}|${item.note}` (consistent with existing codebase pattern).

### Changes

**1. "Dividir" button** — shown in the main action bar alongside COBRAR (only when there are sent items):
```jsx
<button onClick={() => { setSplitMode(true); /* init splitSide: all → 'A' */ }}>
  Dividir cuenta
</button>
```

**2. Split UI** (replaces payment UI when `splitMode === true`):
- List of sent items only (`items.filter(i => i.sent)`)
- Each item has an A / B pill toggle
- Two totals: "Ticket A: €X" | "Ticket B: €Y"
- "Pagar A" → cash/card flow for Ticket A total → on complete: `printCustomerReceipt(ticketAItems, ...)`, add 'A' to `paidTickets`
- "Pagar B" → same for B
- Both paid → "Cerrar mesa" → calls `handleCheckout()` (existing, marks all paid + frees table)
- "Cancelar" → exits split mode, resets state

**3. Printing** — `printCustomerReceipt()` already accepts any items array; pass filtered A or B items directly.

---

## Feature 4: Weekly/Monthly Reports

### Backend changes

**File:** `backend/app/Http/Controllers/ReportController.php`

Add `range(Request $request)` method:
- Params: `from` (YYYY-MM-DD, required), `to` (YYYY-MM-DD, required)
- Query: paid orders where `DATE(created_at) BETWEEN $from AND $to`
- Returns:
```json
{
  "from": "2026-05-01",
  "to": "2026-05-10",
  "revenue": 5000.00,
  "order_count": 50,
  "avg_ticket": 100.00,
  "products": [{ "name": "...", "quantity": 10, "total": 100.00 }],
  "daily": [{ "date": "2026-05-01", "revenue": 500.00, "orders": 5 }]
}
```

Use `DATE(created_at)` with `groupBy` for the `daily` breakdown.

**File:** `backend/routes/api.php`

Add inside the `auth:sanctum` group:
```php
Route::get('/reports/range', [ReportController::class, 'range']);
```

### Frontend changes

**Install:** `pnpm add chart.js` (inside `frontend/`)

**File:** `frontend/src/pages/admin/ventas.astro`

**1. Date range UI** — replace single date picker with quick selectors:
- Buttons: **Hoy** | **Esta semana** | **Este mes** | **Personalizado**
- "Personalizado" shows two `<input type="date">` fields (from / to)
- Each quick button auto-calculates from/to and triggers load

**2. API routing:**
```js
async function loadReport(from, to) {
  const endpoint = (from === to)
    ? `/api/reports/daily?date=${from}`
    : `/api/reports/range?from=${from}&to=${to}`;
  // ...
}
```

**3. Chart.js bar chart** — `<canvas id="revenue-chart">` shown above the products table for range > 1 day. Registers only required Chart.js tree-shakeable modules (`BarController`, `BarElement`, `CategoryScale`, `LinearScale`, `Tooltip`). Destroys and recreates on each load.

**4. Exports** — add date range info to PDF header. Excel/PDF product table logic unchanged.

**5. Middleware** — extend `/admin/ventas` guard from `['admin']` to `['admin', 'manager']` in `frontend/src/middleware.ts`.

---

## Build Order

1. **Call-waiter badge** — smallest, no backend, wires existing infra
2. **Weekly/monthly reports** — backend + frontend, independent of other features
3. **Customer self-ordering** — frontend only, moderate complexity
4. **Split bill** — React state management, most UI complexity

---

## Verification

| Feature | How to verify |
|---------|---------------|
| Call-waiter badge | Open `/menu?table=1` → click "Llamar al camarero" → within 30s the TPV table button shows a red bell badge and a toast fires |
| Customer self-ordering | Open `/menu?table=1` → add items → submit → check `/cocina` shows new pending order |
| Split bill | TPV with occupied table with sent items → click "Dividir" → assign items A/B → pay each → table is freed |
| Range reports | `/admin/ventas` → click "Esta semana" → chart renders + products table shows aggregated data |
