import {Category, Listing} from "./categories.ts";
import {deleteJson, getJson, patchJson, postJson} from "./http.ts";
import {store} from "./store.ts";

export interface Ingredient {
    id: number | null | undefined,
    category: Category,
    name: string,
    price: number,
    containerSize: number,
    pledge: number,
    pledgeContainer: number,
    minStock: number
}


export function createIngredient(ingredient: Ingredient): Promise<Ingredient> {
    return postJson("api/ingredient", ingredient)
    return store.ingredientCache.cachePromise(postJson("api/ingredient", ingredient))
}

export function ingredients(): Promise<Listing<Ingredient>> {
    return getJson("api/ingredient")
    return store.ingredientCache.allAsListingPromise() ?? store.ingredientCache.cacheListingPromise(getJson("api/ingredient"))
}

export function updateIngredient(data: Ingredient): Promise<Ingredient> {
    return patchJson("api/ingredient", data)
    return store.ingredientCache.cachePromise(patchJson("api/ingredient", data))
}

export function ingredient(id: number): Promise<Ingredient> {
    return getJson("api/ingredient/" + id)
    return store.ingredientCache.promiseById(id) ?? store.ingredientCache.cachePromise(getJson("api/ingredient/" + id))
}

export function deleteIngredient(id: number): Promise<any> {
    return deleteJson("api/ingredient/" + id)
    store.ingredientCache.delete(id)
    return deleteJson("api/ingredient/" + id)
}
