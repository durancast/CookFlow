# Capítulo 3. Requisitos

## 3.1. Introducción

Este capítulo recoge los requisitos de CookFlow. Se parte de los requisitos
funcionales implícitos en la API pública y en el modelo de dominio, y se
añaden requisitos no funcionales derivados del contexto de despliegue
(productivo con Docker) y de la migración (compatibilidad de API). La
metodología de priorización es MoSCoW, aplicada de forma pragmática.

## 3.2. Casos de uso principales

| Activo | Casos de uso principales |
|---|---|
| Cliente (público) | Consultar menú del restaurante. |
| Camarero (`waiter`) | Crear pedido, añadir/cambiar platos, cerrar pedido, consultar estado de mesa. |
| Cocina (`kitchen`) | Consultar pedidos en preparación, actualizar estado a `served`. |
| Jefe / Manager (`manager`) | Gestionar mesas, inventario de ingredientes, usuarios, informes. |
| Administrador (`admin`) | Todo lo anterior + alta de tenant, configuración global, auditoría. |

Cada papel es representado por un `UserRole` (`admin`, `manager`, `kitchen`,
`waiter`), mapeado a `ROLE_ADMIN`, `ROLE_MANAGER`, etc. en Spring Security.

## 3.3. Requisitos funcionales

### RF1. Autenticación y sesión
- **RF1.1** *(Must)* Autenticación por email y contraseña.
- **RF1.2** *(Must)* Emisión de token JWT firmado con la clave configurada
  (`JWT_SECRET`) y expiración acotada en el tiempo.
- **RF1.3** *(Should)* Endpoint `GET /api/auth/me` que devuelve el usuario
  autenticado y su rol.
- **RF1.4** *(Could)* Refresh token / rotación de sesión.

### RF2. Menú (multi-tenant)
- **RF2.1** *(Must)* Endpoint público `GET /api/public/menu` que devuelve
  categorías, platos e ingredientes del tenant indicado.
- **RF2.2** *(Must)* CRUD autenticado sobre categorías, platos e ingredientes.
- **RF2.3** *(Should)* Asociación plato-ingredientes con cantidad y unidad.

### RF3. Mesas y pedidos
- **RF3.1** *(Must)* CRUD de mesas con estado `free | occupied | pending`.
- **RF3.2** *(Must)* Creación de pedido ligado a una mesa y a un tenant.
- **RF3.3** *(Must)* Transición de estado del pedido respetando la máquina
  de estados `pending -> preparing -> served -> paid`.
- **RF3.4** *(Should)* Artículos de pedido con cantidad y observaciones.
- **RF3.5** *(Could)* Reparto de cuenta y descuentos.

### RF4. Usuarios y roles
- **RF4.1** *(Must)* Alta de usuarios con rol asignado (admin).
- **RF4.2** *(Must)* Hash de contraseña (BCrypt) en reposo.
- **RF4.3** *(Should)* Cambio de papel dentro de un tenant.

### RF5. Informes y dashboard
- **RF5.1** *(Should)* Endpoint de métricas agregadas (ingresos, pedidos por
  estado, top platos) para el dashboard.
- **RF5.2** *(Could)* Exportación a CSV/Excel.

## 3.4. Requisitos no funcionales

| ID | Requisito | Nivel |
|---|---|---|
| RNF1 | Tiempo de respuesta p50 < 300 ms en API de menú local. | Must |
| RNF2 | Disponibilidad del entorno de desarrollo con un único comando (`docker-compose up`). | Must |
| RNF3 | Las contraseñas nunca se almacenan en claro. | Must |
| RNF4 | Toda mutación de datos validada (DTO + constraint DB). | Must |
| RNF5 | Portabilidad del despliegue: imagen JRE sin dependencias externas instaladas en el host. | Must |
| RNF6 | Reproductibilidad: build del backend idempotente. | Should |
| RNF7 | Compatibilidad de comportamiento con la API de la versión Laravel preexistente. | Must |

## 3.5. Restricciones y supuestos

- El frontend consume la API vía HTTP/JSON; no hay APIs gRPC ni websocket
  en la primera iteración.
- Se asume single-node para PostgreSQL; la réplica queda como trabajo futuro.
- Los datos son confidenciales por tenant: todo endpoint autenticado debe
  filtrar por `tenant_id` del usuario autenticado (exigencia de seguridad,
  no solo funcional).

## 3.6. Conclusión del capítulo

Se han enumerado los casos de uso por perfil y una lista priorizada de
requisitos funcionales y no funcionales. Estos requisitos se convierten en
decisiones de diseño en los capítulos 4 (arquitectura) y 5 (base de datos),
y se verifican en el capítulo 7 (pruebas). RNF7 — compatibilidad de API —
es el hilo conductor de la migración.
