import {LazyProduct} from './product.ts'
import {getJson} from "./http.ts";

export interface Purchase {
    id: number
    user_id: number
    productId: number
    price: number
    date: number
}

export interface History {
    purchases: Purchase[]
    products: Map<number, LazyProduct>
}

export function history(user_id: number, limit: number = 30): Promise<History> {
    return getJson(`api/account/${user_id}/purchase`, new Map([["limit", limit]]))
}

// @ts-expect-error
export function transactionDelete(id: number) {

}
