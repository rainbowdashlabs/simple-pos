import {LazyProduct, product} from './product.ts'

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

export function history(user_id: number, limit: number = 30): History {
    console.log(`Retrieve ${limit} transactions for user ${user_id}`)
    let result: Purchase[] = []
    for (let i = 0; i < limit; i++) {
        result.push({
            id: i,
            user_id: user_id,
            productId: 1,
            price: 1.2,
            date: Math.floor(Date.now() / 1000)
        })
    }
    let products: Map<number, LazyProduct> = new Map()
    products.set(1, product(1))
    console.log(`${result.length} transactions`)
    return {purchases: result, products: products}
}

// @ts-expect-error
export function transactionDelete(id: number) {

}
