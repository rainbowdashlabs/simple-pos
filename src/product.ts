export interface Product {
    id: number,
    name: string,
    price: number
}

export function product(id: number): Product {
    return {
        id: id,
        name: "Paulaner Spezi Zero 0,5",
        price: 1.2
    }
}
