import {createApp} from 'vue'
import './style.css'
import App from './App.vue'

import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {fas} from '@fortawesome/free-solid-svg-icons'
import {createI18n, I18n} from "vue-i18n";

const i18n: I18n = createI18n({
    locale: "en",
    messages: {
        en: {
            accounts: "Accounts",
            manage: "Manage"
        },
        de: {
            accounts: "Konten",
            manage: "Verwalten"
        }
    }
})

const app = createApp(App);

app.config.errorHandler = err => {
    console.error(err)
}
// noinspection TypeScriptValidateTypes
app.use(i18n)
library.add(fas)

app.component("font-awesome-icon", FontAwesomeIcon)
app.mount('#app')
