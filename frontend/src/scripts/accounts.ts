import {CartPosition} from "./cart.ts";
import {deleteJson, getJson, postJson} from "./http.ts";

/**
 * Describes a user account
 */
export interface Account {
    id: number,
    name: string,
    created: Date | string | number,
    balance: number
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
export function accounts(): Promise<Account[]> {
    return getJson("api/account/")
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
export function deposits(id: number, limit: number): Promise<Balance[]> {
    return getJson(`api/account/${id}/balance`, new Map([["limit", limit]]))
}

export function purchase(id: number, entries: CartPosition[]): Promise<any> {
    return postJson(`api/account/${id}/purchase`, entries)
}

/**
 * Deletes am account
 * @param id
 */
export function deleteAccount(id: number): Promise<any> {
    return deleteJson(`api/account/${id}`)
}

/**
 * Creates a new account with a name
 * @param name name of the new account
 */
export function createAccount(name: string): Promise<Account> {
    return postJson("api/account/", {name: name})
}
