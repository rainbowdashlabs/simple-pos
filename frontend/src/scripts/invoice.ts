import {getJson, postJson} from "./http.ts";
import {Ingredient} from "./Ingredient.ts";
import {VatRate} from "./vat.ts";
import {StorageEntry} from "./storage.ts";

export type CountUnit = "CONTAINER" | "PIECE"

export interface ItemDraft {
    kind: "ITEM"
    key: number
    ingredient: Ingredient | null
    text: string
    gtin: string
    quantity: number
    unit: CountUnit
    price: number | null
    /**
     * A rate chosen on this line instead of the ingredient's, or null to use the ingredient's.
     */
    vatRate: VatRate | null
    updateIngredientRate: boolean
}

export interface PledgeDraft {
    kind: "PLEDGE"
    key: number
    text: string
    quantity: number
    /**
     * The pledge per piece as a positive amount; {@link returned} says which way it goes.
     */
    price: number | null
    /**
     * Whether this is pledge given back for returned empties, which is credited, rather than pledge paid.
     */
    returned?: boolean
    /**
     * A rate chosen on this line, or null for the default rate.
     */
    vatRate: VatRate | null
}

export interface DiscountDraft {
    kind: "DISCOUNT"
    key: number
    amount: number | null
    /**
     * The key of the item the discount reduces.
     */
    target: number | null
}

export type PositionDraft = ItemDraft | PledgeDraft | DiscountDraft

export interface InvoiceDraft {
    /**
     * The purchase day as yyyy-mm-dd.
     */
    purchased: string
    supplier: string
    pricesNet: boolean
    printedTotal: number | null
    positions: PositionDraft[]
    nextKey: number
}

/**
 * How a supplier named something on an earlier invoice, with what it stood for then. The last price is as printed,
 * without VAT when {@code lastPriceNet}; it is negative for empties given back.
 */
export interface InvoiceAlias {
    id: number
    text: string | null
    gtin: string | null
    kind: "ITEM" | "PLEDGE"
    ingredient: Ingredient | null
    unit: CountUnit | null
    lastPrice: number | null
    lastPriceNet: boolean
    lastUsed: string
}

const GTIN = /^\d{8,14}$/

/**
 * A printed text as remembered names are compared: trimmed, inner runs of spaces as one, in upper case.
 */
export function normalizeAliasText(text: string): string {
    return text.trim().replace(/\s+/g, " ").toUpperCase()
}

/**
 * Whether a text is a GTIN as invoices print it: 8 to 14 digits.
 */
export function isGtin(text: string): boolean {
    return GTIN.test(text.trim())
}

/**
 * The remembered names whose GTIN is the query or whose text starts with it, best first.
 */
export function searchAliases(query: string): Promise<InvoiceAlias[]> {
    return getJson("api/storage/invoice/aliases", new Map([["q", query]]))
}

export interface InvoiceResult {
    batches: StorageEntry[]
    goods: number
    pledge: number
    total: number
}

/**
 * What an invoice comes to, in cents, computed the way the backend books it.
 */
export interface InvoiceTotals {
    lines: Map<number, number>
    itemCosts: Map<number, number>
    goods: number
    pledge: number
    total: number
}

const DRAFT_KEY = "invoice-draft"

/**
 * Today as yyyy-mm-dd in the browser's time zone.
 */
export function today(): string {
    const now = new Date()
    const pad = (value: number) => String(value).padStart(2, "0")
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

export function emptyInvoice(): InvoiceDraft {
    return {purchased: today(), supplier: "", pricesNet: false, printedTotal: null, positions: [], nextKey: 1}
}

/**
 * The draft kept in this browser, or null when there is none or it cannot be read.
 */
export function loadDraft(): InvoiceDraft | null {
    try {
        const stored = localStorage.getItem(DRAFT_KEY)
        if (!stored) return null
        const draft = JSON.parse(stored) as InvoiceDraft
        if (!Array.isArray(draft.positions)) return null
        const positions = draft.positions
            .filter(position => Number.isInteger(position?.key) && ["ITEM", "PLEDGE", "DISCOUNT"].includes(position.kind))
            .map(position => position.kind === "PLEDGE" && position.price !== null && position.price < 0
                ? {...position, price: -position.price, returned: true} : position)
        const nextKey = positions.reduce((max, position) => Math.max(max, position.key), 0) + 1
        return {...emptyInvoice(), ...draft, positions, nextKey}
    } catch {
        return null
    }
}

export function saveDraft(draft: InvoiceDraft) {
    try {
        localStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
    } catch {
        return
    }
}

export function clearDraft() {
    try {
        localStorage.removeItem(DRAFT_KEY)
    } catch {
        return
    }
}

/**
 * Swaps the ingredients and VAT rates a draft holds for the current ones, as a draft kept in the browser may hold
 * values changed since. An ingredient that is gone or inactive is dropped, so the line asks for one again; a line rate
 * that is no longer active is dropped, so the line falls back to the ingredient's or the default rate.
 */
export function rebind(draft: InvoiceDraft, ingredients: Ingredient[], rates: VatRate[]) {
    for (const position of draft.positions) {
        if (position.kind === "DISCOUNT") continue
        if (position.vatRate) position.vatRate = rates.find(rate => rate.id === position.vatRate!.id) ?? null
        if (position.kind !== "ITEM") continue
        if (position.ingredient) position.ingredient = ingredients.find(ingredient => ingredient.id === position.ingredient!.id) ?? null
        if (!position.vatRate) position.updateIngredientRate = false
    }
}

/**
 * Whether a yyyy-mm-dd day is a real date and not after today.
 */
export function validPurchaseDay(day: string): boolean {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(day) || day > today()) return false
    const [year, month, date] = day.split("-").map(Number)
    const parsed = new Date(year, month - 1, date)
    return parsed.getFullYear() === year && parsed.getMonth() === month - 1 && parsed.getDate() === date
}

