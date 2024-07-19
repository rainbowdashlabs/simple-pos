import {product, Product} from './product.ts'

export interface Transaction {
    id: number
    user_id: number
    product_id: number
    price: number
    date: number
}

export interface History {
    transactions: Transaction[]
    products: Map<number, Product>
}

export function history(user_id: number, limit: number = 30): History {
    console.log(`Retrieve ${limit} transactions for user ${user_id}`)
    let result: Transaction[] = []
    for (let i = 0; i < limit; i++) {
        result.push({
            id: i,
            user_id: user_id,
            product_id: 1,
            price: 1.2,
            date: Math.floor(Date.now() / 1000)
        })
    }
    let products: Map<number, Product> = new Map()
    products.set(1, product(1))
    console.log(`${result.length} transactions`)
    return {transactions: result, products: products}
}

// @ts-expect-error
export function transactionDelete(id: number) {

}
