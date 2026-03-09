# Diseño de la Base de Datos - CookFlow

Este documento describe la estructura relacional del backend para la gestión del menú.

## Diagrama Entidad-Relación (1:N)

La relación principal es de **uno a muchos** (1:N) entre las tablas `categories` y `products`. Esto permite que una categoría (ej: "Hamburguesas") agrupe múltiples productos, asegurando la integridad referencial.

![Diagrama de Base de Datos](./images/diagrama_db.png)

## Estructura de las Tablas

### Tabla: Categories
- `id`: Identificador único (Primary Key).
- `name`: Nombre de la categoría (bebidas, entrantes, hamburguesas, etc.).

### Tabla: Products
- `id`: Identificador único (Primary Key).
- `category_id`: Clave foránea (Foreign Key) relacionada con categories.
- `name`: Nombre del producto.
- `price`: Precio (Decimal 8,2).
- `description`: Descripción del plato.
- `image`: URL o ruta de la imagen del producto.

---
*Diseño realizado por: Luis*