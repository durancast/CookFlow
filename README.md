# 🍽️ CookFlow - Sistema de Gestión para Restaurantes

<div align="center">

**CookFlow** es una solución SaaS integral diseñada para optimizar la operativa de restaurantes, permitiendo la gestión de pedidos, control de mesas y un terminal de punto de venta (TPV) ágil y moderno.

Este proyecto se desarrolla como **Trabajo de Fin de Grado (TFG)** para el ciclo de **2º de DAW**.

![Astro](https://img.shields.io/badge/Astro-BC52EE?style=flat-square&logo=astro&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=react&logoColor=black)
![Tailwind CSS 4](https://img.shields.io/badge/Tailwind_CSS_4-38B2AC?style=flat-square&logo=tailwind-css&logoColor=white)
![Laravel 12](https://img.shields.io/badge/Laravel_12-FF2D20?style=flat-square&logo=laravel&logoColor=white)
![PHP 8.2+](https://img.shields.io/badge/PHP_8.2+-777BB4?style=flat-square&logo=php&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)

</div>

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías-principales)
- [Equipo](#-equipo-de-desarrollo)

---

## ✨ Características

### Gestión de Pedidos
- ✅ Creación y seguimiento de pedidos en tiempo real
- ✅ Asignación de meseros a mesas
- ✅ Estados de pedidos: Pendiente → En Preparación → Servido → Pagado
- ✅ Historial completo de pedidos

### Gestión de Mesas
- ✅ Registro de mesas con capacidad configurable
- ✅ Estados de mesas: Libre, Ocupada, Pendiente
- ✅ Generación de códigos QR dinámicos por mesa
- ✅ Visualización del pedido activo por mesa

### Catálogo de Productos
- ✅ Organización por categorías
- ✅ Precios y descripciones
- ✅ Imágenes de productos
- ✅ Gestión por administrador

### Autenticación y Autorización
- ✅ Sistema de roles: Admin, Mesero
- ✅ Autenticación con Laravel Sanctum (API Tokens)
- ✅ Acceso basado en roles

### Terminal TPV (Front-End)
- ✅ Interfaz responsiva con Astro + React + Tailwind
- ✅ Visualización del menú categorizado
- ✅ Carrito de compras integrado
- ✅ Gestión de mesas en tiempo real

---

## 🚀 Tecnologías Principales

### Frontend
- **Astro 5.17** - Framework moderno con renderizado parcial
- **React 19** - Componentes interactivos
- **Tailwind CSS 4** - Estilos utilitarios
- **TypeScript** - Tipado estático

### Backend
- **Laravel 12** - Framework PHP robusto
- **Laravel Sanctum** - Autenticación API con tokens
- **MySQL** - Base de datos relacional
- **Composer** - Gestor de dependencias PHP

### Herramientas
- **pnpm** - Gestor de paquetes rápido (Node)
- **Vite** - Build tool para assets
- **PHPUnit** - Testing para PHP
- **Artisan** - CLI de Laravel

---

## 📚 Documentación Adicional

- [Setup y Configuración](./docs/setup.md) - Guía detallada de instalación
- [Diseño de Base de Datos](./docs/db.md) - Modelo ER y relaciones
- [Documentación API](./docs/api.md) - Endpoints y ejemplos
- [Autenticación](./docs/auth.md) - Sistema de roles y tokens
- [Órdenes y Flujo](./docs/orders.md) - Lógica de negocio
- [Diseño UI/UX](./docs/design.md) - Guía de estilos

---

## 👥 Equipo de Desarrollo

* **Alejandro Durán** - *Frontend*
* **Nizar** - *Backend & API Developer*
* **Luis** - *Database & Data Modeling*

---

> [!NOTE]
> Este es un proyecto educativo. Todas las decisiones técnicas están documentadas en la carpeta `/docs`.
