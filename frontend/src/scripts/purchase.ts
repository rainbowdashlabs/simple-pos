import {LazyProduct, Product} from './product.ts'
import {deleteJson, getJson} from "./http.ts";

export interface Purchase {
    id: number
    productId: number
    purchased: number
    amount: number
    price: number
}

export interface History {
    purchases: Purchase[]
    products: Map<number, LazyProduct>
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

export function purchaseDelete(id: number): Promise<any> {
    return deleteJson(`api/purchase/${id}`)
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

export interface DailyStat {
    day: string
    sales: number
    revenue: number
    profit: number
}

export interface CategoryStat {
    categoryName: string
    sales: number
    revenue: number
}

export function dailyStats(after: Date): Promise<DailyStat[]> {
    return getJson("api/purchase/stats/daily", new Map([["after", after.toISOString()]]))
}

export function categoryStats(after: Date): Promise<CategoryStat[]> {
    return getJson("api/purchase/stats/categories", new Map([["after", after.toISOString()]]))
}

