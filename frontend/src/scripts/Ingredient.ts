import {Category, CategoryGroup, Listing} from "./categories.ts";
import {getJson, patchJson, postJson} from "./http.ts";

export interface IngredientListing extends Listing<Ingredient> {

}

export interface IngredientGroup extends CategoryGroup<Ingredient> {
}

export interface Ingredient {
    id: number | null | undefined,
    category: Category,
    name: string,
    price: number,
    container_size: number,
    pledge: number,
    pledge_container: number,
    min_stock: number
}

export function createIngredient(ingredient: Ingredient): Promise<Ingredient> {
    return postJson("api/ingredient/", ingredient)
}

export function ingredients(): Promise<IngredientListing> {
    return getJson("api/ingredient")
}

export function updateIngredient(data: Ingredient): Promise<Ingredient> {
    return patchJson("api/ingredient", data)
}

export function ingredient(id: number): Promise<Ingredient> {
    return getJson("api/ingredient/" + id)
}
