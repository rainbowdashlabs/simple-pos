import {createApp} from 'vue'
import '@/style.css'
import App from '@/App.vue'

import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {fas} from '@fortawesome/free-solid-svg-icons'
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
library.add(fas)

app.component("font-awesome-icon", FontAwesomeIcon)
app.mount('#app')
