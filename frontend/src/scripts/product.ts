import {Category, CategoryGroup, Listing} from "./categories.ts";
import {Ingredient, ingredientRequest} from "./Ingredient.ts";
import {deleteJson, getJson, patchJson, postJson, putJson} from "./http.ts";

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

/**
 * A product being filled in, which may not have a category yet.
 */
export type ProductDraft = Omit<Product, 'category'> & { category?: Category }

export interface ProductGroup extends CategoryGroup<Product> {
}

export interface ProductListings {
    categories: ProductGroup[]
}

export interface ProductSalesStat {
    day: string | null,
    product_id: number | undefined | null,
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
    return postJson("api/product", productRequest(product))
}

export function updateProduct(product: Product): Promise<Product> {
    return putJson("api/product/" + product.id, productRequest(product))
}

/**
 * The fields of a product the backend accepts in a request. Recipe entries refer to existing ingredients by id; an
 * ingredient without id is sent whole, to be created with the product.
 */
function productRequest(product: Product) {
    return {
        name: product.name,
        categoryId: product.category.id,
        price: product.price,
        recipe: (product.recipe?.entries ?? []).map(entry => entry.ingredient.id != null
            ? {ingredientId: entry.ingredient.id, amount: entry.amount}
            : {ingredient: ingredientRequest(entry.ingredient), amount: entry.amount})
    }
}

export function products(): Promise<Listing<Product>> {
    return getJson("api/product")
}

export function activeProducts(): Promise<Listing<Product>> {
    return getJson("api/product/active")
}

export function deleteProduct(id: number) {
    return deleteJson("api/product/" + id)
}

export function toggleActive(id: number): Promise<Product> {
    return patchJson("api/product/" + id + "/active", {})
}

/**
 * The sales of this product per day since a date
 * @param id product id
 * @param after only purchases after this date count
 */
export function salesStat(id: number, after: Date): Promise<ProductSalesStat[]> {
    return getJson(`api/product/${id}/sales/stat`, new Map([["after", after.toISOString()]]))
}

export function salesStatSummary(id: number, date: Date): Promise<ProductSalesStat> {
    return getJson(`api/product/${id}/sales/stat/summary`, new Map([["after", date.toISOString()]]))
}

