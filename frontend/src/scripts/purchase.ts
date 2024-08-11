import {LazyProduct} from './product.ts'
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

export function history(user_id: number, limit: number = 30): Promise<History> {
    return getJson(`api/account/${user_id}/purchase`, new Map([["limit", limit]]))
}

export function transactionDelete(id: number): Promise<any> {
    return deleteJson(`api/purchase/${id}`)
}
