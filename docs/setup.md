# 🛠️ Guía de Configuración Inicial (Setup)

Este documento detalla los pasos necesarios para levantar el proyecto **CookFlow** en un entorno de desarrollo local. Sigue este orden para evitar errores de dependencias o base de datos.

---------------------------------------------------------------------------


-----------------------------------------------
## ☀️ Rutina Diaria (Daily Workflow)


Para evitar conflictos y trabajar siempre con la última versión del equipo, sigue estos pasos cada vez que te sientes a programar:

### 1. Sincronizar con el equipo
Antes de tocar una sola línea de código, descarga lo que hayan hecho tus compañeros:

```bash
# Asegúrate de estar en develop
git checkout develop

# Baja los cambios
git pull origin develop
2. Actualizar dependencias y DB
```
Si ves que en el git pull han cambiado archivos como package.json, composer.json o la carpeta migrations, ejecuta:

En /frontend: pnpm install

En /backend: composer install y php artisan migrate


🛑 Antes de terminar el día (Cierre)
No dejes el código solo en tu PC. Súbelo para que los demás puedan usarlo mañana:

```bash
git add .

git commit -m "feat: descripción de lo que has hecho"

git push origin develop

```

-----------------------------------------------------------------------------------

## 📋 Requisitos Previos

Antes de empezar, asegúrate de tener instalado lo siguiente en tu sistema:

* **PHP 8.2+** (y la herramienta **Composer**)
* **Node.js** (Versión LTS recomendada)
* **pnpm** (Instalación: `npm install -g pnpm`)
* **MySQL** (Vía XAMPP, Laragon o MySQL Installer)
* **Git**

---

## 1. 📂 Clonar y Preparar el Repositorio

Abre tu terminal y ejecuta:

```bash
# Clonar el repositorio
git clone [https://github.com/alexd2212/CookFlow.git](https://github.com/alexd2212/CookFlow.git)
cd CookFlow

# CAMBIAR A LA RAMA DE DESARROLLO (Importante)
git checkout develop
```
-------------------------------------------------------------------------------------

## 2. 🖥️ Configuración del Backend (Laravel 11)
Entra en la carpeta del servidor y prepara las dependencias de PHP:

```bash
cd backend

# Instalar librerías de Laravel
composer install

# Crear el archivo de configuración local
cp .env.example .env

# Generar la clave única de la aplicación
php artisan key:generate
```

---------------------------------------------------------------------------------------

## 3.🗄️ Base de Datos MySQL
Crea una base de datos vacía en tu MySQL local llamada cookflow.

Abre el archivo .env que acabas de crear y edita las líneas de conexión:
```
Fragmento de código
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=cookflow
DB_USERNAME=root      # Tu usuario de MySQL
DB_PASSWORD=          # Tu contraseña de MySQL
```

Ejecuta las migraciones para crear las tablas:
```bash
php artisan migrate
```
-------------------------------------------------------------------------------------

## 4. 🎨 Configuración del Frontend (Astro + Tailwind 4)
Abre otra terminal o vuelve a la raíz del proyecto y entra en la carpeta del cliente:

```Bash
cd frontend

# Instalar dependencias con pnpm
pnpm install

# Arrancar el servidor de desarrollo
pnpm dev
```
-----------------------------------------------------------------------
## 🚀 Cómo trabajar en el día a día
Para que el sistema funcione (TPV + API), ambos servicios deben estar encendidos:

Terminal 1 (Backend): php artisan serve (Corre en http://127.0.0.1:8000)

Terminal 2 (Frontend): pnpm dev (Corre en http://localhost:4321)

**Solo cuando necesitemos ambos servicios a la vez**
**Mientras se crea todo, puede estar solamente el que se necesita**

-------------------------------------------------------------------------------
## ⚠️ Solución de Problemas (Troubleshooting)

Error de CORS: Si el Front no recibe datos del Back, Nizar debe revisar backend/config/cors.php para permitir el origen http://localhost:4321.

Vite / Tailwind no carga: Asegúrate de que estás usando pnpm dev y no npm run dev.

Permisos de Storage: En Windows no suele fallar, pero si Laravel da error de escritura, ejecuta:

```bash
php artisan storage:link
```
### 🛠️ Requisito adicional para QRs (Backend)
El sistema genera códigos QR dinámicos y requiere la librería **GD** de PHP:
1. En `php.ini`, busca `;extension=gd` y quita el `;`.
2. Reinicia tu servidor (Apache/Laragon).