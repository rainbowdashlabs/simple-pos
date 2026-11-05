import {Page} from "./page.ts";
import {CartPosition} from "./cart.ts";
import {deleteJson, getJson, patchJson, postJson} from "./http.ts";

/**
 * Describes a user account
 */
export interface Account {
    id: number,
    name: string,
    created: Date | string | number,
    balance: number,
    lastPurchase?: string | null
}

/**
 * Describes a deposit into an account for a user
 */
export interface Balance {
    id: number | null | undefined,
    account: Account | null | undefined,
    amount: number;
    added: number | Date | string;
}

export interface Deposit {
    amount: number
}

/**
 * Retrieve all accounts
 */
export function accounts(query: string = "", page: number = 0, limit: number = 50): Promise<Page<Account>> {
    return getJson("api/account", new Map<string, string | number>([["q", query], ["page", page], ["limit", limit]]))
}

/**
 * The open accounts that bought most recently, latest first.
 */
export function recentAccounts(limit: number = 5): Promise<Account[]> {
    return getJson("api/account/recent", new Map([["limit", limit]]))
}

/**
 * Retrieve a full account by id
 * @param id
 */
export function account(id: number): Promise<Account> {
    return getJson("api/account/" + id)
}

/**
 * Add a deposit to an account
 * @param account account id
 * @param deposit amount to deposit
 */
export function addDeposit(account: Account, deposit: number): Promise<Balance> {
    return postJson(`api/account/${account.id}/balance`, {amount: deposit} as Deposit)
}

/**
 * Get the past deposits for an account. Starting with the most recent one.
 * @param id account id
 * @param limit limit
 */
export function deposits(id: number, page: number = 0, limit: number = 30): Promise<Page<Balance>> {
    return getJson(`api/account/${id}/balance`, new Map([["page", page], ["limit", limit]]))
}

export interface PurchaseReceipt {
    purchaseIds: number[]
}

/**
 * The id standing for "no account" in an account choice: a guest who pays cash. Stored accounts never have it.
 */
export const GUEST_ACCOUNT_ID = 0

/**
 * Sells the cart to a guest who pays cash; the money goes into the till.
 */
export function purchaseAsGuest(entries: CartPosition[]): Promise<PurchaseReceipt> {
    return postJson("api/purchase/guest", positionRequests(entries))
}

export function purchase(id: number, entries: CartPosition[]): Promise<PurchaseReceipt> {
    return postJson(`api/account/${id}/purchase`, positionRequests(entries))
}

function positionRequests(entries: CartPosition[]) {
    return entries.map(entry => ({productId: entry.product.id, amount: entry.amount}))
}

export function renameAccount(id: number, name: string): Promise<Account> {
    return patchJson(`api/account/${id}`, {name: name})
}

export function deleteAccount(id: number): Promise<unknown> {
    return deleteJson(`api/account/${id}`)
}

/**
 * Creates a new account with a name
 * @param name name of the new account
 */
export function createAccount(name: string): Promise<Account> {
    return postJson("api/account", {name: name})
}
