import {createI18n} from "vue-i18n";
import en from "@/locales/en.json";
import de from "@/locales/de.json";

const numberformats = {
    en: {
        currency: {
            style: 'currency', currency: 'EUR', notation: 'standard', currencyDisplay: 'symbol'
        },
        whole_currency: {
            style: 'currency', currency: 'EUR', notation: 'standard', currencyDisplay: 'symbol',
            maximumFractionDigits: 0
        },
        unit_price: {
            style: 'currency', currency: 'EUR', notation: 'standard', currencyDisplay: 'symbol',
            minimumFractionDigits: 2, maximumFractionDigits: 4
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
        whole_currency: {
            style: 'currency', currency: 'EUR', notation: 'standard', currencyDisplay: 'symbol',
            maximumFractionDigits: 0
        },
        unit_price: {
            style: 'currency', currency: 'EUR', notation: 'standard', currencyDisplay: 'symbol',
            minimumFractionDigits: 2, maximumFractionDigits: 4
        },
        decimal: {
            style: 'decimal', minimumFractionDigits: 2, maximumFractionDigits: 2
        },
        percent: {
            style: 'percent', useGrouping: false
        }
    }
} as const

/**
 * The languages the interface is translated into.
 */
export type Language = keyof typeof numberformats

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

/**
 * The translations and number and date formats of the interface, shared by components and plain modules.
 */
export const i18n = createI18n({
    locale: "de",
    messages: {en, de},
    datetimeFormats: datetimeFormats,
    numberFormats: numberformats
})
