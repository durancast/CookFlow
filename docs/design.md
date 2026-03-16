# 🎨 Sistema de Diseño: CookFlow (TPV)

Este documento centraliza las reglas visuales y la configuración de **Tailwind CSS 4** para el frontend del proyecto. El objetivo es mantener una interfaz coherente, moderna y optimizada para entornos de alta intensidad (Dark Mode).

---

## 🌑 Paleta de Colores (Midnight Premium)

Utilizamos una escala de grises azulados (**Zinc**) para evitar el negro puro, lo que mejora el contraste y reduce la fatiga visual del camarero.

| Categoría | Variable CSS | Valor Hex | Clase Tailwind | Uso Principal |
| :--- | :--- | :--- | :--- | :--- |
| **Fondo Base** | `--color-tpv-bg` | `#09090b` | `bg-tpv-bg` | Fondo principal de la App. |
| **Superficie** | `--color-tpv-surface` | `#18181b` | `bg-tpv-surface` | Tarjetas (Cards), Sidebars, Modales. |
| **Bordes** | `--color-tpv-border` | `#27272a` | `border-tpv-border` | Líneas divisorias sutiles. |
| **Acento** | `--color-tpv-accent` | `#6366f1` | `text/bg-tpv-accent` | Botones de acción, precios, estados. |
| **Acento Hover** | `--color-tpv-accent-hover` | `#4f46e5` | `hover:bg-tpv-accent-hover` | Estado de interacción de botones. |
| **Texto Principal** | `--color-tpv-text` | `#fafafa` | `text-tpv-text` | Títulos, precios y nombres de platos. |
| **Texto Muted** | `--color-tpv-text-muted` | `#a1a1aa` | `text-tpv-text-muted` | Descripciones, categorías y etiquetas. |

---

## 🅰️ Tipografía y Escala

Buscamos máxima legibilidad y un aire técnico. Para ello, combinamos una fuente Sans-serif para la interfaz y una Monoespaciada para los datos numéricos (precios).

* **Nombres de Platos:** `font-sans text-lg font-semibold tracking-tight text-tpv-text`
    * *Objetivo:* Lectura rápida y jerarquía clara.
* **Precios y Cantidades:** `font-mono text-xl font-bold text-tpv-accent`
    * *Objetivo:* Al ser monoespaciada, los números no "saltan" al cambiar, facilitando la lectura de tickets y totales.
* **Categorías y Metadatos:** `font-sans text-xs uppercase font-medium tracking-widest text-tpv-text-muted`
    * *Objetivo:* Diferenciar claramente las etiquetas del contenido principal.

---

## 🍱 Componentes de Interfaz (UI Rules)

### 1. Jerarquía de Capas (Elevation)
En el modo oscuro no usamos sombras proyectadas, usamos **contraste de color**:
* **Nivel 0 (Fondo):** `bg-tpv-bg`
* **Nivel 1 (Componentes):** `bg-tpv-surface` + `border border-tpv-border`

### 2. Bordes y Radios
Para dar un aspecto de aplicación moderna y evitar ángulos "duros":
* **Contenedores y Cards:** `rounded-xl` (12px).
* **Botones y Controles:** `rounded-lg` (8px).

### 3. Feedback Visual (Interacción)
* Las **DishCards** deben reaccionar al pasar el cursor para confirmar selección: `hover:border-tpv-accent/50 transition-colors`.
* Los botones principales usan el color de acento para captar la atención del usuario.

---

## 🛠️ Implementación Técnica

La configuración se encuentra en `src/styles/global.css`. Al usar **Tailwind 4**, las variables se inyectan mediante el bloque `@theme`.

> **Nota:** Para que VS Code reconozca las reglas `@theme`, el archivo debe estar configurado con el lenguaje **PostCSS** o **Tailwind CSS**.

```css
@import "tailwindcss";

@theme {
  /* Variables de color corporativas */
  --color-tpv-bg: #09090b;
  /* ... resto de variables ... */
}