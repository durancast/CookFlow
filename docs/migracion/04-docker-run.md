# 04 - Levantar el entorno con Docker

Este documento describe cómo generar el artefacto del backend y levantar el
entorno completo (PostgreSQL + backend Spring Boot) con Docker Compose.

## 1. Pre-requisitos

- Java 21 y Maven (solo para generar el jar).
- Docker y Docker Compose instalados y en ejecución.
- El jar **no** se genera dentro del contenedor: el `Dockerfile` simplemente
  copia `target/cookflow-backend-0.1.0-SNAPSHOT.jar`, por lo primero hay que
  compilarlo en el host.

## 2. Generar el jar

Desde la raíz de `backend-spring/`:

```bash
mvn clean package -DskipTests
```

Verifica que el artefacto exista:

```bash
ls target/cookflow-backend-0.1.0-SNAPSHOT.jar
```

> Alternativa sin Maven: `docker build` no aplica aquí porque el Dockerfile
> asume que el jar ya está compilado (estrategia "fat-jar prebuilt").

## 3. Levantar el entorno

Desde la **raíz del proyecto** (donde está `docker-compose.yml`):

```bash
docker-compose up -d
```

Ver el estado de los contenedores:

```bash
docker-compose ps
```

Espera a que `cookflow-backend` pase a `healthy/up` (el backend arranca solo
cuando Postgres supera el healthcheck `pg_isready`).

## 4. Ver logs

```bash
docker-compose logs -f backend
```

Logs de Postgres:

```bash
docker-compose logs -f postgres
```

## 5. Ejemplos de curl

### Menú público

```bash
curl -s http://localhost:8080/api/public/menu | jq
```

Con tenant concreto:

```bash
curl -s "http://localhost:8080/api/public/menu?tenant=mi-restaurante" | jq
```

### Login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password123"}' | jq
```

La respuesta incluye el token JWT; úsalo en peticiones protegidas:

```bash
TOKEN="<token> anterior"
curl -s http://localhost:8080/api/auth/me -H "Authorization: Bearer $TOKEN" | jq
```

> Ajusta email/según el seed de tu base de datos.

## 6. Parar el entorno

```bash
docker-compose down
```

Mantener los datos de Postgres (por defecto el volumen named se conserva).

Borrar también los datos persistidos:

```bash
docker-compose down -v
```

## 7. Resolución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| Backend en `restart` continuo | Postgres aún no sano o credenciales incorrectas | `docker-compose logs backend` |
| `FATAL: password authentication failed` | Cambió la contraseña del volumen | `docker-compose down -v && docker-compose up -d` |
| Puerto 5432 ocupado | Otro Postgres local | Cambia el mapeo a `"5433:5432"` |
| `Connection refused` al API | Backend aún arrancando | Espera a `Started CookFlowApplication` en logs |
