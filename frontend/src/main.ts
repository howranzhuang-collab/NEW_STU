import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import router from './router'
import './style.css'
import App from './App.vue'

const app = createApp(App)

// 临时验证：检查环境变量
console.log('所有环境变量:', import.meta.env)
console.log('当前 VITE_API_BASE_URL:', import.meta.env.VITE_API_BASE_URL)

app.use(ElementPlus)
app.use(createPinia())
app.use(router)

app.mount('#app')
