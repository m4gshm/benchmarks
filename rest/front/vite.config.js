import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  console.log(`command ${command}, mode ${mode}`)
  const env = loadEnv(mode, process.cwd(), '')
  console.log(`env.BACKEND_URL ${env.BACKEND_URL}`)
  return {
    plugins: [react()],
    define: {
      "env.BACKEND_URL": JSON.stringify(env.BACKEND_URL),
    },
  }
})
