// @ts-check
import { defineConfig } from 'astro/config';
import tailwindcss from '@tailwindcss/vite';
import react from '@astrojs/react';
import node from '@astrojs/node';

// https://astro.build/config
export default defineConfig({
  output: 'server',
  adapter: node({ mode: 'standalone' }),

  vite: {
    //@ts-ignore
    plugins: [tailwindcss()],

    server: {
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:8000',
          changeOrigin: true
        },
        '/storage': {
          target: 'http://127.0.0.1:8000',
          changeOrigin: true
        },
        '/products': {
          target: 'http://127.0.0.1:8000',
          changeOrigin: true
        }
      }
    }
  },

  integrations: [react()]
});
