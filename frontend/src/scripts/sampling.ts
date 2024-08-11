import {Product} from "./product.ts";
import {Category} from "./categories.ts";
import {Ingredient} from "./Ingredient.ts";

export function getRandomItem(array: string[]): string {
    return array[Math.floor(Math.random() * array.length)];
}

export interface SampleCategory extends Category {
    id: number
    name: string
    products: Array<string>
}

export function randInt(upper: number) {
    return Math.floor(Math.random() * upper)
}

// @formatter:off

export const dummyCategories: Map<number, SampleCategory> = new Map()
dummyCategories.set(0, {id:0, name: 'Coffee', products: ["Latte", "Espresso", "Cappuccino", "Americano", "Mocha", "Irish Coffee"]})
dummyCategories.set(1, {id:1, name: "Tea", products: ["Green Tea", "Black Tea", "Herbal Tea", "Masala Chai", "Earl Grey"]})
dummyCategories.set(2, {id:2, name: "Soft Drinks", products: ["Coca-Cola", "Pepsi", "Sprite", "Fanta"]})
dummyCategories.set(3, {id:3, name: "Juices", products: ["Orange Juice", "Apple Juice", "Mango Juice", "Cranberry Juice"]})
dummyCategories.set(4, {id:4, name: "Water", products: ["Still Water", "Sparkling Water", "Mineral Water"]})
dummyCategories.set(5, {id:5, name: "Beers", products: ["Lager", "Ale", "Stout", "Pilsner", "Porter"]})
dummyCategories.set(6, {id:6, name: "Wines", products: ["Red Wine", "White Wine", "Rose Wine", "Champagne", "Sparkling Wine"]})
dummyCategories.set(7, {id:7, name: "Spirits", products: ["Vodka", "Whiskey", "Rum", "Gin", "Tequila"]})
dummyCategories.set(8, {id:8, name: "Cocktails", products: ["Margarita", "Mojito", "Martini", "Bloody Mary", "Cosmopolitan"]})
dummyCategories.set(9, {id:9, name: "Sweet Snacks", products: ["Chocolate Bars", "Ice Cream", "Candy", "Cookies", "Donuts"]})
dummyCategories.set(10, {id:10, name: "Salty Snacks", products: ["Potato Chips", "Pretzels", "Crackers", "Popcorn"]})
dummyCategories.set(11, {id:11, name: "Healthy Snacks", products: ["Nuts", "Granola Bars", "Fruit", "Yogurt", "Rice Cakes"]})
// @formatter:on

export const dummyProducts: Map<number, Product> = new Map()
export const dummyProductCategories: Map<number, Product[]> = new Map()
export const dummyIngredients: Map<number, Ingredient> = new Map()
export const dummyIngredientCategories: Map<number, Ingredient[]> = new Map()

let index = 0

for (let category of dummyCategories.values()) {
    let categoryIngredients: Ingredient[] = []
    let categoryProducts: Product[] = []
    for (let product of category.products) {
        index += 1
        let ingredient: Ingredient = {
            id: index,
            category: category,
            name: product,
            pledge: randChoice([0, 0.08, 0.15, 0.25]),
            pledgeContainer: randChoice([0, 0.75, 1.5]),
            containerSize: randChoice([0, 6, 12, 20, 24]),
            price: randInt(5),
            minStock: randChoice([10, 20, 30])
        }
        let prod: Product = {
            id: index,
            name: product,
            price: randDecimal(10) + 1,
            raw_price: ingredient.price,
            category: category,
            active: randChoice([true, true, true, false]),
            recipe: {entries: [{amount: 1, ingredient: ingredient}]}
        }
        categoryIngredients.push(ingredient)
        categoryProducts.push(prod)
        dummyProducts.set(index, prod)
        dummyIngredients.set(index, ingredient)
    }
    dummyProductCategories.set(category.id, categoryProducts)
    dummyIngredientCategories.set(category.id, categoryIngredients)
}

console.log(dummyProducts)
console.log(dummyProductCategories)
console.log(dummyIngredients)
console.log(dummyIngredientCategories)

export function randDecimal(upper: number) {
    return randInt(upper * 100) / 100
}

export function randChoice<T>(choices: Array<T>): T {
    return choices[randInt(choices.length)]
}

export function randDate(delta: number = 0) {
    let date = new Date()
    return Math.floor(date.setDate(date.getDate() - delta) / 1000)
}
