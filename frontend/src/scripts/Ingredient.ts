import {Category, CategoryGroup, Listing} from "./categories.ts";
import {dummyCategories, dummyIngredientCategories, dummyIngredients} from "./sampling.ts";

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

export function createIngredient(ingredient: Ingredient) {
    ingredient.id = Math.floor(Math.random() * 1000)
    return ingredient
}

export function ingredients(): IngredientListing {
    let res: IngredientGroup[] = []
    for (let entry of dummyIngredientCategories.entries()) {
        res.push({category: dummyCategories.get(entry[0])!, entries: entry[1]})
    }
    return {categories: res}
}

export function updateIngredient(data: Ingredient) {
    console.log(`updated ingredient ${data.id}:${data.name}`)
}

export function ingredient(id: number) {
    return dummyIngredients.get(id)!
}
