export interface Product {
    id: number | null,
    name: string,
    category_id: number,
    price: number,
    in_price: number,
    container_size: number,
    pledge: number,
    pledge_container: number,
    min_stock: number,
    active: boolean
}

export interface Category {
    id: number,
    name: string
}

export interface ProductGroup {
    category: Category,
    products: Product[]
}

export interface ProductListing {
    categories: ProductGroup[]
}

const dummyCategories: Map<number, string> = new Map()
dummyCategories.set(0, 'Snacks')
dummyCategories.set(1, 'Beer')
dummyCategories.set(2, 'Alcohol Free Beer')
dummyCategories.set(3, 'Soft-drinks')
dummyCategories.set(4, 'Juice')

export function product(id: number): Product {
    return {
        id: id,
        category_id: (id % 5),
        name: "Paulaner Spezi Zero 0.5",
        price: 1.2,
        in_price: 1.0,
        container_size: 20,
        pledge: 0.08,
        pledge_container: 1.5,
        min_stock: 10,
        active: true
    }
}

export function products(): ProductListing {
    let res: ProductGroup[] = []
    for (let category of categories()) {
        let group: Product[] = []
        for (let i = 0; i < 5; i++) {
            let cur: Product = product(i)
            cur.active = Math.random() > 0.3
            cur.category_id = category.id
            group.push(cur)
        }
        res.push({category: category, products: group})
    }
    return res
}

export function categories(): Category[] {
    let res: Category[] = []
    for (let entry of dummyCategories.entries()) {
        res.push({id: entry[0], name: entry[1]})
    }
    return res
}
