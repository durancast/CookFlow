// @ts-check
import { defineConfig } from 'astro/config';
import tailwindcss from '@tailwindcss/vite';
import react from '@astrojs/react';

// https://astro.build/config
export default defineConfig({
  vite: {
    //@ts-ignore
    plugins: [tailwindcss()],
    
    server: {
      proxy: {
        // Redirige las llamadas de API
        '/api': { 
          target: 'http://127.0.0.1:8000',
          changeOrigin: true 
        },
        // Redirige el acceso al storage (si usas el link simbólico)
        '/storage': { 
          target: 'http://127.0.0.1:8000',
          changeOrigin: true 
        },
        // Redirige el acceso a la carpeta pública directa (el Plan B)
        '/products': { 
          target: 'http://127.0.0.1:8000',
          changeOrigin: true 
        }
      }
    }
  },

  integrations: [react()]
});