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
