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
            style: 'currency', currency: 'EUR', notation: 'standard', currencyDisplay: 'symbol'
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
            checkout_choose_account: "Choose an account",
            checkout_no_accounts: "There are no accounts yet. Create one first.",
            checkout_cart_empty: "The cart is empty.",
            manage: "Manage",
            date: "Date",
            price: "Price",
            price_total: "Total",
            product: "Product",
            delete: "Delete",
            cancel: "Cancel",
            confirm_delete: "Are you sure you want to delete this? This action cannot be undone.",
            history: "History",
            deposit: "Deposit",
            back_to_pos: "Back to POS",
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
            inactive: "Inactive",
            category: "Category",
            categories: "Categories",
            cash: "Cash",
            stock: "Stock",
            sales: "Sales",
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
            out_of_stock: "Out of Stock",
            total_revenue: "Total Revenue",
            total_profit: "Total Profit",
            total_sales: "Total Sales",
            last_days: "Last {days} days",
            no_data: "No data",
            revenue_trend: "Revenue Trend",
            revenue_by_category: "Revenue by Category",
            cash_overview: "Cash Overview",
            top_sellers: "Top Sellers",
            worst_sellers: "Worst Sellers",
            settings: "Settings",
            color_theme: "Color Theme",
            language: "Language",
            users: {
                title: "Users",
                create: "New user",
                create_button: "Create",
                username: "Username",
                password: "Password",
                new_password: "New password",
                password_hint: "Passwords need at least {min} characters. New users are enabled right away.",
                role: "Role",
                roles: {ADMIN: "Administrator", STAFF: "Staff", USER: "User"},
                enabled: "Enabled",
                reset_password: "Set password",
                save: "Save",
                you: "you",
                disabled: "disabled",
                own_account_hint: "Another administrator has to change your own role and status.",
                password_set: "The password was set. Existing sessions of this user end at their next refresh.",
                own_password_hint: "Setting your own password ends all your sessions at their next refresh, this one included; then log in with the new password."
            },
            no_access: {
                title: "No access",
                text: "Your account cannot use this part of the application yet. Ask an administrator to give you access."
            },
            logout: "Log out",
            errors: {
                unexpected: "Something went wrong. Please try again.",
                network: "The server could not be reached. Check the connection and try again.",
                dismiss: "Dismiss",
                invalid_credentials: "Wrong username or password.",
                account_disabled: "Your account is disabled. Ask an administrator to enable it.",
                too_many_logins: "Too many failed logins. Try again in {minutes} minute. | Too many failed logins. Try again in {minutes} minutes.",
                too_many_requests: "Too many requests. Try again in a moment."
            }
        },
        de: {
            accounts: "Konten",
            checkout_choose_account: "Konto auswählen",
            checkout_no_accounts: "Es gibt noch keine Konten. Lege zuerst eines an.",
            checkout_cart_empty: "Der Warenkorb ist leer.",
            manage: "Verwalten",
            date: "Datum",
            price: "Preis",
            price_total: "Total",
            product: "Produkt",
            delete: "Löschen",
            cancel: "Abbrechen",
            confirm_delete: "Bist du sicher, dass du das löschen möchtest? Diese Aktion kann nicht rückgängig gemacht werden.",
            history: "Verlauf",
            deposit: "Einzahlung",
            back_to_pos: "Zurück zur Kasse",
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
            inactive: "Inaktiv",
            category: "Kategorie",
            categories: "Kategorien",
            cash: "Bargeld",
            stock: "Vorrat",
            sales: "Verkäufe",
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
            out_of_stock: "Nicht vorrätig",
            total_revenue: "Gesamtumsatz",
            total_profit: "Gesamtgewinn",
            total_sales: "Gesamtverkäufe",
            last_days: "Letzte {days} Tage",
            no_data: "Keine Daten",
            revenue_trend: "Umsatzverlauf",
            revenue_by_category: "Umsatz nach Kategorie",
            cash_overview: "Kassenübersicht",
            top_sellers: "Bestseller",
            worst_sellers: "Ladenhüter",
            settings: "Einstellungen",
            color_theme: "Farbthema",
            language: "Sprache",
            users: {
                title: "Konten",
                create: "Neues Konto",
                create_button: "Anlegen",
                username: "Benutzername",
                password: "Passwort",
                new_password: "Neues Passwort",
                password_hint: "Passwörter brauchen mindestens {min} Zeichen. Neue Konten sind sofort aktiv.",
                role: "Rolle",
                roles: {ADMIN: "Administration", STAFF: "Personal", USER: "Basis"},
                enabled: "Aktiv",
                reset_password: "Passwort setzen",
                save: "Speichern",
                you: "du",
                disabled: "deaktiviert",
                own_account_hint: "Deine eigene Rolle und deinen Status muss eine andere Person aus der Administration ändern.",
                password_set: "Das Passwort wurde gesetzt. Bestehende Sitzungen dieses Kontos enden bei ihrer nächsten Erneuerung.",
                own_password_hint: "Ein neues eigenes Passwort beendet alle deine Sitzungen bei ihrer nächsten Erneuerung, auch diese; melde dich dann mit dem neuen Passwort an."
            },
            no_access: {
                title: "Kein Zugriff",
                text: "Dein Konto kann diesen Teil der Anwendung noch nicht nutzen. Lass dir den Zugriff von der Administration freischalten."
            },
            logout: "Abmelden",
            errors: {
                unexpected: "Etwas ist schiefgelaufen. Bitte versuche es erneut.",
                network: "Der Server ist nicht erreichbar. Prüfe die Verbindung und versuche es erneut.",
                dismiss: "Schließen",
                invalid_credentials: "Benutzername oder Passwort ist falsch.",
                account_disabled: "Dein Konto ist deaktiviert. Lass es von der Administration freischalten.",
                too_many_logins: "Zu viele fehlgeschlagene Anmeldungen. Versuche es in {minutes} Minute erneut. | Zu viele fehlgeschlagene Anmeldungen. Versuche es in {minutes} Minuten erneut.",
                too_many_requests: "Zu viele Anfragen. Versuche es gleich noch einmal."
            }
        }
    },
    datetimeFormats: datetimeFormats,
    numberFormats: numberformats
})

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
