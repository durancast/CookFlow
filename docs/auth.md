# Autenticación - CookFlow (Laravel Sanctum)

Base URL: `http://localhost:8000/api`

---

## Tecnología

**Laravel Sanctum v4** — autenticación mediante tokens de API (Bearer Token).
Cada camarero inicia sesión y recibe un token que adjunta en cada petición protegida.

---

## Endpoints

### POST /api/login

Autentica al camarero y devuelve un token de acceso.

**Request:**
```
POST /api/login
Content-Type: application/json
```

```json
{
  "email": "camarero@cookflow.com",
  "password": "password123"
}
```

**Response 200 OK:**
```json
{
  "token": "1|abc123xyz...",
  "user": {
    "id": 1,
    "name": "Carlos García",
    "email": "camarero@cookflow.com"
  }
}
```

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

---

### GET /api/me

Devuelve los datos del camarero autenticado.

**Request:**
```
GET /api/me
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "name": "Carlos García",
  "email": "camarero@cookflow.com"
}
```

**Response 401 (sin token o token inválido):**
```json
{
  "message": "Unauthenticated."
}
```

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
const { token } = await res.json();
localStorage.setItem('token', token);

// Petición autenticada
const meRes = await fetch('/api/me', {
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
});
```

---

## Rutas protegidas vs públicas

| Endpoint | Método | Autenticación |
|---|---|---|
| `/api/products` | GET | No |
| `/api/login` | POST | No |
| `/api/logout` | POST | Sí (Bearer Token) |
| `/api/me` | GET | Sí (Bearer Token) |

---

## Setup inicial — crear un camarero de prueba

```bash
php artisan tinker
```

```php
App\Models\User::create([
    'name'     => 'Carlos García',
    'email'    => 'camarero@cookflow.com',
    'password' => bcrypt('password123'),
]);
```

---

## Pasos para activar completamente

1. Ejecutar la migración de tokens de Sanctum:
   ```bash
   php artisan migrate
   ```

2. Crear un usuario de prueba (ver arriba).

3. Probar el login:
   ```bash
   curl -X POST http://localhost:8000/api/login \
     -H "Content-Type: application/json" \
     -d '{"email":"camarero@cookflow.com","password":"password123"}'
   ```

---

*Documentación generada el 2026-03-12 — Sanctum v4.3*
