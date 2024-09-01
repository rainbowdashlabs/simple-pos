import {Category, CategoryGroup, Listing} from "./categories.ts";
import {Ingredient} from "./Ingredient.ts";
import {deleteJson, getJson, postJson, putJson} from "./http.ts";
import {store} from "./store.ts";

export class Timeframe {
    locale: string
    days: number


    constructor(locale: string, days: number) {
        this.locale = locale;
        this.days = days;
    }

    getDate(): Date {
        let date = new Date()
        date.setDate(date.getDate() - this.days)
        return date
    }
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
    day: number | null,
    product_id: number | undefined | null,
    sales: number,
    revenue: number,
    profit: number
}

export function product(id: number): Promise<Product> {
    return getJson("api/product/" + id)
    return store.productCache.promiseById(id) ?? getJson("api/product/" + id)
}

/**
 * Creates a new product and its ingredients if they were not created already.
 *
 * @param product
 */
export function createProduct(product: Product): Promise<Product> {
    return postJson("api/product", product)
    return store.productCache.cachePromise(postJson("api/product", product))
}

export function updateProduct(product: Product): Promise<Product> {
    return putJson("api/product", product)
    return store.productCache.cachePromise(putJson("api/product", product))
}

export function products(): Promise<Listing<Product>> {
    return getJson("api/product")
    return store.productCache.allAsListingPromise() ?? store.productCache.cacheListingPromise(getJson("api/product"))
}

export function deleteProduct(id: number) {
    return deleteJson("api/product/" + id)
        .then(() => store.productCache.delete(id))
}

/**
 * The sales for this product since date per day
 * @param id product id
 * @param date date
 * @param [limit=30] amount of entries to retrieve
 */
export function salesStat(id: number, limit: number = 30): Promise<ProductSalesStat[]> {
    return getJson(`api/product/${id}/sales/stat`, new Map([["limit", limit]]))
}

export function salesStatSummary(id: number, date: Date): Promise<ProductSalesStat> {
    return getJson(`api/product/${id}/sales/stat/summary`, new Map([["after", date.toISOString()]]))
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
