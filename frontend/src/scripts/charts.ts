import {store} from "./store.ts";
import {i18n, Language} from "@/i18n.ts";

/**
 * One entry of what echarts passes to a tooltip formatter.
 */
export interface TooltipParam {
    marker: string
    name: string
    seriesName: string
    value: number
    dataIndex: number
    axisValue?: string
    percent?: number
}

/**
 * The colour of chart texts, matching light and dark mode.
 */
export function chartTextColor(): string {
    return store.theme === "dark" ? "#e0ecf6" : "#121417"
}

/**
 * Formats money in the given language, with cents, through the same currency format as the rest of the interface.
 * Charts pass the locale they read, so they render again when the language changes.
 */
export function euro(locale: string): (value: number) => string {
    return value => i18n.global.n(value, 'currency', locale as Language)
}

/**
 * Formats money in the given language without cents, for axis labels.
 */
export function wholeEuro(locale: string): (value: number) => string {
    return value => i18n.global.n(value, 'whole_currency', locale as Language)
}
