import {getJson, postJson} from "./http.ts";

export interface CashHistoryEntry extends CashDto {
    username: string,
    date: number
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

export async function cashHistory(limit: number = 100): Promise<CashHistoryEntry[]> {
    return await getJson("api/cash", new Map([["limit", limit]]))
}

export async function submitCash(amount: number, note: string, type: string) {
    console.log(`Submit ${amount} with note "${note}" and type ${type}`)
    await postJson("api/cash", {date: Date.now(), amount: amount, note: note, type: type} as CashDto)
}
