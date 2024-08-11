import {deleteJson, getJson, patchJson, postJson} from "./http.ts";

export interface Category {
    id: number,
    name: string
}

export interface CategoryGroup<T> {
    category: Category
    entries: T[]
}

export interface Listing<T> {
    categories: CategoryGroup<T>[]
}

export function category(id: number): Promise<Category> {
    return getJson("api/category/" + id)
}

export function updateCategory(category: Category): Promise<Category> {
    return patchJson("api/category", category)
}

export function categories(): Promise<Category[]> {
    return getJson("api/category")
}

export function createCategory(name: string): Promise<Category> {
    return postJson("api/category", {name: name, id: null})
}

export function deleteCategory(id: number): Promise<Category> {
    return deleteJson("api/category/" + id)
}
