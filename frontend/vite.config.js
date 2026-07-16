import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // El front pide /api/... a su propio origen y este proxy lo reenvía al
    // backend. Espeja lo que hace el rewrite de Vercel en producción
    // (ver vercel.json), así el navegador ve un solo dominio en los dos lados
    // y la cookie de sesión es first-party siempre. Sin esto habría que
    // apuntar al backend por su dominio real y pelear con CORS y con el
    // bloqueo de cookies de terceros.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
