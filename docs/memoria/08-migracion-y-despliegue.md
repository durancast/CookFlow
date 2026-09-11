# Capítulo 8. Migración y despliegue

## 8.1. Introducción

Un esfuerzo de migración no termina cuando el nuevo backend compila:
termina cuando puede **desplegarse** (o "levantarse") de forma reproducible,
con la misma infraestructura que se ha probado, y documentado para que un
colaborador lo reproduce en minutos. Este capítulo describe (a) la
migración Laravel/MySQL → Spring Boot/PostgreSQL a nivel de API y de
esquema, y (b) el entorno de despliegue con Docker Compose que se entrega
como artefacto del trabajo.

## 8.2. Estrategia de migración

La estrategia adoptada es **"big-bang de backend, API estable"**:

1. **Contrato estable.** La superficie de API REST (`docs/api.md`) se
   mantiene como contrato inmutable entre versiones. El nuevo backend debe
   responder los mismos códigos HTTP, el mismo shape de respuestas y los
   mismos códigos de error que la versión Laravel de referencia.
2. **Equivalencia de esquema.** Las tablas MySQL se reescriben sobre
   PostgreSQL aprovechando tipos nativos (`ENUM`, `CITEXT`) — ver
   capítulo 5. No se busca compatibilidad binaria con MySQL, sino
   compatibilidad de comportamiento.
3. **Pruebas como oráculo.** La suite de 38 tests (§7) codifica el
   comportamiento esperado; los tests de seguridad y de tenancy son los
   "contrat-test" que prueban la no-regresión.
4. **Corte.** Una vez la suite verde y el despliegue local funcionando, se
   apaga la instancia Laravel y se redirige el proxy al nuevo backend.

Esta estrategia es más arriesgada que la "strangler fig" (desplazar
endpoints uno a uno en paralelo), pero es más rápida y se justifica por el
tamaño acotado de la API (10 controllers) y por las pruebas que amparan el
cambio.

## 8.3. Entorno de despliegue con Docker Compose

El artefacto de despliegue se compone de:

- **Imagen del backend.** `backend-spring/Dockerfile` construye sobre
  `eclipse-temurin:21-jdk-alpine`. La imagen es de corrida (no de build):
  se copia el fat-JAR ya compilado en el host.

```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/cookflow-backend-0.1.0-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=default
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Composición.** `docker-compose.yml` define dos servicios:
  - `postgres` sobre `postgres:16` con volumen named persistido,
    healthcheck `pg_isready` y puertos `5432` expuesto.
  - `backend` construido desde `./backend-spring`, esperando a que
    Postgres pase el healthcheck (`depends_on.condition: service_healthy`).

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: cookflow
      POSTGRES_USER: cookflow
      POSTGRES_PASSWORD: cookflow
    ports: ["5432:5432"]
    volumes: [cookflow-pgdata:/var/lib/postgresql/data]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -h localhost -U cookflow -d cookflow"]
      interval: 5s
      timeout: 5s
      retries: 10
      start_period: 10s

  backend:
    build: { context: ./backend-spring, dockerfile: Dockerfile }
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cookflow
      SPRING_DATASOURCE_USERNAME: cookflow
      SPRING_DATASOURCE_PASSWORD: cookflow
      JWT_SECRET: dev-secret-change-in-prod
    ports: ["8080:8080"]
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  cookflow-pgdata:
```

Observaciones de diseño:
- **Healthcheck + depends_on.** Garanticia que el backend no arranca
  antes que Postgres aceptando conexiones; se evita el típico
  `Connection refused` del arranque de Spring.
- **Variables de entorno.** El backend no tiene secretos en el imagen;
  `SPRING_DATASOURCE_*` y `JWT_SECRET` son inyectados por el entorno. La
  variable `JWT_SECRET` marcada con `dev-secret-change-in-prod` es un
  recordatorio explícito de que debe generarse en producción
  (`openssl rand -hex 32`).
- **Volumen named.** Los datos de Postgres sobreviven a
  `docker-compose down`, y se eliminan explícitamente con `-v`.

## 8.4. Ciclo de vida del despliegue

1. **Build host.** `mvn clean package -DskipTests` en `backend-spring/`
   genera el JAR en `target/`.
2. **Lanzamiento.** `docker-compose up -d` levanta ambos contenedores.
3. **Supervisión.** `docker-compose logs -f backend`; se confirma
   `Started CookFlowApplication` y `Tomcat started on port 8080`.
4. **Verificación de API.**
   - `GET /api/public/menu?tenant=X` → 200 con el menú.
   - `POST /api/auth/login` → 200 con token.
   - Uso del token en `/api/auth/me`.
5. **Arresto.** `docker-compose down` (conserva datos) o `down -v`
   (borra datos).

Los pasos completos, con comandos y ejemplos de curl, se encuentran en
`docs/migracion/04-docker-run.md`.

## 8.5. Consideraciones de producción (fuera de alcance)

- TLS: en producción, TLS debe gestionarse en el proxy
  (Caddy/Traefik/ALB), no en el JAR.
- Escalado horizontal: al ser el backend sin estado, duplicar réplicas
  detrás de un balanceador es directo.
- Observabilidad: actuator health/metrics + log de trazas con MDC.
- Backups: `pg_dump` programado sobre el volumen named.
- Rotación de `JWT_SECRET` y de contraseñas.

Estos puntos quedan como trabajo futuro explícito en el capítulo 9.

## 8.6. Checklist de migración

| Ítem | Estado |
|---|---|
| Suite de 38 tests en verde | ✅ |
| Build del fat-JAR reproducible | ✅ |
| `docker-compose up -d` levanta ambos servicios | ✅ |
| Aislamiento multi-tenant verificado en tests | ✅ |
| Contrato de API equivalente a Laravel | ✅ |
| Documentación en `docs/migracion/04` | ✅ |
| Checklist de seguridad revisada (JWT, RBAC, hash) | ✅ |

## 8.7. Conclusión del capítulo

El despliegue con Docker Compose resuelve la reproductibilidad del
entorno (build del JAR + Postgres + orden de arranque) con un punto de
verdad declarativo. La estrategia de migración — API estable, esquema
equivalente, tests como oráculo — es adecuada al tamaño del sistema y a
su equipo. Las consideraciones de producción y el checklist de migración
delimitan el alcance de esta iteración y el trabajo pendiente.
