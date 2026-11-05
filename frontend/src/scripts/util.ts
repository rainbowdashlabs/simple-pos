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
 * The rows of a daily statistic for every day from `days` days ago until today, in order. Days without a row get
 * `empty(day)`, so a chart shows days without sales as zero instead of skipping them. Rows are matched by their
 * local calendar day.
 */
export function everyDay<T>(rows: T[], days: number, dayOf: (row: T) => string | number | Date | null,
                            empty: (day: Date) => T): { day: Date, row: T }[] {
    const byDay = new Map<string, T>()
    for (const row of rows) {
        const day = dayOf(row)
        if (day !== null) byDay.set(new Date(day).toDateString(), row)
    }
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const result: { day: Date, row: T }[] = []
    for (let offset = days; offset >= 0; offset--) {
        const day = new Date(today)
        day.setDate(today.getDate() - offset)
        result.push({day, row: byDay.get(day.toDateString()) ?? empty(day)})
    }
    return result
}