/**
 * A price as the invoice is booked with it: at most four decimals, as unit prices are kept.
 */
export function bookedPrice(price: number): number {
    return Math.round(price * 10000) / 10000
}

/**
 * The VAT rate a line is booked with: the one chosen on the line, else the ingredient's for an item, else the
 * default rate.
 */
export function rateOf(position: ItemDraft | PledgeDraft, defaultRate: VatRate | null): VatRate | null {
    if (position.vatRate) return position.vatRate
    if (position.kind === "ITEM" && position.ingredient?.vatRate) return position.ingredient.vatRate
    return defaultRate
}

/**
 * A pledge line's price with its direction: negative for returned empties.
 */
export function signedPledgePrice(pledge: PledgeDraft): number | null {
    if (pledge.price === null) return null
    return pledge.returned ? -pledge.price : pledge.price
}

/**
 * The pledge an item carries, from its ingredient: per container the bottles' pledge plus the crate's, per piece the
 * bottle's; null when the item has none.
 */
export function pledgeOf(item: ItemDraft): { quantity: number, price: number } | null {
    const ingredient = item.ingredient
    if (!ingredient || item.quantity < 1) return null
    const price = item.unit === "CONTAINER"
        ? Math.round((ingredient.containerSize * ingredient.pledge + ingredient.pledgeContainer) * 100) / 100
        : ingredient.pledge
    return price > 0 ? {quantity: item.quantity, price} : null
}

/**
 * The units an item puts into storage.
 */
export function unitsOf(item: ItemDraft): number {
    if (item.unit === "CONTAINER") return item.quantity * (item.ingredient?.containerSize ?? 0)
    return item.quantity
}

function scaled(value: number, decimals: number): bigint {
    return BigInt(Math.round(value * 10 ** decimals))
}

function divideHalfUp(value: bigint, divisor: bigint): bigint {
    const negative = value < 0n
    const magnitude = negative ? -value : value
    const rounded = (magnitude * 2n + divisor) / (divisor * 2n)
    return negative ? -rounded : rounded
}

/**
 * An amount with up to four decimals, plus VAT at {@code rate} percent when given, in cents rounded half up.
 */
function grossCents(amount: number, quantity: number, rate: VatRate | null, net: boolean): number {
    const amount4 = scaled(amount, 4) * BigInt(quantity)
    if (!net || !rate) return Number(divideHalfUp(amount4, 100n))
    return Number(divideHalfUp(amount4 * (10000n + scaled(rate.rate, 2)), 1000000n))
}

/**
 * Every line's gross amount, each item's cost after its discounts, and the totals, all in cents. Lines that are not
 * filled in count as 0.
 */
export function computeTotals(draft: InvoiceDraft, defaultRate: VatRate | null): InvoiceTotals {
    const lines = new Map<number, number>()
    const itemCosts = new Map<number, number>()
    let pledge = 0
    for (const position of draft.positions) {
        if (position.kind === "ITEM") {
            const cents = position.price === null ? 0
                : grossCents(position.price, position.quantity, rateOf(position, defaultRate), draft.pricesNet)
            lines.set(position.key, cents)
            itemCosts.set(position.key, cents)
        } else if (position.kind === "PLEDGE") {
            const price = signedPledgePrice(position)
            const cents = price === null ? 0
                : grossCents(price, position.quantity, rateOf(position, defaultRate), draft.pricesNet)
            lines.set(position.key, cents)
            pledge += cents
        }
    }
    for (const position of draft.positions) {
        if (position.kind !== "DISCOUNT") continue
        const target = draft.positions.find(other => other.key === position.target)
        const rate = target?.kind === "ITEM" ? rateOf(target, defaultRate) : null
        const cents = position.amount === null ? 0 : grossCents(position.amount, 1, rate, draft.pricesNet)
        lines.set(position.key, -cents)
        if (position.target !== null && itemCosts.has(position.target)) {
            itemCosts.set(position.target, itemCosts.get(position.target)! - cents)
        }
    }
    const goods = [...itemCosts.values()].reduce((sum, cents) => sum + cents, 0)
    return {lines, itemCosts, goods, pledge, total: goods + pledge}
}

