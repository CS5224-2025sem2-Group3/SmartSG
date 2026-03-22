import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { initializeAuth } from './services/authService'

async function bootstrap() {
  await initializeAuth()
  createApp(App).use(router).mount('#app')
}

bootstrap()
