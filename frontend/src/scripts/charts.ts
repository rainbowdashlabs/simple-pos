import {store} from "./store.ts";

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
 * Formats euro amounts in the given locale, with cents.
 */
export function euro(locale: string): (value: number) => string {
    const format = new Intl.NumberFormat(locale, {style: "currency", currency: "EUR"})
    return value => format.format(value)
}

/**
 * Formats euro amounts in the given locale without cents, for axis labels.
 */
export function wholeEuro(locale: string): (value: number) => string {
    const format = new Intl.NumberFormat(locale, {style: "currency", currency: "EUR", maximumFractionDigits: 0})
    return value => format.format(value)
}
