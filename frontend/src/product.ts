export interface Timeframe {
    locale: string,
    days: number
}

export class Timeframes {
    static year: Timeframe = {locale: "year", days: 365}
    static quarter: Timeframe = {locale: "quarter", days: 90}
    static month: Timeframe = {locale: "month", days: 30}
    static week: Timeframe = {locale: "week", days: 7}
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

export interface Category {
    id: number,
    name: string
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

export interface StorageEntry {
    id: number | null,
    product_id: number,
    purchased: number,
    price: number,
    amount: number,
    sold: number
}

export interface StorageListings {
    categories: StorageGroup[]
}

export interface StorageGroup {
    category: Category,
    products: StorageSummary[]
}

export interface StorageSummary {
    product: Product
    stock: number
}

const dummyCategories: Map<number, string> = new Map()
dummyCategories.set(0, 'Snacks')
dummyCategories.set(1, 'Beer')
dummyCategories.set(2, 'Alcohol Free Beer')
dummyCategories.set(3, 'Soft-drinks')
dummyCategories.set(4, 'Juice')

export function product(id: number): Product {
    return {
        id: id,
        category_id: (id % 5),
        name: "Paulaner Spezi Zero 0.5",
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

export function category(id: number): Category {
    return {id: id, name: dummyCategories.get(id)!}
}

export function updateCategory(category: Category) {
    console.log("Updated category: " + category)
}

export function categories(): Category[] {
    let res: Category[] = []
    for (let entry of dummyCategories.entries()) {
        res.push({id: entry[0], name: entry[1]})
    }
    console.log("Generated categories: " + res)
    return res
}

export function inventorySummary(): StorageListings {
        let res: StorageGroup[] = []
    for (let category of categories()) {
        let group: StorageSummary[] = []
        for (let i = 0; i < 5; i++) {
            let cur: Product = product(i)
            cur.active = Math.random() > 0.3
            cur.category_id = category.id
            group.push({product: cur, stock: Math.floor(Math.random() * 50)})
        }
        res.push({category: category, products: group})
    }
    return {categories: res}

}

export function inboundInventory(id: number, limit: number = 30): StorageEntry[] {
    let res = []
    for (let i = 0; i < limit; i++) {
        res.push({
            id: id,
            product_id: id,
            purchased: Math.floor(Date.now() / 1000),
            price: Math.random() * 10,
            amount: Math.floor(Math.random() * 100),
            sold: 0
        })
    }
    return res
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
