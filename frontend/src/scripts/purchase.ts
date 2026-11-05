import {LazyProduct, Product} from './product.ts'
import {deleteJson, getJson, postJson} from "./http.ts";
import {store} from "./store.ts";

export interface Purchase {
    id: number
    productId: number
    purchased: number
    amount: number
    price: number
    soldBy?: string | null
}

export interface History {
    purchases: Purchase[]
    products: Record<number, LazyProduct>
}

export interface SalesStat {
    product: Product
    sales: number
    revenue: number
    profit: number
}

export function history(user_id: number, limit: number = 30): Promise<History> {
    return getJson(`api/account/${user_id}/purchase`, new Map([["limit", limit]]))
}

/**
 * Cancels one purchase. A sale that can still be undone no longer includes it afterwards.
 */
export function purchaseDelete(id: number): Promise<unknown> {
    return deleteJson(`api/purchase/${id}`).then(result => {
        forgetFromLastSale(id)
        return result
    })
}

function forgetFromLastSale(id: number) {
    const sale = store.lastSale
    if (!sale) return
    const remaining = sale.purchaseIds.filter(purchaseId => purchaseId !== id)
    store.lastSale = remaining.length ? {...sale, purchaseIds: remaining} : null
}

/**
 * Cancels several purchases together, such as all positions of one sale.
 */
export function cancelPurchases(ids: number[]): Promise<unknown> {
    return postJson("api/purchase/cancel", {ids: ids})
}

export function statsTop(type: string, after: Date, limit: number = 30): Promise<SalesStat[]> {
    let args = new Map([
        ["sorting", type],
        ["limit", String(limit)],
        ["after", after.toISOString()]
    ]);
    return getJson("api/purchase/stats/top", args)
}

export function statsBottom(after: Date, limit: number = 10): Promise<SalesStat[]> {
    let args = new Map([
        ["limit", String(limit)],
        ["after", after.toISOString()]
    ]);
    return getJson("api/purchase/stats/bottom", args)
}

/**
 * Sales, revenue and profit of a day, or of a whole period when `day` is null.
 */
export interface PeriodStat {
    day: string | null
    sales: number
    revenue: number
    profit: number
}

/**
 * Sales of one business day, as "yyyy-mm-dd".
 */
export interface DailyStat extends PeriodStat {
    day: string
}

export interface CategoryStat {
    categoryId: number
    categoryName: string
    sales: number
    revenue: number
}

/**
 * Sales, revenue and profit of all products since `after`, in one figure each.
 */
export function salesSummary(after: Date): Promise<PeriodStat> {
    return getJson("api/purchase/stats/summary", new Map([["after", after.toISOString()]]))
}

export function dailyStats(after: Date): Promise<DailyStat[]> {
    return getJson("api/purchase/stats/daily", new Map([["after", after.toISOString()]]))
}

export function categoryStats(after: Date): Promise<CategoryStat[]> {
    return getJson("api/purchase/stats/categories", new Map([["after", after.toISOString()]]))
}

