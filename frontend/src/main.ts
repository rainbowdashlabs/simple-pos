import {createApp} from 'vue'
import './style.css'
import App from './App.vue'

import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {fas} from '@fortawesome/free-solid-svg-icons'
import {createI18n, DefaultDateTimeFormatSchema, DefaultNumberFormatSchema, I18n} from "vue-i18n";

const numberformats: DefaultNumberFormatSchema = {
    en: {
// @ts-expect-error
        currency: {
            style: 'currency', currency: 'USD', notation: 'standard'
        },
        decimal: {
            style: 'decimal', minimumFractionDigits: 2, maximumFractionDigits: 2
        },
        percent: {
            style: 'percent', useGrouping: false
        }
    },
    de: {
// @ts-expect-error
        currency: {
            style: 'currency', currency: 'EUR', notation: 'standard', currencyDisplay: 'symbol'
        },
        decimal: {
            style: 'decimal', minimumFractionDigits: 2, maximumFractionDigits: 2
        },
        percent: {
            style: 'percent', useGrouping: false
        }


    }
}

const datetimeFormats: DefaultDateTimeFormatSchema = {
    en: {
// @ts-expect-error
        short: {
            year: 'numeric', month: 'short', day: 'numeric'
        },
        long: {
            year: 'numeric', month: 'short', day: 'numeric',
            weekday: 'short', hour: 'numeric', minute: 'numeric'
        }
    },
    de: {
// @ts-expect-error
        short: {
            year: 'numeric', month: 'short', day: 'numeric'
        },
        long: {
            year: 'numeric', month: 'short', day: 'numeric',
            weekday: 'short', hour: 'numeric', minute: 'numeric'
        }
    }
}

const i18n: I18n = createI18n({
    locale: "en",
    messages: {
        en: {
            accounts: "Accounts",
            manage: "Manage",
            date: "Date",
            price: "Price",
            price_total: "Total",
            product: "Product",
            delete: "Delete",
            history: "History",
            deposit: "Deposit",
            overview: "Overview",
            products: "Products",
            inventory: "Inventory",
            name: "Name",
            purchase_price: "Purchase Price",
            container_size: "Container Size",
            pledge: "Pledge",
            pledge_container: "Container Pledge",
            min_stock: "Min. Stock",
            active: "Active",
            category: "Category",
            cash: "Cash",
            stock: "Stock",
            sales: "Sales",
            sales_month: "Sales Month",
            sales_year: "Sales Year",
            prices: "Prices",
            statistic: "Statistik",
            amount: "Amount",
            purchases: "Purchases",
            revenue: "Revenue",
            month: "Month",
            year: "Year"
        },
        de: {
            accounts: "Konten",
            manage: "Verwalten",
            date: "Datum",
            price: "Preis",
            price_total: "Total",
            product: "Produkt",
            delete: "Löschen",
            history: "Verlauf",
            deposit: "Einzahlung",
            overview: "Übersicht",
            products: "Produkte",
            inventory: "Inventar",
            name: "Name",
            purchase_price: "Einkaufspreis",
            container_size: "Packung",
            pledge: "Pfand",
            pledge_container: "Packungspfand",
            min_stock: "Min. Vorrat",
            active: "Aktiv",
            category: "Kategorie",
            cash: "Bargeld",
            stock: "Vorrat",
            sales: "Verkäufe",
            sales_month: "Verkäufe Monat",
            sales_year: "Verkäufe Jahr",
            prices: "Preise",
            statistic: "Statistik",
            amount: "Anzahl",
            purchases: "Einkäufe",
            revenue: "Umsatz",
            profit: "Gewinn",
            month: "Monat",
            year: "Jahr"
        }
    },
    datetime: datetimeFormats,
    // @ts-expect-error
    numberFormats: numberformats
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