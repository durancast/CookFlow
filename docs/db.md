# Diseño de la Base de Datos - CookFlow
Nombre de la BBDD = cookflow_db

Este documento describe la estructura relacional del backend para la gestión del menú.

## Diagrama Entidad-Relación (1:N)

La relación principal es de **uno a muchos** (1:N) entre las tablas `categories` y `products`. Esto permite que una categoría (ej: "Hamburguesas") agrupe múltiples productos, asegurando la integridad referencial.

![Diagrama de Base de Datos](./images/diagrama_db.png)

## Estructura de las Tablas

### Tabla: Categories
- `id`: Identificador único (Primary Key).
- `name`: Nombre de la categoría (bebidas, entrantes, hamburguesas, etc.).
- `slug` : Versión del nombre para las URL's (hamburguesas)

### Tabla: Products
- `id`: Identificador único (Primary Key).
- `name`: Nombre del producto.
- `price`: Precio (Decimal 8,2).
- `description`: Descripción del plato.
- `image`: URL o ruta de la imagen del producto.
- `category_id`: Clave foránea (Foreign Key) relacionada con categories.
---
*Diseño realizado por: Luis*