# Capítulo 9. Conclusiones y trabajo futuro

## 9.1. Introducción

Este capítulo cierra la memoria. Recoge en primer lugar las conclusiones
técnicas del trabajo — qué se logró respecto a los objetivos declarados en
el capítulo 1 — y, en segundo lugar, las líneas de trabajo futuro
priorizadas que quedan abiertas de forma deliberada.

## 9.2. Conclusiones

### 9.2.1. Objetivo de producto

Se ha implementado el backend completo de CookFlow como un servicio web
para un TPV de restaurante: menú multi-tenant, mesas, pedidos con máquina
de estados explícita (`pending → preparing → served → paid`), usuarios y
roles, e informes de dashboard. La API pública documentada en
`docs/api.md` se cumple íntegramente por el nuevo backend Spring Boot.

### 9.2.2. Objetivo de ingeniería: la migración

La migración de Laravel + MySQL a Spring Boot 3 + PostgreSQL 16 se ha
completado sobre un contrato de API estable, no sobre una equivalencia
binaria. Diferencias concretas que la migración ha aprovechado:

- **Tipos `ENUM` nativos.** Estados de pedido, tablas y roles se definen en
  la base de datos (cap. 5) y se validan en el dominio (cap. 6) — doble
  verificación.
- **`CITEXT` para emails y slugs.** Elimina `WHERE LOWER(email)=` en
  consultas y constraints.
- **PK compuesta en `dish_ingredients`.** Modelo más limpio y más eficiente
  para el acceso por `(dish_id, ingredient_id)` que varias columnas de
  join.
- **Dinero en centavos.** `INT price_cents` + `NUMERIC(12,3)` para
  cantidades de ingredientes.

### 9.2.3. Objetivo de ingeniería: calidad

La estrategia de pruebas con Testcontainers sobre PostgreSQL real
(cap. 7) ha permitido validar no solo el código Java, sino el *sistema*:
dialecto SQL, constraints, RBAC, aislamiento multi-tenant y la máquina de
estados. El resultado es una suite de **38 tests en verde** y una
confianza razonable en que la migración no ha regredos las invariantes de
los requisitos (cap. 3).

### 9.2.4. Objetivo de ingeniería: despliegue

El entorno de despliegue con Docker Compose (cap. 8) entrega un sistema de
"un comando": `mvn package` + `docker-compose up -d` levanta la aplicación
completa en un host con Docker. Los healthchecks y el orden de arranque
resuelven la clase de incidencias de arranque más común en entornos de
integración.

### 9.2.5. Objetivo académico

El trabajo demuestra dominio de la pila Java moderna en un contexto real:
Spring Boot 3, Java 21 (records, sealed patterns), JPA con ID compuesta,
Spring Security + JWT, Testcontainers, Flyway-style de migraciones SQL y
despliegue contenerizado. La comparación con el stack Laravel original no
es retórica: cada diferencia (tipos, validaciones, seguridad) está aplicada
y testeada, no solo discutida.

## 9.3. Limitaciones

- **Frontend.** Fuera de alcance; la compatibilidad de API se ha verificado
  sobre contratos, no sobre flujo de usuario.
- **Carga.** No se han realizado pruebas de estrés con más de unos pocos
  mil ops/s.
- **Multi-nodo.** PostgreSQL se asume single-node; la réplica y la
  partición por tiempo de `orders` quedan para futuro.
- **Observabilidad.** Actuator básico; no se incluye traza distribuida,
  métricas ni alertado.
- **Pagos.** No se incorporan pasarelas; el ciclo de pago se modela solo
  como transición de estado (`served → paid`).

## 9.4. Trabajo futuro

Priorización sugerida (alto → bajo):

1. **Observabilidad productiva.** Micrometer + Prometheus + Grafana, log
   estructurado con trazas correlacionadas.
2. **Pruebas de carga.** Escenario realista TPV (100 usuarios, 20
   pedidos/min) con k6/Gatling.
3. **Contratos de integración.** Pacto entre frontend y backend para
   prevenir regresiones en el shape de respuesta.
4. **Refreshtoken y rotación.** Sesiones más cortas + rotación segura del
   JWT secret.
5. **Multi-tenancy reforzada.** `RLS` en PostgreSQL como red de seguridad
   ante errores de filtrado a nivel de aplicación.
6. **Pagos.** Integración con pasarela (Stripe/PSP local) en el flujo
   `served → paid`.
7. **Frontend.** Reemplazo del frontend heredado por una SPA que aproveche
   la API OpenAPI generada (springdoc).

## 9.5. Cierre

CookFlow, en esta versión, es un sistema TPV completo y portable cuyo
backend ha sido reescrito sobre Spring Boot 3 y PostgreSQL con un modelo de
calidad verificable por 38 tests de integración y un despliegue contenerizado
de un comando. Las decisiones que más valor aportan a largo plazo son las de
modelo (multi-tenancy por fila, dinero en centavos, tipos nativos,
máquina de estados, records inmutables) antes que las de framework. El
trabajo futuro (§9.4) ataca en orden los huecos que quedan en observabilidad,
carga y seguridad de sesión.
