import {dummyCategories} from "./sampling.ts";

export interface Category {
    id: number,
    name: string
}

export function category(id: number): Category {
    return {id: id, name: dummyCategories.get(id)!}
}

export function updateCategory(category: Category) {
    console.log("Updated category: " + category)
}

export function categories(): Category[] {
    let res: Category[] = []
    for (let entry of dummyCategories.entries()) {
        res.push({id: entry[0], name: entry[1]})
    }
    console.log("Generated categories: " + res)
    return res
}
