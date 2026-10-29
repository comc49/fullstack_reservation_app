import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Send API calls from the dev server (port 5173) to Spring Boot (port 8080).
    // The browser only ever talks to 5173, so there are no CORS errors in development.
    proxy: {
      '/v1': 'http://localhost:8080',
    },
  },
})
