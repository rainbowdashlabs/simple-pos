import {categories, CategoryGroup, Listing} from "./categories.ts";
import {Ingredient} from "./Ingredient.ts";
import {dummyIngredientCategories, dummyIngredients, randInt} from "./sampling.ts";

export interface InventoryEntry {
    amount: number
    product: StorageSummary
}

export interface InventoryCorrection {
    product_id: number
    amount: number
}

export interface InventoryGroup extends CategoryGroup<InventoryEntry> {
}

export interface StorageEntry {
    id: number | null,
    product_id: number,
    purchased: number,
    price: number,
    amount: number,
    sold: number
}

export interface InboundStorage {
    product_id: number,
    price: number,
    pledge: number,
    amount: number
}

export interface StorageListing extends Listing<StorageEntry> {
}

export interface StorageGroup extends CategoryGroup<StorageSummary> {
}

export interface StorageSummary {
    ingredient: Ingredient
    stock: number
}

export interface IngredientsStock extends Ingredient {
    stock: number
}

export function storageInfo(id: number): IngredientsStock {
    return {...dummyIngredients.get(id)!, stock: randInt(50)}
}

export function stockSummary(): Listing<IngredientsStock> {
    let res: CategoryGroup<IngredientsStock>[] = []
    for (let category of categories()) {
        let group: IngredientsStock[] = []
        for (let ingredient of dummyIngredientCategories.get(category.id)!) {
            group.push(storageInfo(ingredient.id!))
        }
        res.push({category: category, entries: group})
    }
    return {categories: res}
}

export function storageHistory(id: number, limit: number = 30): StorageEntry[] {
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

export function addStorage(data: InboundStorage) {
    console.log("Added storage data: " + data)
}

export function submitInventory(data: InventoryCorrection[]) {
    console.log("Submit inventory: ")
    console.log(data)
}
