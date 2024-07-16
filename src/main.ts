import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import {fas} from '@fortawesome/free-solid-svg-icons'

const app = createApp(App);

app.config.errorHandler = err => {
    console.error(err)
}

library.add(fas)

app.component("font-awesome-icon", FontAwesomeIcon)
app.mount('#app')
