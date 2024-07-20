import {getRandomItem} from "./sampling.ts";

/**
 * Describes a user account
 */
export interface LazyAccount {
    id: number,
    name: string;
    balance: number;
}

/**
 * Describes an account with additional data
 */
export interface FullAccount extends LazyAccount {

}

/**
 * Describes a deposit into an account for a user
 */
export interface Deposit {
    id: number;
    user_id: number,
    amount: number;
    date: number;
}


const names = ["John", "Jane", "Peter", "Paul", "Jennifer", "Mark", "Emma", "Robert", "Linda", "Michael"];
const surnames = ["Smith", "Johnson", "Williams", "Brown", "Taylor", "Miller", "Wilson", "Moore", "Davis", "Clark", "Doe"];

function randomUser(id: number): LazyAccount {
    return {
        id: id,
        name: `${getRandomItem(names)} ${getRandomItem(surnames)}`,
        balance: Math.random() * 60 - 30
    }
}

const dummy_accounts: LazyAccount[] = []
for (let i = 0; i < 60; i++) {
    dummy_accounts.push(randomUser(i))
}

/**
 * Retrieve all accounts
 */
export function accounts(): LazyAccount[] {
    return dummy_accounts
}

/**
 * Retrieve a full account by id
 * @param id
 */
export function account(id: number): FullAccount {
    return randomUser(id)
}

/**
 * Add a deposit to an account
 * @param id account id
 * @param deposit amount to deposit
 */
export function addDeposit(id: number, deposit: number) {
    console.log(`Deposit ${deposit} for ${id}`)
}

/**
 * Get the past deposits for an account. Starting with the most recent one.
 * @param id account id
 * @param limit limit
 */
export function deposits(id: number, limit: number): Deposit[] {
    console.log(`Retrieve ${limit} deposits for ${id}`)
    let result: Deposit[] = []
    for (let i = 0; i < limit; i++) {
        result.push({id: i, user_id: id, amount: Math.round(Math.random() * 50), date: Math.floor(Date.now() / 1000)})
    }
    console.log(result)
    return result
}

/**
 * Deletes am account
 * @param id
 */
export function deleteAccount(id: number) {
    console.log(`deleted account ${id}`)
}

/**
 * Creates a new account with a name
 * @param name name of the new account
 */
export function createAccount(name: string): LazyAccount {
    console.log(`Created account ${name}`)
    return {
        id: 100,
        name: name,
        balance: 0
    }
}
