# Capítulo 1. Introducción

## 1.1. Contexto

En el sector de la restauración, la gestión de pedidos y del ticket de caja
(terminal de punto de venta, TPV) suele apoyarse en herramientas genéricas de
facturación que no capturan las particularidades operativas del negocio:
mesas y comandas, menús con productos compuestos (platos con ingredientes),
roles muy diferenciados de personal (camarero, jefe, administración) y la
necesidad de consultar el estado de la sala y los pedidos en tiempo real.
CookFlow nace como una alternativa específica para este dominio: una
aplicación web full-stack que cubre el ciclo completo de venta en sala,
desde la consulta del menú por parte del cliente hasta la cierre de
informe por parte de la administración.

El proyecto tiene además una dimensión académica fundamental: parte de una
implementación preexistente construida sobre **Laravel (PHP) y MySQL**, y
su evolución consiste en **migrar el backend a Spring Boot (Java 21) y el
motor de datos a PostgreSQL**, conservando la API pública y la base de datos
esquemáticamente equivalente. Esta doble vertiente — Producto + migración —
define el alcance de esta memoria.

## 1.2. Objetivos

Los objetivos del trabajo se organizan en tres bloques:

1. **Objetivos de producto.**
   - Gestión de menús estructurados: categorías, platos e ingredientes, con
     soporte multi-tenant mediante el campo `tenant_id`.
   - Gestión de mesas y de pedidos/comandas con estados de vida
     (abierta, en cocina, cerrada...).
   - Control de acceso por roles (RBAC) con autenticación JWT.
   - Consultas de informe y dashboard para la administración.

2. **Objetivos de ingeniería.**
   - Migrar el backend de Laravel a Spring Boot 3 preservando la superficie
     de API documentada en `docs/api.md`.
   - Migrar el esquema de datos de MySQL a PostgreSQL, aprovechando tipos
     nativos como `ENUM` y mejorando el modelado de la relación
     plato-ingredientes (`dish_ingredients`).
   - Establecer una estrategia de pruebas automatizadas con Testcontainers
     sobre PostgreSQL real.
   - Definir un entorno de despliegue reproducible basado en Docker Compose.

3. **Objetivos académicos.**
   - Demostrar el dominio de la pila Spring Boot (capas, DTO, seguridad,
     validación, excepciones).
   - Comparar ambos stacks (Laravel/MySQL vs Spring/PostgreSQL) en lo que
     afecta directamente al diseño: gestión de transacciones, constraints,
     tipos de datos y seguridad.

## 1.3. Alcance y limitaciones

El alcance se limita al **backend** de la aplicación: API REST, modelo de
datos, seguridad y despliegue. El frontend (SPA) y el proceso de diseño de
UI quedan fuera de esta memoria, aunque la API que consume queda
documentada. La migración se considera completa cuando la API del nuevo
backend responde de forma compatible y la suite de pruebas pasa sobre
PostgreSQL real.

Entre las limitaciones explícitas: no se implementan pasarelas de pago, no
se prevén despliegues multi-región y la autenticación se limita a
credenciales + JWT sin refresh tokens ni MFA.

## 1.4. Estructura de la memoria

Tras este capítulo se presentan las tecnologías empleadas (capítulo 2), los
requisitos funcionales y no funcionales (capítulo 3), la arquitectura de la
aplicación (capítulo 4), el modelo de base de datos (capítulo 5), la
implementación del backend (capítulo 6), la estrategia de calidad y pruebas
(capítulo 7) y la migración y despliegue con Docker (capítulo 8). El capítulo
9 recoge conclusiones y trabajo futuro.

## 1.5. Conclusión del capítulo

Este capítulo ha definido CookFlow como un TPV web para restaurante, ha
planteado los objetivos de producto, ingeniería y aprendizaje del trabajo y
ha acotado el alcance al backend. Los capítulos que siguen desarrollan cada
uno de esos objetivos con el nivel de detalle necesario para reproducir el
resultado.
