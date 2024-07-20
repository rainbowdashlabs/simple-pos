import {Timeframe} from "./product.ts";

export class Timeframes {
    static readonly year: Timeframe = {locale: "year", days: 365}
    static readonly quarter: Timeframe = {locale: "quarter", days: 90}
    static readonly month: Timeframe = {locale: "month", days: 30}
    static readonly week: Timeframe = {locale: "week", days: 7}
}


