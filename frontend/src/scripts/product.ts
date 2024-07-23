import {categories, Category} from "./categories.ts";

export interface Timeframe {
    locale: string,
    days: number
}


export interface Product {
    id: number | null,
    name: string,
    category_id: number,
    price: number,
    purchase_price: number,
    container_size: number,
    pledge: number,
    pledge_container: number,
    min_stock: number,
    active: boolean
}

export interface ProductGroup {
    category: Category,
    products: Product[]
}

export interface ProductListings {
    categories: ProductGroup[]
}

export interface ProductSalesStat {
    day: number,
    product_id: number,
    sales: number,
    revenue: number,
    profit: number
}

export function product(id: number): Product {
    return {
        id: id,
        category_id: (id % 5),
        name: "Paulaner Spezi Zero 0.5 with extra steps",
        price: 1.2,
        purchase_price: 1.0,
        container_size: 20,
        pledge: 0.08,
        pledge_container: 1.5,
        min_stock: 10,
        active: true
    }
}

export function createProduct(product: Product) {
    console.log("Created product " + product)
}

export function updateProduct(product: Product) {
    console.log("Updated product " + product)
}

export function products(): ProductListings {
    let res: ProductGroup[] = []
    for (let category of categories()) {
        let group: Product[] = []
        for (let i = 0; i < 5; i++) {
            let cur: Product = product(i)
            cur.active = Math.random() > 0.3
            cur.category_id = category.id
            group.push(cur)
        }
        res.push({category: category, products: group})
    }
    return {categories: res}
}

/**
 * The sales for this product since date per day
 * @param id product id
 * @param date date
 * @param [limit=30] amount of entries to retrieve
 */
// @ts-expect-error
export function salesProduct(id: number, date: Date, limit: number = 30): ProductSalesStat[] {
    let res: ProductSalesStat[] = []
    let currDate = new Date()
    for (let i = 0; i < limit; i++) {
        var timestamp = currDate.setDate(currDate.getDate() - 1)
        res.push({
            day: timestamp,
            product_id: id,
            sales: Math.floor(Math.random() * 10),
            profit: 0,
            revenue: 0
        })
    }
    return res
}

export function salesCountProduct(id: number, date: Date): ProductSalesStat {
    return {
        day: date.getMilliseconds(),
        product_id: id,
        sales: 100,
        profit: 10,
        revenue: 50
    }
}

// statistic functions
/**
 * A ranking for the best-selling products
 */
// @ts-expect-error
export function bestSellingProducts(date: Date, limit: number = 30) {

}

/**
 * A ranking for the worst-selling products
 */
// @ts-expect-error
export function worstSellingProducts(date: Date, limit: number = 30) {

}

// Monitoring
// @ts-expect-error
export function lowStockProducts(limit: number = 30) {

}
