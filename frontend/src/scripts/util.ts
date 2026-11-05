import {Timeframe} from "./product.ts";

export class Timeframes {
    static readonly year: Timeframe = new Timeframe("year", 365)
    static readonly quarter: Timeframe = new Timeframe("quarter", 90)
    static readonly month: Timeframe = new Timeframe("month", 30)
    static readonly week: Timeframe = new Timeframe("week", 7)
}

export function buildDarkMode(color: string, prefix: string = "bg") {
    return `${prefix}-${color} dark:${prefix}-${color}-d`
}

export function roundCurrency(number: number) {
    return Math.round(number * 100) / 100
}

/**
 * The calendar date of `day` in the browser's zone, as `yyyy-mm-dd`, the form the backend sends business dates in.
 */
export function localDateKey(day: Date): string {
    const month = String(day.getMonth() + 1).padStart(2, "0")
    const date = String(day.getDate()).padStart(2, "0")
    return `${day.getFullYear()}-${month}-${date}`
}

/**
 * A `yyyy-mm-dd` business date as a local `Date` at midnight. `new Date("yyyy-mm-dd")` would read it as UTC midnight
 * and show the day before in zones west of UTC.
 */
export function parseLocalDate(value: string): Date {
    const [year, month, date] = value.split("-").map(Number)
    return new Date(year, month - 1, date)
}

/**
 * The rows of a daily statistic for every day from `days` days ago until today, in order. Days without a row get
 * `empty(day)`, so a chart shows days without sales as zero instead of skipping them. Rows carry their business date
 * as `yyyy-mm-dd` and are matched to the calendar days by it.
 */
export function everyDay<T>(rows: T[], days: number, dayOf: (row: T) => string | null,
                            empty: (day: Date) => T): { day: Date, row: T }[] {
    const byDay = new Map<string, T>()
    for (const row of rows) {
        const day = dayOf(row)
        if (day !== null) byDay.set(day, row)
    }
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const result: { day: Date, row: T }[] = []
    for (let offset = days; offset >= 0; offset--) {
        const day = new Date(today)
        day.setDate(today.getDate() - offset)
        result.push({day, row: byDay.get(localDateKey(day)) ?? empty(day)})
    }
    return result
}
