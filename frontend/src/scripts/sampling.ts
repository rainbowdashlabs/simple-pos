export function getRandomItem(array: string[]): string {
    return array[Math.floor(Math.random() * array.length)];
}

export const dummyCategories: Map<number, string> = new Map()
dummyCategories.set(0, 'Snacks')
dummyCategories.set(1, 'Beer')
dummyCategories.set(2, 'Alcohol Free Beer')
dummyCategories.set(3, 'Soft-drinks')
dummyCategories.set(4, 'Juice')
