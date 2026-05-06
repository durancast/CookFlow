# Plan: Role-Based Access Control + Cookie Auth

## Context
The app currently has only 2 roles (`admin`, `waiter`). Route guards are client-side-only (localStorage), meaning any URL can be accessed directly. Several admin pages are missing guards entirely, and one (`/admin/empleados`) has its redirect intentionally commented out. The kitchen screen has no guard at all. The goal is to add 2 new roles (`cook`, `manager`), enforce real server-side access control via Astro SSR middleware, replace localStorage auth with cookies, and make unauthorized access show a proper 403 page instead of a flash of content.

---

## Roles

| Role | `/tpv` | `/cocina` | `/admin/productos` `/admin/categorias` | All other `/admin/*` | `/menu` |
|------|--------|-----------|----------------------------------------|----------------------|---------|
| **admin** | ✓ full | ✓ full | ✓ full (add/edit/delete) | ✓ full | ✓ |
| **manager** | ✗ | ✓ full | ✓ edit only (no add/delete) | ✗ | ✓ |
| **waiter** | ✓ full | ✓ read-only (no status buttons) | ✗ | ✗ | ✓ |
| **cook** | ✗ | ✓ full | ✗ | ✗ | ✓ |
| (guest) | ✗ | ✗ | ✗ | ✗ | ✓ |

Login redirect by role: `admin` → `/admin`, `manager` → `/admin/productos`, `waiter` → `/tpv`, `cook` → `/cocina`

---

## Implementation Steps

### 1. Backend — DB + Roles

**File: `backend/database/migrations/XXXX_change_role_to_string.php`** (new)
- Change `role` column from `enum('admin','waiter')` to `string` — removes SQLite enum limitation
- Default stays `'waiter'`

**File: `backend/database/seeders/UserSeeder.php`**
- Add `manager@cookflow.com` / `manager` (role: `manager`)
- Add `cocinero@cookflow.com` / `cocinero` (role: `cook`)

**File: `backend/app/Http/Middleware/CheckRole.php`** (new)
- Variadic middleware: `handle(Request $request, Closure $next, string ...$roles)`
- Returns 403 JSON if `$request->user()->role` is not in `$roles`

**File: `backend/bootstrap/app.php`**
- Register alias: `'role' => CheckRole::class`

**File: `backend/routes/api.php`**
- Products mutations (store, destroy): `['auth:sanctum', 'role:admin']`
- Products update (PATCH/PUT): `['auth:sanctum', 'role:admin,manager']`
- Categories store + destroy: `['auth:sanctum', 'role:admin']`
- Categories update: `['auth:sanctum', 'role:admin,manager']`
- Order status PATCH: add `auth:sanctum` (cook + waiter token will be in localStorage)
- Keep `GET /api/orders`, `GET /api/products`, `GET /api/categories`, `GET /api/public/menu` public

---

### 2. Frontend — Enable SSR

**`frontend/package.json`**
```
npm install @astrojs/node
```

**File: `frontend/astro.config.mjs`**
```js
import node from '@astrojs/node';
// add to defineConfig:
output: 'server',
adapter: node({ mode: 'standalone' }),
```

---

### 3. Middleware

**File: `frontend/src/middleware.ts`** (new)
```typescript
import { defineMiddleware } from 'astro:middleware';

// Explicit permissions — more specific routes listed before catch-alls
const ROUTE_PERMISSIONS: Array<{ match: (p: string) => boolean; roles: string[] }> = [
  { match: p => p === '/tpv',                          roles: ['admin', 'waiter'] },
  { match: p => p === '/cocina',                       roles: ['admin', 'manager', 'waiter', 'cook'] },
  { match: p => p === '/admin/productos',              roles: ['admin', 'manager'] },
  { match: p => p === '/admin/categorias',             roles: ['admin', 'manager'] },
  { match: p => p.startsWith('/admin'),                roles: ['admin'] },
];

const PUBLIC = (p: string) => p === '/' || p.startsWith('/menu');

export const onRequest = defineMiddleware(async (ctx, next) => {
  const { pathname } = ctx.url;
  if (PUBLIC(pathname)) return next();

  const rule = ROUTE_PERMISSIONS.find(r => r.match(pathname));
  if (!rule) return next(); // unmatched = allow

  const token = ctx.cookies.get('auth_token')?.value;
  const role  = ctx.cookies.get('user_role')?.value;

  if (!token || !role) return ctx.redirect('/');
  if (!rule.roles.includes(role)) return ctx.rewrite(new URL('/403', ctx.url));

  return next();
});
```

---

### 4. 403 Page

