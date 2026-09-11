# Contexto de migración CookFlow

- Proyecto: TPV para restaurante (TFG DAW).
- Backend actual: Laravel 12 + MySQL.
- Backend objetivo: Spring Boot 3 + PostgreSQL.
- Frontend: Astro + React (se mantiene por ahora).
- Requisitos clave:
  - Multi-tenant preparado (tenant_id en todas las tablas).
  - Ingredientes con cantidad + unidad por plato (dish_ingredients).
  - Auth JWT en lugar de Sanctum.
  - Mismos dominios: users, categories, dishes/products, ingredients, dish_ingredients, dining_tables, orders, order_items.