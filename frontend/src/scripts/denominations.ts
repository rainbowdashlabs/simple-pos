/**
 * The coins and notes of a currency, each as its value in cents. The till count offers one field per entry.
 */
export interface DenominationSet {
    coins: number[],
    notes: number[]
}

export const EURO: DenominationSet = {
    coins: [1, 2, 5, 10, 20, 50, 100, 200],
    notes: [500, 1000, 2000, 5000, 10000, 20000, 50000]
}

/**
 * The total in cents of the pieces counted per denomination.
 */
export function totalCents(pieces: Record<number, number>): number {
    return Object.entries(pieces).reduce((sum, [cents, count]) => sum + Number(cents) * count, 0)
}

/**
 * The text colour for a till difference: green when the till matches, red when money is missing, amber when there is
 * more than expected.
 */
export function differenceClass(difference: number): string {
    if (difference === 0) return "text-green-600"
    return difference < 0 ? "text-red-500" : "text-amber-500"
}
