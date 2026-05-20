import {createApp} from 'vue'
import '@/style.css'
import App from '@/App.vue'

import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {fas} from '@fortawesome/free-solid-svg-icons'
import {createI18n} from "vue-i18n";

const numberformats = {
    en: {
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
} as const

const datetimeFormats = {
    en: {
        short: {
            year: 'numeric', month: 'short', day: 'numeric'
        },
        long: {
            year: 'numeric', month: 'short', day: 'numeric',
            weekday: 'short', hour: 'numeric', minute: 'numeric'
        }
    },
    de: {
        short: {
            year: 'numeric', month: 'short', day: 'numeric'
        },
        long: {
            year: 'numeric', month: 'short', day: 'numeric',
            weekday: 'short', hour: 'numeric', minute: 'numeric'
        }
    }
} as const

const i18n = createI18n({
    locale: "de",
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
            withdraw: "Withdraw",
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
            categories: "Categories",
            cash: "Cash",
            stock: "Stock",
            sales: "Sales",
            sales_month: "Sales Month",
            sales_year: "Sales Year",
            prices: "Prices",
            statistic: "Statistic",
            amount: "Amount",
            purchases: "Purchases",
            revenue: "Revenue",
            month: "Month",
            year: "Year",
            storage: "Storage",
            piece: "Piece",
            pieces: "Pieces",
            total: "Total",
            difference: "Difference",
            counted: "Counted",
            count: "Count",
            note: "Notiz",
            type: "Type",
            user: "User",
            of: "of",
            ingredients: "Ingredients",
            other: "Other",
            null: "Unknown",
            undefined: "Undefined",
            purchase: "Purchase",
            balance: "Balance",
            stock_projection: "Stock Projection",
            daily_consumption: "Daily consumption",
            days_remaining: "Days remaining",
            days: "Days",
            profit: "Profit",
            low_stock: "Low Stock",
            total_revenue: "Total Revenue",
            total_profit: "Total Profit",
            total_sales: "Total Sales",
            this_month: "This month",
            no_data: "No data",
            revenue_trend: "Revenue Trend",
            revenue_by_category: "Revenue by Category",
            cash_overview: "Cash Overview"
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
            withdraw: "Auszahlung",
            overview: "Übersicht",
            products: "Produkte",
            inventory: "Inventar",
            name: "Name",
            purchase_price: "Einkaufspreis",
            container_size: "Packung",
            container: "Packungen",
            pledge: "Pfand",
            pledge_container: "Packungspfand",
            min_stock: "Min. Vorrat",
            active: "Aktiv",
            category: "Kategorie",
            categories: "Kategorien",
            cash: "Bargeld",
            stock: "Vorrat",
            sales: "Verkäufe",
            sales_month: "Verkäufe Monat",
            sales_year: "Verkäufe Jahr",
            prices: "Preise",
            statistic: "Statistik",
            amount: "Betrag",
            purchases: "Einkäufe",
            revenue: "Umsatz",
            profit: "Gewinn",
            month: "Monat",
            year: "Jahr",
            storage: "Lager",
            piece: "Einheiten",
            pieces: "Einheiten",
            total: "Total",
            difference: "Differenz",
            counted: "Gezählt",
            count: "Anzahl",
            note: "Notiz",
            type: "Art",
            user: "Nutzer",
            of: "von",
            ingredients: "Zutaten",
            other: "Sonstige",
            null: "Unbekannt",
            undefined: "Undefiniert",
            purchase: "Einkauf",
            balance: "Guthaben",
            stock_projection: "Bestandsprognose",
            daily_consumption: "Tagesverbrauch",
            days_remaining: "Tage verbleibend",
            days: "Tage",
            low_stock: "Niedriger Bestand",
            total_revenue: "Gesamtumsatz",
            total_profit: "Gesamtgewinn",
            total_sales: "Gesamtverkäufe",
            this_month: "Dieser Monat",
            no_data: "Keine Daten",
            revenue_trend: "Umsatzverlauf",
            revenue_by_category: "Umsatz nach Kategorie",
            cash_overview: "Kassenübersicht"
        }
    },
    datetimeFormats: datetimeFormats,
    numberFormats: numberformats
})

import router from '@/router.ts'

const app = createApp(App);

app.config.errorHandler = err => {
    console.error(`Unexpected Error:`, err)
}
app.use(router)
app.use(i18n)
library.add(fas)

app.component("font-awesome-icon", FontAwesomeIcon)
app.mount('#app')
