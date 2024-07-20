import {Timeframe} from "./product.ts";

export class Timeframes {
    static year: Timeframe = {locale: "year", days: 365}
    static quarter: Timeframe = {locale: "quarter", days: 90}
    static month: Timeframe = {locale: "month", days: 30}
    static week: Timeframe = {locale: "week", days: 7}
}


