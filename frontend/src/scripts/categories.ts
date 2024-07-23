import {dummyCategories} from "./sampling.ts";

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

export function category(id: number): Category {
    return dummyCategories.get(id)!
}

export function updateCategory(category: Category) {
    console.log("Updated category: " + category)
}

export function categories(): Category[] {
    return Array.from(dummyCategories.values())
}
