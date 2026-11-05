import {createApp} from 'vue'
import '@/style.css'
import App from '@/App.vue'

import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {registerIcons} from '@/icons.ts'
import {i18n} from "@/i18n.ts";



import router from '@/router.ts'
import {notifyError} from '@/scripts/notifications.ts'

const app = createApp(App);

app.config.errorHandler = err => notifyError(err)
window.addEventListener("unhandledrejection", event => {
    notifyError(event.reason)
    event.preventDefault()
})
app.use(router)
app.use(i18n)
registerIcons()

app.component("font-awesome-icon", FontAwesomeIcon)
app.mount('#app')
