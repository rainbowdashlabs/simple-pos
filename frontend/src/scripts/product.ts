import {Category, CategoryGroup, Listing} from "./categories.ts";
import {Ingredient} from "./Ingredient.ts";
import {getJson, postJson, putJson} from "./http.ts";

export interface Timeframe {
    locale: string,
    days: number
}

export interface RecipeEntry {
    ingredient: Ingredient
    amount: number
}

export interface Recipe {
    entries: RecipeEntry[]
}

export interface LazyProduct {
    id: number | null,
    name: string,
    category: Category,
    price: number,
    raw_price: number | undefined | null,
    active: boolean
}

export interface Product extends LazyProduct {
    recipe: Recipe
}

export interface ProductGroup extends CategoryGroup<Product> {
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

export function product(id: number): Promise<Product> {
    return getJson("api/product/" + id)
}

/**
 * Creates a new product and its ingredients if they were not created already.
 *
 * @param product
 */
export function createProduct(product: Product): Promise<Product> {
    return postJson("api/product/", product)
}

export function updateProduct(product: Product): Promise<Product> {
    return putJson("api/product/", product)
}

export function products(): Promise<Listing<Product>> {
    return getJson("api/product/")
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
