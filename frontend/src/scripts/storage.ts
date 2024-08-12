import {CategoryGroup, Listing} from "./categories.ts";
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
    amount: number,
    sold: number
}

export interface InboundStorage {
    ingredient: Ingredient,
    purchased: Date,
    price: number,
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

export function stockSummary(): Promise<Listing<StorageSummary>> {
    return getJson("api/storage/stock")
}

export function storageHistory(id: number, limit: number = 30): Promise<StorageEntry[]> {
    return getJson("api/storage/" + id, new Map([["limit", limit]]))
}

export function addStorage(data: InboundStorage): Promise<StorageEntry> {
    return postJson("api/storage", data)
}

export function submitInventory(data: InventoryCorrection[]) {
    return putJson("api/storage", data)
}

export function stockLow(limit: number = 100): Promise<StorageSummary[]> {
    return getJson("api/storage/stock/low", new Map([["limit", limit]]))
}
