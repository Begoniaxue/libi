import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import './styles/global.css'
import { useUserStore } from './store/user'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Vant)

const userStore = useUserStore()
userStore.initFromStorage()

app.mount('#app')
