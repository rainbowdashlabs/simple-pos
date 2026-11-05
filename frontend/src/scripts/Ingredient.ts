import {Category, Listing} from "./categories.ts";
import {deleteJson, getJson, patchJson, postJson} from "./http.ts";

export interface Ingredient {
    id: number | null | undefined,
    category: Category,
    name: string,
    price: number,
    containerSize: number,
    pledge: number,
    pledgeContainer: number,
    minStock: number,
    active: boolean
}


export interface IngredientRequest {
    name: string,
    categoryId: number,
    price: number,
    containerSize: number,
    pledge: number,
    pledgeContainer: number,
    minStock: number
}

/**
 * The fields of an ingredient the backend accepts in a request.
 */
export function ingredientRequest(ingredient: Ingredient): IngredientRequest {
    return {
        name: ingredient.name,
        categoryId: ingredient.category.id,
        price: ingredient.price,
        containerSize: ingredient.containerSize,
        pledge: ingredient.pledge,
        pledgeContainer: ingredient.pledgeContainer,
        minStock: ingredient.minStock
    }
}

export function createIngredient(ingredient: Ingredient): Promise<Ingredient> {
    return postJson("api/ingredient", ingredientRequest(ingredient))
}

export function ingredients(): Promise<Listing<Ingredient>> {
    return getJson("api/ingredient")
}

export function updateIngredient(data: Ingredient): Promise<Ingredient> {
    return patchJson("api/ingredient/" + data.id, ingredientRequest(data))
}

export function ingredient(id: number): Promise<Ingredient> {
    return getJson("api/ingredient/" + id)
}

export function deleteIngredient(id: number): Promise<unknown> {
    return deleteJson("api/ingredient/" + id)
}

export function toggleIngredientActive(id: number): Promise<Ingredient> {
    return patchJson("api/ingredient/" + id + "/active", {})
}
