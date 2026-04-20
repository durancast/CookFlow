# CookFlow — Project Structure

## Overview
Restaurant SaaS POS system (TFG — Final Year Project). Manages orders, tables, and point-of-sale operations.

**Team:** Alejandro (Frontend) · Nizar (Backend/API) · Luis (Database)

---

## Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Astro 5.17 + React 19 + Tailwind CSS 4 + Nanostores |
| Backend | Laravel 12 (PHP 8.2+) + Sanctum 4.3 |
| Database | MySQL (SQLite for local dev) |
| Auth | Bearer tokens via Laravel Sanctum |
| QR Codes | simple-qrcode (SVG per table) |
| Language | TypeScript (strict) / PHP |
| Package mgr | pnpm (frontend) / Composer (backend) |

---

## Directory Tree

```
CookFlow/
├── frontend/                        # Astro + React TPV UI (port 4321)
│   └── src/
│       ├── components/
│       │   ├── DishCard.astro       # Product card with add-to-cart button
│       │   ├── NavSidebar.astro     # Waiter navigation sidebar
│       │   └── react/
│       │       └── OrderSidebar.jsx # React cart component (subscribes to Nanostores)
│       ├── data/
│       │   └── products.ts          # Static product data (dev reference)
│       ├── layouts/
│       │   ├── Layout.astro         # Base layout
│       │   └── AdminLayout.astro    # Admin layout
│       ├── pages/
│       │   ├── index.astro          # Login page
│       │   ├── tpv/
│       │   │   └── tpv.astro        # POS terminal (3-col: nav | products | cart)
│       │   └── admin/
│       │       ├── index.astro      # Admin dashboard
│       │       ├── empleados.astro  # Employee CRUD
│       │       ├── mesas.astro      # Table CRUD + QR viewer
│       │       └── productos.astro  # Product CRUD + image upload
│       ├── store/
│       │   └── cartStore.js         # Nanostores: cartItems, selectedTable atoms
│       ├── types/
│       │   ├── api.ts               # API response types (Category, Product)
│       │   └── product.ts           # Product types + CategorySlug union
│       └── styles/
│           └── global.css           # Global styles + custom scrollbar
│
├── backend/                         # Laravel 12 API (port 8000)
│   ├── app/
│   │   ├── Http/
│   │   │   ├── Controllers/
│   │   │   │   ├── AuthController.php      # login / logout / me
│   │   │   │   ├── ProductController.php   # CRUD + image upload
│   │   │   │   ├── TableController.php     # CRUD + QR generation
│   │   │   │   ├── OrderController.php     # create order / update status
│   │   │   │   └── UserController.php      # employee CRUD
│   │   │   └── Middleware/
│   │   │       └── AdminMiddleware.php     # role='admin' gate
│   │   └── Models/
│   │       ├── User.php             # HasApiTokens, role (admin|waiter)
│   │       ├── Category.php         # hasMany(Product)
│   │       ├── Product.php          # belongsTo(Category), image stored in storage/
│   │       ├── Table.php            # status (free|occupied|pending), activeOrder relation
│   │       ├── Order.php            # status (pending|preparing|served|paid)
│   │       └── OrderItem.php        # quantity, unit_price (captured at order time), notes
│   ├── database/
│   │   ├── migrations/              # 9 migration files
│   │   └── seeders/
│   │       ├── UserSeeder.php       # Demo admin + waiter accounts
│   │       └── TableSeeder.php      # Demo tables
│   └── routes/
│       └── api.php                  # All API routes
│
├── docs/
│   ├── setup.md                     # Installation & daily workflow
│   ├── db.md                        # DB design + ER diagram
│   ├── api.md                       # API endpoint reference
│   ├── auth.md                      # Auth patterns (Sanctum)
│   ├── orders.md                    # Order flow & business logic
│   └── design.md                    # UI/UX design system
│
└── STRUCTURE.md                     # This file
```

---

## Database Schema