**File: `frontend/src/pages/403.astro`** (new)
- Dark-theme page matching app style
- "Acceso restringido" header, role-specific message
- Button: back to their area (read role from cookie client-side, link to correct route)

---

### 5. Login Page — Set Cookies

**File: `frontend/src/pages/index.astro`** — script block changes:
- After successful login, set cookies alongside localStorage:
  ```js
  document.cookie = `auth_token=${token}; path=/; SameSite=Strict; Max-Age=86400`;
  document.cookie = `user_role=${data.user.role}; path=/; SameSite=Strict; Max-Age=86400`;
  ```
- Update redirect logic:
  ```js
  const redirects = { admin: '/admin', manager: '/admin/productos', waiter: '/tpv', cook: '/cocina' };
  window.location.href = redirects[data.user.role] ?? '/tpv';
  ```

---

### 6. Logout — Clear Cookies

Both `NavSidebar.astro` and `tpv/index.astro` (header logout button) must clear cookies:
```js
document.cookie = 'auth_token=; path=/; Max-Age=0';
document.cookie = 'user_role=; path=/; Max-Age=0';
localStorage.clear();
window.location.href = '/';
```

---

### 7. Kitchen Display — Readonly for Waiters

**File: `frontend/src/pages/cocina/index.astro`**
- Server-side: read role from `Astro.cookies`
- Pass prop: `<KitchenDisplay client:only="react" readonly={role === 'waiter'} />`

**File: `frontend/src/components/react/KitchenDisplay.jsx`**
- Accept `readonly` prop (default: false)
- Hide "Preparando" + "Listo ✓" buttons when `readonly === true`
- Show a subtle "Vista de sala" badge on the header when readonly

---

### 8. Admin Pages — Hide Add/Delete for Manager

**File: `frontend/src/pages/admin/productos.astro`**
- Server-side: `const role = Astro.cookies.get('user_role')?.value`
- In the page template/JS: if `role !== 'admin'`, hide "Nuevo Producto" button and all "Eliminar" buttons

**File: `frontend/src/pages/admin/categorias.astro`**
- Same pattern: hide "Nueva Categoría" form and "Eliminar" buttons for manager role

**File: `frontend/src/pages/admin/empleados.astro`**
- Uncomment the redirect line: `window.location.replace('/');` (line ~53)
- Remove debug `console.log` statements

---

### 9. Remove Redundant Client-Side Guards

Once middleware is in place, the individual `checkAuth()` functions inside async data loaders in admin pages are redundant. Remove them from:
- `admin/index.astro`
- `admin/productos.astro`
- `admin/categorias.astro`
- `admin/ventas.astro`

Keep the `<script is:inline>` in `AdminLayout.astro` as a secondary defense (belt-and-suspenders for logged-out users).

---

## Critical Files

| File | Change |
|------|--------|
| `backend/database/migrations/XXXX_change_role_to_string.php` | new — role column to string |
| `backend/database/seeders/UserSeeder.php` | add cook + manager test users |
| `backend/app/Http/Middleware/CheckRole.php` | new — variadic role middleware |
| `backend/bootstrap/app.php` | register `role` alias |
| `backend/routes/api.php` | protect product/category/order mutations |
| `frontend/astro.config.mjs` | SSR + node adapter |
| `frontend/src/middleware.ts` | new — route permission table |
| `frontend/src/pages/403.astro` | new — forbidden page |
| `frontend/src/pages/index.astro` | set cookies on login, update redirects |
| `frontend/src/pages/cocina/index.astro` | pass readonly prop |
| `frontend/src/components/react/KitchenDisplay.jsx` | readonly prop |
| `frontend/src/pages/admin/productos.astro` | hide add/delete for manager |
| `frontend/src/pages/admin/categorias.astro` | hide add/delete for manager |
| `frontend/src/pages/admin/empleados.astro` | fix disabled guard |
| `frontend/src/components/NavSidebar.astro` | clear cookies on logout |
| `frontend/src/pages/tpv/index.astro` | clear cookies on logout |

---

## Verification

1. `php artisan migrate && php artisan db:seed --class=UserSeeder` — confirm 6 users with correct roles
2. Log in as `cocinero@cookflow.com` → redirected to `/cocina`; navigating to `/tpv` → 403; `/admin` → 403
3. Log in as waiter → kitchen screen loads but action buttons hidden (read-only badge visible)
4. Log in as manager → `/admin/productos` loads without Add/Delete buttons; `/admin/ventas` → 403
5. Log in as admin → full access, all buttons visible
6. No login, navigate to `/tpv` directly → redirect to `/` (no content flash)
7. No login, navigate to `/menu` → page loads (public)
8. `PATCH /api/orders/{id}/status` without token → 401
