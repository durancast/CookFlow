# Autenticación - CookFlow (Laravel Sanctum)

Base URL: `http://localhost:8000/api`

---

## Tecnología

**Laravel Sanctum v4** — autenticación mediante tokens de API (Bearer Token).
Cada usuario inicia sesión y recibe un token que adjunta en cada petición protegida.

---

## Endpoints

### POST /api/login

Autentica al usuario y devuelve un token de acceso junto con sus datos y rol.

**Request:**
```
POST /api/login
Content-Type: application/json
```

```json
{
  "email": "admin@cookflow.com",
  "password": "password123"
}
```

**Response 200 OK:**
```json
{
  "token": "1|abc123tokengenerado...",
  "user": {
    "id": 1,
    "name": "Alejandro",
    "email": "admin@cookflow.com",
    "role": "admin",
    "email_verified_at": null,
    "created_at": "2026-03-19T09:33:00.000000Z",
    "updated_at": "2026-03-19T09:33:00.000000Z"
  }
}
```

> **Contrato con el frontend:** las claves `token` y `user.role` están **garantizadas** en la respuesta.
> El frontend de Alejandro debe leer `data.token` y `data.user.role`.

**Response 422 (credenciales incorrectas):**
```json
{
  "message": "Las credenciales no son correctas.",
  "errors": {
    "email": ["Las credenciales no son correctas."]
  }
}
```

---

### POST /api/logout

Invalida el token actual. Requiere autenticación.

**Request:**
```
POST /api/logout
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
  "message": "Sesión cerrada correctamente."
}
```

**Response 401 (sin token o token inválido):**
```json
{
  "message": "Unauthenticated."
}
```

---

### GET /api/me

Devuelve los datos del usuario autenticado.

**Request:**
```
GET /api/me
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "name": "Alejandro",
  "email": "admin@cookflow.com",
  "role": "admin",
  "email_verified_at": null,
  "created_at": "2026-03-19T09:33:00.000000Z",
  "updated_at": "2026-03-19T09:33:00.000000Z"
}
```

---

## Roles de usuario

| Rol | Descripción |
|---|---|
| `user` | Camarero — acceso al TPV, puede hacer pedidos |
| `admin` | Administrador — acceso a rutas protegidas de gestión |

El campo `role` se almacena en la columna `role` de la tabla `users` (valor por defecto: `"user"`).

---

## Middleware de rol `admin`

Las rutas de administración están protegidas por dos capas de middleware:

```
auth:sanctum  →  admin
```

- `auth:sanctum` verifica que el Bearer Token sea válido.
- `admin` verifica que `user.role === "admin"`.

**Response 403 (token válido pero rol insuficiente):**
```json
{
  "message": "Forbidden"
}
```

Para proteger una ruta nueva de admin, añádela en `routes/api.php` dentro del grupo:

```php
Route::middleware(['auth:sanctum', 'admin'])->group(function () {
    // tus rutas aquí
});
```

---

## Rutas protegidas vs públicas

| Endpoint | Método | Autenticación | Rol requerido |
| --- | --- | --- | --- |
| `/api/products` | GET | No | — |
| `/api/login` | POST | No | — |
| `/api/logout` | POST | Sí (Bearer Token) | cualquiera |
| `/api/me` | GET | Sí (Bearer Token) | cualquiera |

---

## Cómo usar el token en el frontend

Guardar el token al hacer login y adjuntarlo en cada petición protegida:

```ts
// Login
const res = await fetch('/api/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password }),
});
const { token, user } = await res.json();
localStorage.setItem('token', token);
localStorage.setItem('role', user.role); // 'admin' | 'user'

// Petición autenticada
const meRes = await fetch('/api/me', {
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
});
```

---

## Setup inicial — crear usuarios de prueba

```bash
php artisan tinker
```

```php
// Admin
App\Models\User::create([
    'name'     => 'Alejandro',
    'email'    => 'admin@cookflow.com',
    'password' => bcrypt('password123'),
    'role'     => 'admin',
]);

// Camarero
App\Models\User::create([
    'name'     => 'Carlos García',
    'email'    => 'camarero@cookflow.com',
    'password' => bcrypt('password123'),
    'role'     => 'user',
]);
```

---

## Probar en Postman

1. `POST /api/login` con body JSON → copia el valor de `token`
2. En las siguientes peticiones: pestaña **Authorization** → tipo **Bearer Token** → pega el token
3. `GET /api/me` para verificar que el token funciona y que `role` llega correctamente

---

Documentación actualizada el 2026-03-19 — Sanctum v4.3