/**
 * Why the draft cannot be booked yet, as translation keys with the 1-based position they are about (0 for the whole
 * invoice), or nothing. Two items keeping different rates for one ingredient are caught here, as the backend refuses
 * them.
 */
export function problemsOf(draft: InvoiceDraft, totals: InvoiceTotals): { key: string, position: number }[] {
    const problems: { key: string, position: number }[] = []
    if (!validPurchaseDay(draft.purchased)) problems.push({key: "invoice.problem.bad_date", position: 0})
    if (!draft.positions.some(position => position.kind === "ITEM")) problems.push({key: "invoice.problem.no_item", position: 0})
    const keptRates = new Map<number | null | undefined, number | undefined>()
    draft.positions.forEach((position, index) => {
        const number = index + 1
        if (position.kind === "ITEM") {
            if (!position.ingredient) problems.push({key: "invoice.problem.no_ingredient", position: number})
            if (position.quantity < 1) problems.push({key: "invoice.problem.no_quantity", position: number})
            if (position.price === null || position.price < 0) problems.push({key: "invoice.problem.no_price", position: number})
            if (position.unit === "CONTAINER" && position.ingredient && position.ingredient.containerSize <= 0) {
                problems.push({key: "invoice.problem.no_container", position: number})
            }
            if (position.gtin.trim() && !isGtin(position.gtin)) problems.push({key: "invoice.problem.bad_gtin", position: number})
            if ((totals.itemCosts.get(position.key) ?? 0) < 0) problems.push({key: "invoice.problem.discount_too_large", position: number})
            if (draft.pricesNet && position.updateIngredientRate && position.ingredient && position.vatRate) {
                const kept = keptRates.get(position.ingredient.id)
                if (kept !== undefined && kept !== position.vatRate.id) problems.push({key: "invoice.problem.rate_conflict", position: number})
                keptRates.set(position.ingredient.id, position.vatRate.id)
            }
        } else if (position.kind === "PLEDGE") {
            if (position.quantity < 1) problems.push({key: "invoice.problem.no_quantity", position: number})
            if (position.price === null) problems.push({key: "invoice.problem.no_price", position: number})
        } else {
            if (position.amount === null || position.amount < 0) problems.push({key: "invoice.problem.no_amount", position: number})
            const target = draft.positions.find(other => other.key === position.target)
            if (target?.kind !== "ITEM") problems.push({key: "invoice.problem.no_target", position: number})
        }
    })
    return problems
}

/**
 * The draft as the backend books it. A purchase day of today is sent as none, so the invoice counts as bought now.
 */
export function invoiceRequest(draft: InvoiceDraft) {
    if (!validPurchaseDay(draft.purchased)) throw new RangeError("Invalid purchase day " + draft.purchased)
    const itemIndex = (key: number | null) => draft.positions.findIndex(position => position.key === key)
    return {
        purchased: draft.purchased === today() ? null : new Date(`${draft.purchased}T12:00:00`).toISOString(),
        supplier: draft.supplier.trim() || null,
        pricesNet: draft.pricesNet,
        positions: draft.positions.map(position => {
            if (position.kind === "ITEM") {
                return {
                    kind: "ITEM",
                    ingredientId: position.ingredient?.id,
                    text: position.text.trim() || null,
                    gtin: position.gtin.trim() || null,
                    quantity: position.quantity,
                    unit: position.unit,
                    price: position.price === null ? null : bookedPrice(position.price),
                    vatRateId: draft.pricesNet ? position.vatRate?.id ?? null : null,
                    updateIngredientRate: draft.pricesNet && position.updateIngredientRate
                }
            }
            if (position.kind === "PLEDGE") {
                return {
                    kind: "PLEDGE",
                    text: position.text.trim() || null,
                    quantity: position.quantity,
                    price: signedPledgePrice(position) === null ? null : bookedPrice(signedPledgePrice(position)!),
                    vatRateId: draft.pricesNet ? position.vatRate?.id ?? null : null
                }
            }
            return {
                kind: "DISCOUNT",
                amount: position.amount === null ? null : bookedPrice(position.amount),
                discountOf: itemIndex(position.target)
            }
        })
    }
}

/**
 * Books a request made by {@link invoiceRequest}.
 */
export function bookInvoice(request: ReturnType<typeof invoiceRequest>): Promise<InvoiceResult> {
    return postJson("api/storage/invoice", request)
}