```
users           categories      products
─────           ──────────      ────────
id              id              id
name            name            name
email*          slug            price DECIMAL(8,2)
role            └─ hasMany ──►  description
password                        image
                                category_id FK
                                └─ belongsTo ─► categories

tables          orders          order_items
──────          ──────          ───────────
id              id              id
number*         table_id FK     order_id FK
capacity        waiter_id FK    product_id FK
status†         status‡         quantity
                total_price     unit_price (snapshot)
                                notes
```
`*` unique · `†` free|occupied|pending · `‡` pending|preparing|served|paid  
All models have NO timestamps except `order_items`.

---

## API Reference

**Base URL:** `http://localhost:8000/api`

### Auth
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /login | public | Returns `{token, user}` |
| POST | /logout | sanctum | Revokes token |
| GET | /me | sanctum | Authenticated user |
| GET | /users-list | public | `[{id, name, role}]` for login dropdown |

### Products
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /products | public | All products with category (eager-loaded) |
| POST | /products | admin | Create (multipart/form-data for image) |
| PATCH | /products/{id} | admin | Update |
| DELETE | /products/{id} | admin | Delete + removes image file |

### Tables
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /tables | sanctum | Tables with activeOrder + qr_url |
| GET | /tables/{id}/qr | sanctum | SVG QR code |
| POST | /tables | admin | Create |
| PATCH | /tables/{id} | admin | Update |
| DELETE | /tables/{id} | admin | Delete |

### Orders
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /orders | public | Create order (backend recalculates total) |
| PATCH | /orders/{id}/status | sanctum | Update status (waiter owns order) |

**Order payload:**
```json
{
  "table_id": 1,
  "items": [
    { "product_id": 3, "quantity": 2, "notes": "Sin cebolla" }
  ]
}
```

---

## Frontend Architecture

### Login Flow
1. Load `/api/users-list` → populate dropdown
2. Select name → enter password → POST `/api/login`
3. Email auto-built: `{name_lowercase}@cookflow.com`
4. Store `token` + `role` in `localStorage`
5. Redirect: admin → `/admin/empleados` | waiter → `/tpv/tpv`

### TPV (POS) Layout
```
┌─────────────────────────────────────────────────┐
│  NavSidebar  │   Product Grid      │  OrderSidebar  │
│  (Astro)     │   + Category Filter │  (React)        │
│              │   (Astro + JS)      │                 │
└─────────────────────────────────────────────────┘
```
- Category filter: client-side visibility toggle on `.dish-card`
- Table selection: modal, color-coded (green=free, red=occupied)
- Cart: Nanostores atoms, React `OrderSidebar` subscribes

### State (cartStore.js)
```js
cartItems     // atom — [{ id, name, quantity, price, note }]
selectedTable // atom — table object

addToCart(product)
updateQuantity(id, qty)
removeFromCart(id)
clearCart()
addNoteToItem(id, note)
```

---

## Key Architectural Decisions

1. **Astro + React islands** — Astro for SSR/static, React only for interactive cart
2. **Nanostores** — lightweight over Redux; simple cart + table state
3. **Astro dev proxy** — `/api/*` → Laravel at port 8000 (avoids CORS in dev)
4. **No model timestamps** — intentionally disabled for simplicity (TFG scope)
5. **Server-side price recalculation** — `DB::transaction()` in OrderController, never trusts client total
6. **Eager loading** — controllers load relations (products.category, tables.activeOrder) to prevent N+1

---

## Dev Workflow

```bash
# Terminal 1 — Backend
cd backend
php artisan serve          # http://localhost:8000

# Terminal 2 — Frontend
cd frontend
pnpm dev                   # http://localhost:4321
```

**First time setup:**
```bash
# Backend
cd backend
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate --seed

# Frontend
cd frontend
pnpm install
```

Frontend `.env`: `PUBLIC_BACKEND_URL=http://127.0.0.1:8000`

**Branch:** Work on `develop`. Pull before starting each session.
