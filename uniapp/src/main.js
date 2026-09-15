import { createSSRApp } from 'vue'
import App from './App.vue'
import CustomTabbar from './components/CustomTabbar.vue'

export function createApp() {
  const app = createSSRApp(App)
  app.component('CustomTabbar', CustomTabbar)
  return {
    app
  }
}