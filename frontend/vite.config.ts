import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(() => {
  return {
    plugins: [react()],
    base: process.env.VERCEL ? '/' : '/Smart-EV-charging-infrastructure-optimization/',
  }
})
