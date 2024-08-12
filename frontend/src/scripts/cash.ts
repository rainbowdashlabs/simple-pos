import {getJson, postJson} from "./http.ts";
import {User} from "./user.ts";

export interface CashHistoryEntry extends CashDto {
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

export async function cashHistory(limit: number = 100): Promise<CashHistoryEntry[]> {
    return await getJson("api/cash", new Map([["limit", limit]]))
}

export async function submitCash(amount: number, note: string, type: string) {
    await postJson("api/cash", {date: Date.now(), amount: amount, note: note, type: type} as CashDto)
}
