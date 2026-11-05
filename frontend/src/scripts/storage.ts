import {Page} from "./page.ts";
import {CategoryGroup} from "./categories.ts";
import {Ingredient} from "./Ingredient.ts";
import {getJson, postJson, putJson} from "./http.ts";

export interface InventoryCorrection {
    ingredient: number
    amount: number
}

export interface InventoryEntry {
    amount: number
    product: StorageSummary
}

export interface InventoryGroup extends CategoryGroup<InventoryEntry> {
}

export interface StorageEntry {
    id: number | null,
    purchased: number,
    price: number,
    cost: number,
    amount: number,
    sold: number
}

export interface InboundStorage {
    ingredientId: number,
    purchased: Date,
    price: number | null,
    total: number | null,
    pledge: number,
    amount: number
}

export interface StorageSummary {
    ingredient: Ingredient
    stock: number
}


export function stockInfo(id: number): Promise<StorageSummary> {
    return getJson("api/storage/stock/" + id)
}

export function stockSummary(page: number = 0, limit: number = 100): Promise<Page<StorageSummary>> {
    return getJson("api/storage/stock", new Map([["page", page], ["limit", limit]]))
}

export function addStorage(data: InboundStorage): Promise<StorageEntry> {
    return postJson("api/storage", data)
}

export function submitInventory(data: InventoryCorrection[]) {
    return putJson("api/storage", data)
}

export function stockLow(page: number = 0, limit: number = 100): Promise<Page<StorageSummary>> {
    return getJson("api/storage/stock/low", new Map([["page", page], ["limit", limit]]))
}

export function stockOut(page: number = 0, limit: number = 100): Promise<Page<StorageSummary>> {
    return getJson("api/storage/stock/out", new Map([["page", page], ["limit", limit]]))
}

export interface StockProjection {
    ingredient: Ingredient
    currentStock: number
    dailyConsumption: number
    daysRemaining: number | null
}

export function stockProjection(): Promise<StockProjection[]> {
    return getJson("api/storage/stock/projection")
}

export interface ShoppingListEntry {
    ingredient: Ingredient
    stock: number
    dailyUsage: number
    amount: number
    containers: number
    estimatedCost: number
}

export function shoppingList(days: number): Promise<ShoppingListEntry[]> {
    return getJson("api/storage/shopping-list", new Map([["days", days]]))
}
