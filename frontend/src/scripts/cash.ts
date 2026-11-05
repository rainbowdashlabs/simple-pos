import {Page} from "./page.ts";
import {getJson, postJson} from "./http.ts";
import {User} from "./user.ts";

export interface CashHistoryEntry extends CashDto {
    id: number,
    user: User,
    created: number
}

export interface CashDto {
    amount: number,
    type: string,
    note: string
}

export interface CashTotal{
    amount: number
}

export async function currentCash(): Promise<CashTotal> {
    return await getJson("/api/cash/total")
}
export async function currentPledge(): Promise<CashTotal> {
    return await getJson("/api/cash/total/pledge")
}
export async function currentBalance(): Promise<CashTotal> {
    return await getJson("/api/cash/total/balance")
}

export async function cashHistory(page: number = 0, limit: number = 50): Promise<Page<CashHistoryEntry>> {
    return await getJson("api/cash", new Map([["page", page], ["limit", limit]]))
}

export async function submitCash(amount: number, note: string, type: string) {
    await postJson("api/cash", {amount: amount, note: note, type: type} as CashDto)
}

export interface TillCount {
    id: number,
    counted: number,
    expected: number,
    difference: number,
    denominations: Record<number, number>,
    countedBy: string,
    created: string | number
}

export async function countTill(denominations: Record<number, number>): Promise<TillCount> {
    return await postJson("api/cash/count", {denominations: denominations})
}

export async function tillCounts(page: number = 0, limit: number = 5): Promise<Page<TillCount>> {
    return await getJson("api/cash/count", new Map([["page", page], ["limit", limit]]))
}

export interface EmptiesReturn {
    ingredientId: number,
    bottles: number,
    crates: number
}

export async function returnEmpties(returns: EmptiesReturn[]): Promise<CashTotal> {
    return await postJson("api/cash/pledge/return", returns)
}
